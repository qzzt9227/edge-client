package io.qzz.iie.ui.panel;

import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class BooleanSettingItem implements InlineSettingItem {
	private final BooleanSetting setting;

	public BooleanSettingItem(BooleanSetting setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	@Override
	public BooleanSetting setting() {
		return setting;
	}

	@Override
	public int height() {
		return 11;
	}

	@Override
	public int preferredWidth(UiPainter painter) {
		String label = io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey()) + ": ";
		String valStr = setting.value() ? "true" : "false";
		return painter.textWidth(label) + painter.textWidth(valStr) + 8;
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
		String valStr = setting.value() ? "true" : "false";
		int valColor = setting.value() ? ClickGuiTheme.SETTING_TRUE : ClickGuiTheme.SETTING_FALSE;
		int availableWidth = width - 8;
		int textY = y + 1;

		painter.marqueeTwoPartText(
			label,
			ClickGuiTheme.SETTING_TEXT,
			valStr,
			valColor,
			x + 4,
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
				setting.set(!setting.value());
				return true;
			}
		}
		return false;
	}
}
