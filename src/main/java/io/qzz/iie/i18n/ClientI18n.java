package io.qzz.iie.i18n;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.qzz.iie.Client;
import net.minecraft.network.chat.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 客户端独立国际化与语言管理服务。
 *
 * <p>允许用户在 ClickGUI 中切换模组显示语言（中文/英文/跟随游戏），
 * 而无需更改 Minecraft 原版游戏的全局语言。</p>
 */
public final class ClientI18n {
	public static final String LANG_ZH_CN = "zh_cn";
	public static final String LANG_EN_US = "en_us";
	public static final String LANG_AUTO = "auto";

	private static final Map<String, String> ZH_CN_MAP = new HashMap<>();
	private static final Map<String, String> EN_US_MAP = new HashMap<>();
	private static volatile String currentLanguage = LANG_AUTO;

	static {
		loadLanguageMap(LANG_ZH_CN, ZH_CN_MAP);
		loadLanguageMap(LANG_EN_US, EN_US_MAP);
	}

	private ClientI18n() {
	}

	public static String currentLanguage() {
		return currentLanguage;
	}

	public static void setLanguage(String language) {
		if (language == null || language.isBlank()) {
			currentLanguage = LANG_AUTO;
		} else {
			currentLanguage = language.toLowerCase();
		}
	}

	/**
	 * 获取指定翻译键的本地化字符串。
	 *
	 * <p>在指定语言模式下，优先从独立字典查找；若未找到或处于 auto 模式，
	 * 回退到原版 {@link Component#translatable(String)}。</p>
	 */
	public static String translate(String key) {
		if (key == null) {
			return "";
		}

		if (LANG_ZH_CN.equals(currentLanguage)) {
			String val = ZH_CN_MAP.get(key);
			if (val != null) {
				return val;
			}
		} else if (LANG_EN_US.equals(currentLanguage)) {
			String val = EN_US_MAP.get(key);
			if (val != null) {
				return val;
			}
		}

		try {
			return Component.translatable(key).getString();
		} catch (Throwable ignored) {
			// 在无 Minecraft 环境（如轻量单测）下回退到内置字典
			if (ZH_CN_MAP.containsKey(key)) {
				return ZH_CN_MAP.get(key);
			}
			if (EN_US_MAP.containsKey(key)) {
				return EN_US_MAP.get(key);
			}
			return key;
		}
	}

	/**
	 * 获取格式化后的本地化字符串。
	 */
	public static String translateFormatted(String key, Object... args) {
		String template = translate(key);
		try {
			return String.format(template, args);
		} catch (Exception e) {
			return template;
		}
	}

	public static Map<String, String> getMap(String lang) {
		if (LANG_ZH_CN.equals(lang)) {
			return Collections.unmodifiableMap(ZH_CN_MAP);
		}
		if (LANG_EN_US.equals(lang)) {
			return Collections.unmodifiableMap(EN_US_MAP);
		}
		return Collections.emptyMap();
	}

	private static void loadLanguageMap(String langCode, Map<String, String> targetMap) {
		String resourcePath = "/assets/client/lang/" + langCode + ".json";
		try (InputStream in = ClientI18n.class.getResourceAsStream(resourcePath)) {
			if (in == null) {
				return;
			}
			try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
					if (entry.getValue().isJsonPrimitive()) {
						targetMap.put(entry.getKey(), entry.getValue().getAsString());
					}
				}
			}
		} catch (Throwable t) {
			Client.LOGGER.error("Failed to load language resource: {}", resourcePath, t);
		}
	}
}
