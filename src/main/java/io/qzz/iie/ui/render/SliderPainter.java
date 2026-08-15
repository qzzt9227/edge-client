package io.qzz.iie.ui.render;

import io.qzz.iie.ui.component.control.SliderControl;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import java.util.Objects;

/** 统一绘制所有使用 {@link SliderControl} 的滑块。 */
public final class SliderPainter {
	private SliderPainter() {
	}

	public static void draw(
		UiPainter painter,
		Rect bounds,
		SliderControl slider,
		String displayedValue
	) {
		Objects.requireNonNull(painter, "painter");
		Objects.requireNonNull(bounds, "bounds");
		Objects.requireNonNull(slider, "slider");
		Objects.requireNonNull(displayedValue, "displayedValue");

		int trackHeight = 6;
		int trackY = bounds.top() + (int) (bounds.height() - trackHeight) / 2;
		painter.roundedRect(
			bounds.left(),
			trackY,
			(int) bounds.width(),
			trackHeight,
			trackHeight / 2,
			ClickGuiTheme.CONTROL_OFF
		);
		int filled = (int) Math.round(bounds.width() * slider.fraction());
		if (filled > 0) {
			painter.roundedRect(
				bounds.left(),
				trackY,
				filled,
				trackHeight,
				trackHeight / 2,
				ClickGuiTheme.ACCENT
			);
		}
		int thumbSize = 12;
		int thumbX = bounds.left()
			+ Math.clamp(filled - thumbSize / 2, 0, Math.max(0, (int) bounds.width() - thumbSize));
		int thumbY = bounds.top() + (int) (bounds.height() - thumbSize) / 2;
		painter.roundedRect(
			thumbX,
			thumbY,
			thumbSize,
			thumbSize,
			thumbSize / 2,
			ClickGuiTheme.ACCENT
		);
		painter.text(
			displayedValue,
			bounds.right() - painter.textWidth(displayedValue),
			bounds.top() - painter.lineHeight() - 2,
			ClickGuiTheme.TEXT_SECONDARY
		);
	}
}
