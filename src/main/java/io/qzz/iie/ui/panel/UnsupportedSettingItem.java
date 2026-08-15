package io.qzz.iie.ui.panel;

import io.qzz.iie.setting.Setting;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class UnsupportedSettingItem implements InlineSettingItem {
	private final Setting<?> setting;

	public UnsupportedSettingItem(Setting<?> setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	@Override
	public Setting<?> setting() {
		return setting;
	}

	@Override
	public int height() {
		return 11;
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
		String label = io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey()) + ": [Unsupported]";
		painter.marqueeText(
			label,
			x + 4,
			y + 1,
			width - 8,
			ClickGuiTheme.MODULE_DISABLED,
			hovered,
			time
		);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		return false;
	}
}
