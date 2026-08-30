package io.qzz.iie.setting;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 强类型颜色设置，支持 RGB 或 ARGB 色值存储、十六进制 HTML 代码解析与预置纯色。
 */
public final class ColorSetting extends Setting<Integer> {
	public static final int PRESET_RED = 0xFF0000;
	public static final int PRESET_GREEN = 0x00FF00;
	public static final int PRESET_BLUE = 0x0000FF;
	public static final int PRESET_YELLOW = 0xFFFF00;
	public static final int PRESET_PURPLE = 0x9B59B6;

	public static final List<Integer> PRESETS = List.of(
		PRESET_RED,
		PRESET_GREEN,
		PRESET_BLUE,
		PRESET_YELLOW,
		PRESET_PURPLE
	);

	private final boolean hasAlpha;

	public ColorSetting(String id, String translationKey, int defaultRgb) {
		this(id, translationKey, defaultRgb, false);
	}

	public ColorSetting(
		String id,
		String translationKey,
		int defaultValue,
		boolean hasAlpha
	) {
		super(id, translationKey, normalizeColor(defaultValue, hasAlpha));
		this.hasAlpha = hasAlpha;
	}

	public boolean hasAlpha() {
		return hasAlpha;
	}

	public int red() {
		return (value() >> 16) & 0xFF;
	}

	public int green() {
		return (value() >> 8) & 0xFF;
	}

	public int blue() {
		return value() & 0xFF;
	}

	public int alpha() {
		return (value() >> 24) & 0xFF;
	}

	public int rgb() {
		return value() & 0x00FFFFFF;
	}

	public int argb() {
		return hasAlpha ? value() : (0xFF000000 | (value() & 0x00FFFFFF));
	}

	/**
	 * 返回 HTML 十六进制颜色代码，默认不带 {@code #} 号。
	 */
	public String hex() {
		return formatHex(value(), hasAlpha, false);
	}

	/**
	 * 返回带 {@code #} 号的 HTML 十六进制颜色代码。
	 */
	public String hexWithHash() {
		return formatHex(value(), hasAlpha, true);
	}

	public static final List<Integer> PRESET_COLORS = PRESETS;

	/**
	 * 从十六进制字符串设置颜色。自动且静默转换去除前导 {@code #} 号。
	 */
	public void setHex(String hex) {
		set(parseHex(hex, hasAlpha));
	}

	public void setFromHex(String hex) {
		setHex(hex);
	}

	@Override
	protected Integer normalize(Integer rawValue) {
		Objects.requireNonNull(rawValue, "rawValue");
		return normalizeColor(rawValue, hasAlpha);
	}

	/**
	 * 解析十六进制颜色字符串，静默支持带或不带 {@code #}，支持 3/4/6/8 位格式。
	 */
	public static int parseHex(String hexText, boolean hasAlpha) {
		if (hexText == null) {
			throw new IllegalArgumentException("Hex text cannot be null");
		}
		String clean = hexText.trim();
		if (clean.startsWith("#")) {
			clean = clean.substring(1).trim();
		}
		if (clean.isEmpty()) {
			throw new IllegalArgumentException("Empty hex color string");
		}

		if (clean.length() == 3) {
			// 缩写 RGB: "F0A" -> "FF00AA"
			int r = Integer.parseInt(clean.substring(0, 1), 16) * 17;
			int g = Integer.parseInt(clean.substring(1, 2), 16) * 17;
			int b = Integer.parseInt(clean.substring(2, 3), 16) * 17;
			return hasAlpha ? (0xFF << 24 | r << 16 | g << 8 | b) : (r << 16 | g << 8 | b);
		}

		if (clean.length() == 6) {
			int rgb = Integer.parseInt(clean, 16);
			return hasAlpha ? (0xFF000000 | rgb) : rgb;
		}

		if (clean.length() == 8) {
			long val = Long.parseLong(clean, 16);
			return (int) val;
		}

		throw new IllegalArgumentException("Invalid hex color length: " + clean);
	}

	public static String formatHex(int color, boolean hasAlpha, boolean includeHash) {
		String prefix = includeHash ? "#" : "";
		if (hasAlpha) {
			return String.format(Locale.ROOT, "%s%08X", prefix, color);
		}
		return String.format(Locale.ROOT, "%s%06X", prefix, color & 0x00FFFFFF);
	}

	private static int normalizeColor(int color, boolean hasAlpha) {
		if (!hasAlpha) {
			return color & 0x00FFFFFF;
		}
		return color;
	}
}
