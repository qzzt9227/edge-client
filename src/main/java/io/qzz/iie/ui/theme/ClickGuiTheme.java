package io.qzz.iie.ui.theme;

public final class ClickGuiTheme {
	// Panel tokens
	public static final int OVERLAY = 0x64000000; // Semi-transparent screen background
	public static final int PANEL_HEADER = 0xCC000000; // Dark opaque category header
	public static final int PANEL_HEADER_HOVER = 0xEE1A1A1A;
	public static final int PANEL_BODY = 0x64000000; // Semi-transparent category body
	public static final int PANEL_BORDER = 0x80000000;

	// Module state colors
	public static final int MODULE_ENABLED = 0xFF5FA8FF; // Vibrant theme color
	public static final int MODULE_DISABLED = 0xFF666666;
	public static final int MODULE_HOVER = 0x25FFFFFF;

	// Setting item colors
	public static final int SETTING_TEXT = 0xFFE0E0E0;
	public static final int SETTING_TRUE = 0xFF44E044;
	public static final int SETTING_FALSE = 0xFFE04444;
	public static final int SETTING_MODE = 0xFF7396FF;
	public static final int SETTING_BIND = 0xFF5FA8FF;
	public static final int SETTING_BINDING = 0xFFFFCC00;
	public static final int SETTING_ACTION = 0xFF7396FF;

	// Slider tokens
	public static final int SLIDER_TRACK = 0x80303030;
	public static final int SLIDER_FILL = 0xFF5FA8FF;

	// General & Secondary Screen tokens
	public static final int WATERMARK = 0xFF3CA2FD;
	public static final int SCROLLBAR = 0x60FFFFFF;
	public static final int TEXT_PRIMARY = 0xFFFFFFFF;
	public static final int TEXT_SECONDARY = 0xFFA0A0A0;
	public static final int ACCENT = 0xFF5FA8FF;
	public static final int CONTROL_DARK = 0xFF161820;
	public static final int CONTROL_OFF = 0xFF343844;
	public static final int ROW_HOVER = 0x20FFFFFF;
	public static final int OUTLINE = 0xFF44474F;
	public static final int WINDOW_BORDER = 0xFF12141C;
	public static final int SIDEBAR = 0xFF1A1C24;
	public static final int CONTENT = 0xFF20232C;
	public static final int SELECTED = 0xFF004A77;
	public static final int CHOICE_OPTION_HOVER = 0xFF3D4350;
	public static final int ICON_MUTED = 0xFF9094A0;

	private ClickGuiTheme() {
	}

	/**
	 * 计算随时间和索引变化的动态彩虹色 (Chroma)。
	 */
	public static int getChromaColor(long time, long offset) {
		long speed = 3000L;
		float cycle = 1.0F - (float) (Math.abs(time - offset * 300L) % speed) / (float) speed;
		return java.awt.Color.HSBtoRGB(cycle, 0.65F, 1.0F);
	}
}
