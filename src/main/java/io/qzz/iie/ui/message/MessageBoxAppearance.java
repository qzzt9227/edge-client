package io.qzz.iie.ui.message;

import java.util.Objects;

/**
 * 一帧消息提示框绘制所需的不可变外观快照。
 */
public record MessageBoxAppearance(
	double boxScale,
	double textScale,
	double opacity,
	int textColor,
	String fontResourceId
) {
	public static final int BASE_WIDTH = 220;
	public static final int BASE_HEIGHT = 52;

	public MessageBoxAppearance {
		requireFinitePositive(boxScale, "boxScale");
		requireFinitePositive(textScale, "textScale");
		if (!Double.isFinite(opacity) || opacity < 0.0 || opacity > 1.0) {
			throw new IllegalArgumentException("opacity must be between 0 and 1");
		}
		textColor = 0xFF000000 | textColor & 0x00FFFFFF;
		fontResourceId = Objects.requireNonNull(fontResourceId, "fontResourceId");
		if (!fontResourceId.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
			throw new IllegalArgumentException("Invalid font resource ID: " + fontResourceId);
		}
	}

	public int boxWidth() {
		return Math.max(1, (int) Math.round(BASE_WIDTH * boxScale));
	}

	public int boxHeight() {
		return Math.max(1, (int) Math.round(BASE_HEIGHT * boxScale));
	}

	private static void requireFinitePositive(double value, String name) {
		if (!Double.isFinite(value) || value <= 0.0) {
			throw new IllegalArgumentException(name + " must be finite and positive");
		}
	}
}
