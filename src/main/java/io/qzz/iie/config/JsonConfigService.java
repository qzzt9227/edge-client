package io.qzz.iie.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.qzz.iie.Client;
import io.qzz.iie.api.hud.HudPosition;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.ColorSetting;
import io.qzz.iie.setting.DoubleRangeSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;
import io.qzz.iie.setting.JsonSetting;
import io.qzz.iie.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * 通用客户端配置 API。
 *
 * <p>模块只需声明稳定的模块 ID、设置 ID 和选项 ID；本服务会自动持久化
 * 已注册模块的开关、设置和快捷键，不需要模块自己处理 JSON。</p>
 */
public final class JsonConfigService {
	public static final int SCHEMA_VERSION = 1;

	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.create();

	private final ModuleManager moduleManager;
	private final Path configPath;
	private final Set<Setting<?>> observedSettings =
		Collections.newSetFromMap(new IdentityHashMap<>());
	private String savedSnapshot;
	private boolean loading;

	public JsonConfigService(ModuleManager moduleManager, Path configPath) {
		this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager");
		this.configPath = Objects.requireNonNull(configPath, "configPath").toAbsolutePath().normalize();
		observeRegisteredSettings();
		moduleManager.addChangeListener(this::modelChanged);
		io.qzz.iie.ui.panel.CategoryPositionManager.setSaveCallback(this::modelChanged);
	}

	public static JsonConfigService atDefaultPath(ModuleManager moduleManager) {
		Path gameDirectory = FabricLoader.getInstance().getGameDir();
		return new JsonConfigService(
			moduleManager,
			gameDirectory.resolve("edge-client/cfg/edge-config.json")
		);
	}

	public Path configPath() {
		return configPath;
	}

	/**
	 * 启动时加载配置。配置不存在视为正常首次启动。
	 */
	public synchronized boolean load() {
		if (!Files.exists(configPath)) {
			savedSnapshot = snapshot();
			return true;
		}

		loading = true;
		try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
			JsonElement parsed = JsonParser.parseReader(reader);
			if (!parsed.isJsonObject()) {
				throw new IllegalArgumentException("Config root must be a JSON object");
			}
			apply(parsed.getAsJsonObject());
			savedSnapshot = snapshot();
			return true;
		} catch (IOException | RuntimeException error) {
			Client.LOGGER.error("Failed to load Edge Client config from {}", configPath, error);
			savedSnapshot = snapshot();
			return false;
		} finally {
			loading = false;
		}
	}

	/**
	 * 状态发生变化时写盘；无变化时不会重复改写文件。
	 */
	public synchronized boolean saveIfChanged() {
		String currentSnapshot = snapshot();
		if (currentSnapshot.equals(savedSnapshot)) {
			return false;
		}
		return writeSnapshot(currentSnapshot);
	}

	/**
	 * 强制写入当前状态，用于显式保存和客户端关闭阶段。
	 */
	public synchronized boolean saveNow() {
		return writeSnapshot(snapshot());
	}

	private void apply(JsonObject root) {
		JsonElement guiLayoutElement = root.get("guiLayout");
		if (guiLayoutElement != null && guiLayoutElement.isJsonObject()) {
			JsonObject layoutObj = guiLayoutElement.getAsJsonObject();
			java.util.Map<String, io.qzz.iie.ui.panel.CategoryPositionManager.PanelState> loaded = new java.util.LinkedHashMap<>();
			for (String key : layoutObj.keySet()) {
				JsonElement elem = layoutObj.get(key);
				if (elem != null && elem.isJsonObject()) {
					JsonObject p = elem.getAsJsonObject();
					int x = p.has("x") && p.get("x").isJsonPrimitive() && p.get("x").getAsJsonPrimitive().isNumber()
						? p.get("x").getAsInt() : 20;
					int y = p.has("y") && p.get("y").isJsonPrimitive() && p.get("y").getAsJsonPrimitive().isNumber()
						? p.get("y").getAsInt() : 20;
					boolean opened = !p.has("opened") || !p.get("opened").isJsonPrimitive() || !p.get("opened").getAsJsonPrimitive().isBoolean()
						|| p.get("opened").getAsBoolean();
					java.util.Set<String> expanded = new java.util.HashSet<>();
					if (p.has("expanded") && p.get("expanded").isJsonArray()) {
						for (JsonElement item : p.get("expanded").getAsJsonArray()) {
							if (item.isJsonPrimitive() && item.getAsJsonPrimitive().isString()) {
								expanded.add(item.getAsString());
							}
						}
					}
					loaded.put(key, new io.qzz.iie.ui.panel.CategoryPositionManager.PanelState(x, y, opened, expanded));
				}
			}
			io.qzz.iie.ui.panel.CategoryPositionManager.setAllStates(loaded);
		}

		JsonElement modulesElement = root.get("modules");
		if (modulesElement == null || !modulesElement.isJsonObject()) {
			return;
		}
		JsonObject modules = modulesElement.getAsJsonObject();
		for (Module module : moduleManager.modules()) {
			JsonElement moduleElement = modules.get(module.id().toString());
			if (moduleElement == null || !moduleElement.isJsonObject()) {
				continue;
			}
			applyModuleSettings(module, moduleElement.getAsJsonObject());
		}
		for (Module module : moduleManager.modules()) {
			JsonElement moduleElement = modules.get(module.id().toString());
			if (moduleElement == null || !moduleElement.isJsonObject()) {
				continue;
			}
			JsonElement enabled = moduleElement.getAsJsonObject().get("enabled");
			if (module.metadata().toggleable()
				&& enabled != null && enabled.isJsonPrimitive()
				&& enabled.getAsJsonPrimitive().isBoolean()) {
				moduleManager.setEnabled(module.id(), enabled.getAsBoolean());
			}
		}
	}

	private static void applyModuleSettings(Module module, JsonObject moduleObject) {
		JsonElement settingsElement = moduleObject.get("settings");
		if (settingsElement == null || !settingsElement.isJsonObject()) {
			return;
		}
		JsonObject settings = settingsElement.getAsJsonObject();
		for (Setting<?> setting : module.settings()) {
			JsonElement value = settings.get(setting.id());
			if (value == null) {
				continue;
			}
			try {
				applySetting(setting, value);
			} catch (RuntimeException error) {
				Client.LOGGER.warn(
					"Ignoring invalid config value for module '{}' setting '{}'",
					module.id(),
					setting.id(),
					error
				);
			}
		}
	}

	private static void applySetting(Setting<?> setting, JsonElement value) {
		if (setting instanceof JsonSetting jsonSetting) {
			jsonSetting.decodeJson(value);
			return;
		}
		if (setting instanceof HudPositionSetting positionSetting
			&& value.isJsonObject()) {
			JsonObject position = value.getAsJsonObject();
			JsonElement x = position.get("x");
			JsonElement y = position.get("y");
			if (x == null || y == null
				|| !x.isJsonPrimitive() || !y.isJsonPrimitive()
				|| !x.getAsJsonPrimitive().isNumber()
				|| !y.getAsJsonPrimitive().isNumber()) {
				throw new IllegalArgumentException("HUD position must contain numeric x and y");
			}
			double positionX = x.getAsDouble();
			double positionY = y.getAsDouble();
			if (!Double.isFinite(positionX) || !Double.isFinite(positionY)) {
				throw new IllegalArgumentException("HUD position must be finite");
			}
			positionSetting.set(new HudPosition(positionX, positionY));
			return;
		}
		if (setting instanceof DoubleRangeSetting rangeSetting) {
			if (value.isJsonObject()) {
				JsonObject obj = value.getAsJsonObject();
				double min = obj.has("min") && obj.get("min").isJsonPrimitive() && obj.get("min").getAsJsonPrimitive().isNumber()
					? obj.get("min").getAsDouble() : rangeSetting.rangeMinimum();
				double max = obj.has("max") && obj.get("max").isJsonPrimitive() && obj.get("max").getAsJsonPrimitive().isNumber()
					? obj.get("max").getAsDouble() : rangeSetting.rangeMaximum();
				rangeSetting.setRange(min, max);
			} else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
				double num = value.getAsDouble();
				rangeSetting.setRange(num, num);
			}
			return;
		}
		if (!value.isJsonPrimitive()) {
			return;
		}
		if (setting instanceof BooleanSetting booleanSetting
			&& value.getAsJsonPrimitive().isBoolean()) {
			booleanSetting.set(value.getAsBoolean());
		} else if (setting instanceof DoubleSetting doubleSetting
			&& value.getAsJsonPrimitive().isNumber()) {
			double number = value.getAsDouble();
			if (!Double.isFinite(number)) {
				throw new IllegalArgumentException("Number must be finite");
			}
			doubleSetting.set(number);
		} else if (setting instanceof ChoiceSetting<?> choiceSetting
			&& value.getAsJsonPrimitive().isString()) {
			choiceSetting.selectOptionId(value.getAsString());
		} else if (setting instanceof ColorSetting colorSetting) {
			if (value.getAsJsonPrimitive().isString()) {
				colorSetting.setHex(value.getAsString());
			} else if (value.getAsJsonPrimitive().isNumber()) {
				colorSetting.set(value.getAsInt());
			}
		} else if (setting instanceof KeybindSetting keybindSetting
			&& value.getAsJsonPrimitive().isNumber()) {
			int keyCode = value.getAsInt();
			if (keyCode < 0) {
				keybindSetting.clear();
			} else {
				keybindSetting.bind(keyCode);
			}
		}
	}

	private String snapshot() {
		JsonObject root = new JsonObject();
		root.addProperty("schemaVersion", SCHEMA_VERSION);

		JsonObject guiLayout = new JsonObject();
		for (java.util.Map.Entry<String, io.qzz.iie.ui.panel.CategoryPositionManager.PanelState> entry
			: io.qzz.iie.ui.panel.CategoryPositionManager.getAllStates().entrySet()) {
			JsonObject obj = new JsonObject();
			obj.addProperty("x", entry.getValue().x());
			obj.addProperty("y", entry.getValue().y());
			obj.addProperty("opened", entry.getValue().opened());
			if (!entry.getValue().expandedModules().isEmpty()) {
				com.google.gson.JsonArray expandedArr = new com.google.gson.JsonArray();
				for (String modId : entry.getValue().expandedModules()) {
					expandedArr.add(modId);
				}
				obj.add("expanded", expandedArr);
			}
			guiLayout.add(entry.getKey(), obj);
		}
		if (!guiLayout.isEmpty()) {
			root.add("guiLayout", guiLayout);
		}

		JsonObject modules = new JsonObject();
		for (Module module : moduleManager.modules()) {
			JsonObject moduleObject = new JsonObject();
			if (module.metadata().toggleable()) {
				moduleObject.addProperty("enabled", module.isEnabled());
			}
			JsonObject settings = new JsonObject();
			for (Setting<?> setting : module.settings()) {
				addSetting(settings, setting);
			}
			moduleObject.add("settings", settings);
			modules.add(module.id().toString(), moduleObject);
		}
		root.add("modules", modules);
		return GSON.toJson(root);
	}

	private static void addSetting(JsonObject destination, Setting<?> setting) {
		if (setting instanceof JsonSetting jsonSetting) {
			destination.add(setting.id(), jsonSetting.encodeJson());
		} else if (setting instanceof BooleanSetting booleanSetting) {
			destination.addProperty(setting.id(), booleanSetting.value());
		} else if (setting instanceof DoubleSetting doubleSetting) {
			destination.addProperty(setting.id(), doubleSetting.value());
		} else if (setting instanceof DoubleRangeSetting rangeSetting) {
			JsonObject rangeObj = new JsonObject();
			rangeObj.addProperty("min", rangeSetting.minimum());
			rangeObj.addProperty("max", rangeSetting.maximum());
			destination.add(setting.id(), rangeObj);
		} else if (setting instanceof ChoiceSetting<?> choiceSetting) {
			destination.addProperty(setting.id(), choiceSetting.selectedOption().id());
		} else if (setting instanceof ColorSetting colorSetting) {
			destination.addProperty(setting.id(), colorSetting.hex());
		} else if (setting instanceof KeybindSetting keybindSetting) {
			destination.addProperty(setting.id(), keybindSetting.value().keyCode());
		} else if (setting instanceof HudPositionSetting positionSetting) {
			JsonObject position = new JsonObject();
			position.addProperty("x", positionSetting.value().x());
			position.addProperty("y", positionSetting.value().y());
			destination.add(setting.id(), position);
		}
	}

	private boolean writeSnapshot(String snapshot) {
		Path parent = configPath.getParent();
		Path temporary = configPath.resolveSibling(configPath.getFileName() + ".tmp");
		try {
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(
				temporary,
				snapshot,
				StandardCharsets.UTF_8,
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE
			);
			moveIntoPlace(temporary);
			savedSnapshot = snapshot;
			return true;
		} catch (IOException error) {
			Client.LOGGER.error("Failed to save Edge Client config to {}", configPath, error);
			try {
				Files.deleteIfExists(temporary);
			} catch (IOException cleanupError) {
				Client.LOGGER.debug("Failed to remove temporary config {}", temporary, cleanupError);
			}
			return false;
		}
	}

	private void moveIntoPlace(Path temporary) throws IOException {
		try {
			Files.move(
				temporary,
				configPath,
				StandardCopyOption.ATOMIC_MOVE,
				StandardCopyOption.REPLACE_EXISTING
			);
		} catch (AtomicMoveNotSupportedException ignored) {
			Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void modelChanged() {
		observeRegisteredSettings();
		if (!loading) {
			saveIfChanged();
		}
	}

	private void observeRegisteredSettings() {
		for (Module module : moduleManager.modules()) {
			for (Setting<?> setting : module.settings()) {
				if (observedSettings.add(setting)) {
					setting.addChangeListener(this::modelChanged);
				}
			}
		}
	}
}
