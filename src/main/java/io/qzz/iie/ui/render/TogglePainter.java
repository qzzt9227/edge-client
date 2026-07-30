package io.qzz.iie.ui.render;

import io.qzz.iie.ui.animation.ArgbColor;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.theme.ClickGuiTheme;

public final class TogglePainter {
	private TogglePainter() {
	}

	public static void draw(UiPainter painter, Rect bounds, double progress) {
		int trackColor = ArgbColor.interpolate(
			ClickGuiTheme.CONTROL_OFF,
			ClickGuiTheme.ACCENT,
			progress
		);
		painter.roundedRect(
			bounds.left(),
			bounds.top(),
			(int) bounds.width(),
			(int) bounds.height(),
			(int) bounds.height() / 2,
			trackColor
		);

		int thumbSize = (int) bounds.height() - 6;
		int thumbStart = bounds.left() + 3;
		int thumbEnd = bounds.right() - thumbSize - 3;
		int thumbX = (int) Math.round(thumbStart + (thumbEnd - thumbStart) * progress);
		int thumbColor = ArgbColor.interpolate(
			ClickGuiTheme.ICON_MUTED,
			ClickGuiTheme.CONTROL_DARK,
			progress
		);
		painter.roundedRect(
			thumbX,
			bounds.top() + 3,
			thumbSize,
			thumbSize,
			thumbSize / 2,
			thumbColor
		);
	}
}
