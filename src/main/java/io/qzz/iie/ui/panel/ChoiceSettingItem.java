package io.qzz.iie.ui.panel;

import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class ChoiceSettingItem<T> implements InlineSettingItem {
	private static final int HEADER_HEIGHT = 12;
	private static final int OPTION_HEIGHT = 11;

	private final ChoiceSetting<T> setting;
	private boolean expanded;

	public ChoiceSettingItem(ChoiceSetting<T> setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	@Override
	public ChoiceSetting<T> setting() {
		return setting;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	@Override
	public int height() {
		if (!expanded) {
			return HEADER_HEIGHT;
		}
		return HEADER_HEIGHT + setting.options().size() * OPTION_HEIGHT;
	}

	@Override
	public int preferredWidth(UiPainter painter) {
		String label = io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey()) + ": ";
		int maxOptionWidth = 0;
		for (ChoiceOption<T> opt : setting.options()) {
			maxOptionWidth = Math.max(
				maxOptionWidth,
				painter.textWidth(io.qzz.iie.i18n.ClientI18n.translate(opt.translationKey()))
			);
		}
		return painter.textWidth(label) + maxOptionWidth + 18;
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
		boolean hoveredHeader = mouseX >= x && mouseX <= x + width
			&& mouseY >= y && mouseY < y + HEADER_HEIGHT;

		if (hoveredHeader) {
			painter.fill(x + 2, y, width - 4, HEADER_HEIGHT, ClickGuiTheme.ROW_HOVER);
		}

		String label = io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey()) + ": ";
		ChoiceOption<T> selected = setting.selectedOption();
		String modeName = io.qzz.iie.i18n.ClientI18n.translate(selected.translationKey());
		int availableWidth = width - 20;
		int textY = y + 1;

		painter.marqueeTwoPartText(
			label,
			ClickGuiTheme.SETTING_TEXT,
			modeName,
			ClickGuiTheme.SETTING_MODE,
			x + 4,
			textY,
			availableWidth,
			hoveredHeader,
			time
		);

		// Dropdown indicator
		String indicator = expanded ? "-" : "+";
		painter.text(indicator, x + width - 10, textY, ClickGuiTheme.TEXT_SECONDARY);

		// Sub-options list
		if (expanded) {
			List<ChoiceOption<T>> options = setting.options();
			int optY = y + HEADER_HEIGHT;

			for (int i = 0; i < options.size(); i++) {
				ChoiceOption<T> option = options.get(i);
				boolean isSelected = option.equals(selected);
				boolean hoveredOpt = mouseX >= x + 4 && mouseX <= x + width - 4
					&& mouseY >= optY && mouseY < optY + OPTION_HEIGHT;

				if (hoveredOpt) {
					painter.fill(x + 6, optY, width - 12, OPTION_HEIGHT, ClickGuiTheme.ROW_HOVER);
				}

				String optName = io.qzz.iie.i18n.ClientI18n.translate(option.translationKey());
				String prefix = isSelected ? "> " : "  ";
				int optColor;
				if (isSelected) {
					optColor = ClickGuiTheme.SETTING_MODE;
				} else if (hoveredOpt) {
					optColor = ClickGuiTheme.TEXT_PRIMARY;
				} else {
					optColor = ClickGuiTheme.TEXT_SECONDARY;
				}

				painter.marqueeText(
					prefix + optName,
					x + 8,
					optY + 1,
					width - 16,
					optColor,
					hoveredOpt,
					time
				);

				optY += OPTION_HEIGHT;
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		// Header click -> toggle expanded
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + HEADER_HEIGHT) {
			if (button == 0) {
				if (!expanded && setting.id().equals("custom_font")) {
					try {
						@SuppressWarnings("unchecked")
						ChoiceSetting<String> fontSetting = (ChoiceSetting<String>) setting;
						fontSetting.updateOptions(io.qzz.iie.font.ClientFontManager.getAvailableFontOptions());
					} catch (Throwable ignored) {
					}
				}
				expanded = !expanded;
				return true;
			}
			return false;
		}

		if (!expanded) {
			return false;
		}

		// Options click (left click selects, right click selects and closes)
		if (button == 0 || button == 1) {
			List<ChoiceOption<T>> options = setting.options();
			int optY = y + HEADER_HEIGHT;

			for (int i = 0; i < options.size(); i++) {
				if (mouseX >= x + 4 && mouseX <= x + width - 4
					&& mouseY >= optY && mouseY < optY + OPTION_HEIGHT) {
					setting.selectOption(i);
					if (button == 1) {
						expanded = false;
					}
					return true;
				}
				optY += OPTION_HEIGHT;
			}
		}

		return false;
	}
}
