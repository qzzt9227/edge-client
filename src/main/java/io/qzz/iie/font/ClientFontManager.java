package io.qzz.iie.font;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import io.qzz.iie.setting.ChoiceOption;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * 客户端自定义字体管理器：
 * 负责自动创建字体目录（<gameDir>/edge-client/font 与 ~/edge-client/font）、
 * 扫描 TTF/OTF 字体文件、动态注册 FontSet 并管理当前生效的 FontDescription。
 */
public final class ClientFontManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("EdgeClient-Font");
	public static final String DEFAULT_FONT = "default";

	private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".ttf", ".otf");
	private static FontDescription activeFontDescription = null;
	private static String currentFontName = DEFAULT_FONT;

	private static final Map<String, Identifier> LOADED_FONTS = new HashMap<>();

	private ClientFontManager() {
	}

	/**
	 * 确保所有字体存储目录存在，不存在时自动创建。
	 */
	public static void ensureDirectoriesExist() {
		for (Path dir : getFontDirectories()) {
			try {
				if (!Files.exists(dir)) {
					Files.createDirectories(dir);
					LOGGER.info("Created custom font directory: {}", dir);
				}
			} catch (IOException e) {
				LOGGER.warn("Failed to create font directory: {}", dir, e);
			}
		}
	}

	/**
	 * 获取所有可能存放自定义字体的目录。
	 */
	public static List<Path> getFontDirectories() {
		List<Path> dirs = new ArrayList<>();
		try {
			Path gameDir = FabricLoader.getInstance().getGameDir().resolve("edge-client/font");
			dirs.add(gameDir);
		} catch (Throwable ignored) {
			dirs.add(Path.of("edge-client/font"));
		}

		String userHome = System.getProperty("user.home");
		if (userHome != null && !userHome.isBlank()) {
			Path homeDir = Path.of(userHome, "edge-client", "font");
			if (!dirs.contains(homeDir)) {
				dirs.add(homeDir);
			}
		}
		return dirs;
	}

	/**
	 * 扫描所有支持的字体文件（.ttf 与 .otf）。
	 */
	public static List<String> scanAvailableFontFiles() {
		ensureDirectoriesExist();
		Set<String> fontFiles = new LinkedHashSet<>();

		for (Path dir : getFontDirectories()) {
			if (!Files.exists(dir) || !Files.isDirectory(dir)) {
				continue;
			}
			try (Stream<Path> stream = Files.list(dir)) {
				stream.filter(Files::isRegularFile).forEach(path -> {
					String fileName = path.getFileName().toString();
					String lower = fileName.toLowerCase(Locale.ROOT);
					for (String ext : SUPPORTED_EXTENSIONS) {
						if (lower.endsWith(ext)) {
							fontFiles.add(fileName);
							break;
						}
					}
				});
			} catch (IOException e) {
				LOGGER.warn("Error scanning font directory: {}", dir, e);
			}
		}

		List<String> sorted = new ArrayList<>(fontFiles);
		sorted.sort(String.CASE_INSENSITIVE_ORDER);
		return sorted;
	}

	/**
	 * 获取字体设置项的 ChoiceOption 列表。
	 */
	public static List<ChoiceOption<String>> getAvailableFontOptions() {
		List<ChoiceOption<String>> options = new ArrayList<>();
		options.add(new ChoiceOption<>(DEFAULT_FONT, "client.option.font.default", DEFAULT_FONT));

		Set<String> usedIds = new HashSet<>();
		usedIds.add(DEFAULT_FONT);

		int index = 1;
		for (String fontFile : scanAvailableFontFiles()) {
			String baseId = sanitizeId(fontFile);
			String uniqueId = baseId;
			while (usedIds.contains(uniqueId)) {
				uniqueId = baseId + "_" + (index++);
			}
			usedIds.add(uniqueId);
			options.add(new ChoiceOption<>(uniqueId, fontFile, fontFile));
		}
		return options;
	}

	public static String sanitizeId(String name) {
		if (name == null || name.isBlank()) {
			return "font";
		}
		String sanitized = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
		if (sanitized.isEmpty() || !Character.isLetterOrDigit(sanitized.charAt(0))) {
			sanitized = "f_" + sanitized;
		}
		return sanitized;
	}

	/**
	 * 根据字体名称解析字体文件路径。
	 */
	public static Optional<Path> resolveFontFile(String fontName) {
		if (fontName == null || fontName.isBlank() || fontName.equals(DEFAULT_FONT)) {
			return Optional.empty();
		}
		for (Path dir : getFontDirectories()) {
			Path candidate = dir.resolve(fontName);
			if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
				return Optional.of(candidate);
			}
		}
		return Optional.empty();
	}

	/**
	 * 应用指定字体，立即生效。
	 */
	public static void applyFont(String fontName) {
		if (fontName == null || fontName.isBlank() || fontName.equals(DEFAULT_FONT)) {
			currentFontName = DEFAULT_FONT;
			activeFontDescription = null;
			return;
		}

		Optional<Path> fontPath = resolveFontFile(fontName);
		if (fontPath.isEmpty()) {
			LOGGER.warn("Custom font file not found: {}, falling back to default", fontName);
			currentFontName = DEFAULT_FONT;
			activeFontDescription = null;
			return;
		}

		currentFontName = fontName;
		try {
			Identifier fontId = getOrRegisterFont(fontName, fontPath.get());
			if (fontId != null) {
				activeFontDescription = new FontDescription.Resource(fontId);
			} else {
				activeFontDescription = null;
			}
		} catch (Throwable t) {
			LOGGER.warn("Failed to load custom font: {}", fontName, t);
			activeFontDescription = null;
		}
	}

	/**
	 * 获取当前生效的 FontDescription（若为默认原版字体则返回 null）。
	 */
	public static FontDescription getActiveFontDescription() {
		return activeFontDescription;
	}

	/**
	 * 获取当前选中的字体名称。
	 */
	public static String getCurrentFontName() {
		return currentFontName;
	}

	private static Identifier getOrRegisterFont(String fontName, Path fontPath) {
		if (LOADED_FONTS.containsKey(fontName)) {
			return LOADED_FONTS.get(fontName);
		}

		String sanitized = fontName.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9_.-]", "_");
		Identifier fontId = Identifier.fromNamespaceAndPath("client", "custom_font_" + sanitized);

		try {
			Minecraft client = Minecraft.getInstance();
			FontManager fontManager = getMinecraftFontManager(client);
			if (fontManager == null) {
				LOADED_FONTS.put(fontName, fontId);
				return fontId;
			}

			byte[] fontBytes = Files.readAllBytes(fontPath);
			ByteBuffer buffer = MemoryUtil.memAlloc(fontBytes.length);
			buffer.put(fontBytes);
			buffer.flip();

			FT_Face face;
			try (MemoryStack stack = MemoryStack.stackPush()) {
				PointerBuffer pLibrary = stack.mallocPointer(1);
				int err = FreeType.FT_Init_FreeType(pLibrary);
				if (err != 0) {
					throw new IllegalStateException("Failed to initialize FreeType: error code " + err);
				}
				long library = pLibrary.get(0);

				PointerBuffer pFace = stack.mallocPointer(1);
				err = FreeType.FT_New_Memory_Face(library, buffer, 0L, pFace);
				if (err != 0) {
					throw new IllegalStateException("Failed to create FreeType face: error code " + err);
				}
				face = FT_Face.create(pFace.get(0));
			}

			TrueTypeGlyphProvider provider = new TrueTypeGlyphProvider(
				buffer,
				face,
				11.0f,
				2.0f,
				0.0f,
				0.0f,
				""
			);

			GlyphProvider.Conditional conditional = new GlyphProvider.Conditional(
				provider,
				FontOption.Filter.ALWAYS_PASS
			);

			List<GlyphProvider.Conditional> providerList = new ArrayList<>();
			providerList.add(conditional);

			// 将原版默认字体的 provider 作为 fallback 链接在后面，防止非 CJK 字体缺失中文字符
			try {
				FontSet defaultFontSet = getDefaultFontSet(fontManager);
				if (defaultFontSet != null) {
					for (Field f : FontSet.class.getDeclaredFields()) {
						if (List.class.isAssignableFrom(f.getType())) {
							f.setAccessible(true);
							List<?> list = (List<?>) f.get(defaultFontSet);
							if (list != null) {
								for (Object item : list) {
									if (item instanceof GlyphProvider.Conditional c) {
										providerList.add(c);
									}
								}
								break;
							}
						}
					}
				}
			} catch (Throwable ignored) {
			}

			TextureManager textureManager = client.getTextureManager();
			GlyphStitcher stitcher = new GlyphStitcher(textureManager, fontId);
			FontSet fontSet = new FontSet(stitcher);
			fontSet.reload(providerList, Set.of());

			// 注入到 FontManager.fontSets 中
			injectFontSet(fontManager, fontId, fontSet);

			LOADED_FONTS.put(fontName, fontId);
			LOGGER.info("Successfully loaded and registered custom font: {} -> {}", fontName, fontId);
			return fontId;
		} catch (Throwable t) {
			LOGGER.warn("Error registering custom font {}: {}", fontName, t.getMessage(), t);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static void injectFontSet(FontManager fontManager, Identifier id, FontSet fontSet) {
		try {
			for (Field field : FontManager.class.getDeclaredFields()) {
				if (Map.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					Map<?, ?> map = (Map<?, ?>) field.get(fontManager);
					if (map != null) {
						((Map<Identifier, FontSet>) map).put(id, fontSet);
						break;
					}
				}
			}
		} catch (Throwable t) {
			LOGGER.warn("Failed to inject FontSet into FontManager: {}", t.getMessage());
		}
	}

	private static FontSet getDefaultFontSet(FontManager fontManager) {
		if (fontManager == null) {
			return null;
		}
		try {
			for (Field field : FontManager.class.getDeclaredFields()) {
				if (Map.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					Map<?, ?> map = (Map<?, ?>) field.get(fontManager);
					if (map != null) {
						Object set = map.get(Identifier.fromNamespaceAndPath("minecraft", "default"));
						if (set instanceof FontSet fontSet) {
							return fontSet;
						}
					}
				}
			}
		} catch (Throwable ignored) {
		}
		return null;
	}

	private static FontManager getMinecraftFontManager(Minecraft client) {
		if (client == null) {
			return null;
		}
		try {
			for (Field field : Minecraft.class.getDeclaredFields()) {
				if (FontManager.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					return (FontManager) field.get(client);
				}
			}
		} catch (Throwable ignored) {
		}
		return null;
	}
}
