package io.qzz.iie.api.hud;

/**
 * HUD 元素中心点在当前 GUI 视口中的归一化坐标。
 */
public record HudPosition(double x, double y) {
	public HudPosition {
		requireFinite(x, "x");
		requireFinite(y, "y");
		x = Math.clamp(x, 0.0, 1.0);
		y = Math.clamp(y, 0.0, 1.0);
	}

	private static void requireFinite(double value, String name) {
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException(name + " must be finite");
		}
	}
}
