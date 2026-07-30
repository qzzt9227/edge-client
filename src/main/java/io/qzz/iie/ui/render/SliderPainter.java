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

		int trackY = bounds.top() + 4;
		painter.roundedRect(
			bounds.left(),
			trackY,
			(int) bounds.width(),
			4,
			2,
			ClickGuiTheme.CONTROL_OFF
		);
		int filled = (int) Math.round(bounds.width() * slider.fraction());
		painter.roundedRect(
			bounds.left(),
			trackY,
			filled,
			4,
			2,
			ClickGuiTheme.ACCENT
		);
		int thumbX = bounds.left()
			+ Math.clamp(filled - 4, 0, Math.max(0, (int) bounds.width() - 8));
		painter.roundedRect(
			thumbX,
			bounds.top(),
			8,
			12,
			4,
			ClickGuiTheme.TEXT_PRIMARY
		);
		painter.text(
			displayedValue,
			bounds.right() - painter.textWidth(displayedValue),
			bounds.top() - painter.lineHeight() - 2,
			ClickGuiTheme.TEXT_SECONDARY
		);
	}
}
