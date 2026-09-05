package io.qzz.iie.ui.panel;

import io.qzz.iie.setting.FoldSetting;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可折叠分组设置的内联 UI 渲染项。
 *
 * <p>展示带折叠箭头指示器的分组标题栏，点击即可切换展开/折叠。</p>
 */
public final class FoldSettingItem implements InlineSettingItem {
	private static final int ITEM_HEIGHT = 12;

	private final FoldSetting setting;

	public FoldSettingItem(FoldSetting setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	@Override
	public FoldSetting setting() {
		return setting;
	}

	@Override
	public int height() {
		return ITEM_HEIGHT;
	}

	@Override
	public int preferredWidth(UiPainter painter) {
		String label = (setting.value() ? "▼ " : "▶ ")
			+ io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey());
		return painter.textWidth(label) + 8 + indent() * 6;
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

		if (hovered) {
			painter.fill(x, y, width, height(), ClickGuiTheme.MODULE_HOVER);
		}

		int indentOffset = indent() * 6;
		if (indentOffset > 0) {
			painter.fill(x + 2 + indentOffset - 3, y + 2, 1, height() - 4, ClickGuiTheme.PANEL_BORDER);
		}

		String arrow = setting.value() ? "▼ " : "▶ ";
		String text = arrow + io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey());
		int textX = x + 4 + indentOffset;
		int textY = y + 1;
		int availableWidth = width - 8 - indentOffset;

		int textColor = hovered ? ClickGuiTheme.TEXT_PRIMARY : ClickGuiTheme.TEXT_SECONDARY;
		painter.marqueeText(text, textX, textY, availableWidth, textColor, hovered, time);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height()) {
			if (button == 0 || button == 1) {
				setting.set(!setting.value());
				return true;
			}
		}
		return false;
	}
}
