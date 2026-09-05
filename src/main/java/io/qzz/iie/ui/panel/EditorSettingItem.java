package io.qzz.iie.ui.panel;

import io.qzz.iie.setting.EditorSetting;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class EditorSettingItem implements InlineSettingItem {
	private final EditorSetting<?> setting;
	private final Consumer<EditorSetting<?>> editorOpener;

	public EditorSettingItem(
		EditorSetting<?> setting,
		Consumer<EditorSetting<?>> editorOpener
	) {
		this.setting = Objects.requireNonNull(setting, "setting");
		this.editorOpener = Objects.requireNonNull(editorOpener, "editorOpener");
	}

	@Override
	public EditorSetting<?> setting() {
		return setting;
	}

	@Override
	public int height() {
		return 11;
	}

	@Override
	public int preferredWidth(UiPainter painter) {
		String label = io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey()) + ": [Edit]";
		return painter.textWidth(label) + 8;
	}

	@Override
	public void render(
		UiPainter painter,
		int x,
		int y,
		int width,
		int mouseX,
		int mouseY,
		long time,
		AtomicInteger colorIndex
	) {
		boolean hovered = mouseX >= x && mouseX <= x + width
			&& mouseY >= y && mouseY < y + height();
		String label = io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey()) + ": ";
		String action = "[Edit]";
		int indentOffset = indent() * 6;
		if (indentOffset > 0) {
			painter.fill(x + 2 + indentOffset - 3, y + 2, 1, height() - 4, ClickGuiTheme.PANEL_BORDER);
		}
		int availableWidth = width - 8 - indentOffset;
		int textX = x + 4 + indentOffset;
		int textY = y + 1;

		painter.marqueeTwoPartText(
			label,
			ClickGuiTheme.SETTING_TEXT,
			action,
			ClickGuiTheme.SETTING_ACTION,
			textX,
			textY,
			availableWidth,
			hovered,
			time
		);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height()) {
			if (button == 0) {
				editorOpener.accept(setting);
				return true;
			}
		}
		return false;
	}
}
