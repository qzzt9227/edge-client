package io.qzz.iie.api.hud;

import io.qzz.iie.ui.layout.Rect;

import java.util.Objects;

/**
 * 将归一化 HUD 位置解析为不会越出视口的元素边界。
 */
public final class HudPositionLayout {
	private HudPositionLayout() {
	}

	public static Rect resolve(
		HudPosition position,
		int viewportWidth,
		int viewportHeight,
		int elementWidth,
		int elementHeight
	) {
		Objects.requireNonNull(position, "position");
		if (viewportWidth < 0 || viewportHeight < 0
			|| elementWidth < 0 || elementHeight < 0) {
			throw new IllegalArgumentException("HUD viewport and element sizes must be non-negative");
		}

		double availableWidth = Math.max(0, viewportWidth - elementWidth);
		double availableHeight = Math.max(0, viewportHeight - elementHeight);
		return new Rect(
			Math.round(availableWidth * position.x()),
			Math.round(availableHeight * position.y()),
			elementWidth,
			elementHeight
		);
	}
}
