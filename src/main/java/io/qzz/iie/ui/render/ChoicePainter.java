package io.qzz.iie.ui.render;

import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.ui.animation.ArgbColor;
import io.qzz.iie.ui.component.control.ChoiceControl;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import java.util.Objects;
import java.util.function.Function;

public final class ChoicePainter {
	private ChoicePainter() {
	}

	public static void drawHeader(
		UiPainter painter,
		ChoiceControl control,
		Function<String, String> translate
	) {
		Objects.requireNonNull(translate, "translate");
		Rect bounds = control.collapsedBounds();
		painter.roundedRectWithBorder(
			bounds.left(),
			bounds.top(),
			(int) bounds.width(),
			(int) bounds.height(),
			7,
			1,
			ClickGuiTheme.CONTROL_DARK,
			ClickGuiTheme.OUTLINE
		);
		String value = translate.apply(control.setting().selectedOption().translationKey());
		painter.text(
			painter.trimToWidth(value, (int) bounds.width() - 28),
			bounds.left() + 8,
			bounds.top() + (int) (bounds.height() - painter.lineHeight()) / 2,
			ClickGuiTheme.TEXT_PRIMARY
		);
		painter.text(
			control.isExpanded() ? "^" : "v",
			bounds.right() - 14,
			bounds.top() + (int) (bounds.height() - painter.lineHeight()) / 2,
			ClickGuiTheme.TEXT_SECONDARY
		);
	}

	public static void drawDrawer(
		UiPainter painter,
		ChoiceControl control,
		double progress,
		Function<String, String> translate
	) {
		if (progress <= 0.0) {
			return;
		}
		int offset = (int) Math.round((1.0 - progress) * -6.0);
		Rect drawer = control.drawerBounds();
		int background = ArgbColor.interpolate(
			0x002F353F,
			ClickGuiTheme.ROW_HOVER,
			progress
		);
		painter.roundedRect(
			drawer.left(),
			drawer.top() + offset,
			(int) drawer.width(),
			(int) drawer.height(),
			7,
			background
		);

		for (int index = 0; index < control.setting().options().size(); index++) {
			ChoiceOption<?> option = control.setting().options().get(index);
			Rect optionBounds = control.optionBounds(index);
			if (control.hoveredOptionIndex() == index) {
				painter.roundedRect(
					optionBounds.left() + 3,
					optionBounds.top() + offset + 2,
					(int) optionBounds.width() - 6,
					(int) optionBounds.height() - 4,
					5,
					ArgbColor.interpolate(
						0x003C4856,
						ClickGuiTheme.CHOICE_OPTION_HOVER,
						progress
					)
				);
			}
			boolean selected = option.equals(control.setting().selectedOption());
			int color = ArgbColor.interpolate(
				0x00E4E8ED,
				selected ? ClickGuiTheme.ACCENT : ClickGuiTheme.TEXT_PRIMARY,
				progress
			);
			painter.text(
				painter.trimToWidth(
					translate.apply(option.translationKey()),
					(int) optionBounds.width() - 16
				),
				optionBounds.left() + 8,
				optionBounds.top() + offset
					+ (int) (optionBounds.height() - painter.lineHeight()) / 2,
				color
			);
		}
	}
}
