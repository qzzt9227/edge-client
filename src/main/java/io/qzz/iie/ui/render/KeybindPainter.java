package io.qzz.iie.ui.render;

import io.qzz.iie.ui.component.control.KeybindControl;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import java.util.Objects;

public final class KeybindPainter {
	private KeybindPainter() {
	}

	public static void draw(UiPainter painter, KeybindControl control, String displayText) {
		Objects.requireNonNull(displayText, "displayText");
		Rect bounds = control.inputBounds();
		int outline = control.isListening() ? ClickGuiTheme.ACCENT : ClickGuiTheme.OUTLINE;
		int textColor = control.isListening()
			? ClickGuiTheme.ACCENT
			: ClickGuiTheme.TEXT_PRIMARY;

		painter.roundedRectWithBorder(
			bounds.left(),
			bounds.top(),
			(int) bounds.width(),
			(int) bounds.height(),
			7,
			1,
			ClickGuiTheme.CONTROL_DARK,
			outline
		);
		String trimmed = painter.trimToWidth(displayText, (int) bounds.width() - 16);
		painter.text(
			trimmed,
			bounds.left() + ((int) bounds.width() - painter.textWidth(trimmed)) / 2,
			bounds.top() + ((int) bounds.height() - painter.lineHeight()) / 2,
			textColor
		);
	}
}
