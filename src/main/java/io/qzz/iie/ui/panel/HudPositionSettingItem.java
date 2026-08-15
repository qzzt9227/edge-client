package io.qzz.iie.ui.panel;

import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class HudPositionSettingItem implements InlineSettingItem {
	private final HudPositionSetting setting;
	private final Consumer<HudPositionSetting> editorOpener;

	public HudPositionSettingItem(
		HudPositionSetting setting,
		Consumer<HudPositionSetting> editorOpener
	) {
		this.setting = Objects.requireNonNull(setting, "setting");
		this.editorOpener = Objects.requireNonNull(editorOpener, "editorOpener");
	}

	@Override
	public HudPositionSetting setting() {
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
		int availableWidth = width - 8;
		int textY = y + 1;

		painter.marqueeTwoPartText(
			label,
			ClickGuiTheme.SETTING_TEXT,
			action,
			ClickGuiTheme.SETTING_ACTION,
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
				editorOpener.accept(setting);
				return true;
			}
		}
		return false;
	}
}
