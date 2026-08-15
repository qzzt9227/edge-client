package io.qzz.iie.ui.panel;

import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class DoubleSettingItem implements InlineSettingItem {
	private final DoubleSetting setting;
	private boolean dragging;

	public DoubleSettingItem(DoubleSetting setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	@Override
	public DoubleSetting setting() {
		return setting;
	}

	@Override
	public int height() {
		return 20;
	}

	@Override
	public int preferredWidth(UiPainter painter) {
		String label = io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey()) + ": ";
		String valStr = formatValue(setting.value(), setting.step());
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
		String valStr = formatValue(setting.value(), setting.step());
		int availableWidth = width - 8;
		int textY = y + 1;

		painter.marqueeTwoPartText(
			label,
			ClickGuiTheme.SETTING_TEXT,
			valStr,
			ClickGuiTheme.SLIDER_FILL,
			x + 4,
			textY,
			availableWidth,
			hovered,
			time
		);

		// Slider track and filled bar (moved down by 5px to avoid text overlap)
		int trackX = x + 4;
		int trackY = y + 15;
		int trackWidth = Math.max(4, width - 8);
		int trackHeight = 3;

		painter.fill(trackX, trackY, trackWidth, trackHeight, ClickGuiTheme.SLIDER_TRACK);

		double min = setting.minimum();
		double max = setting.maximum();
		double range = max - min;
		double fraction = range <= 0.0 ? 0.0 : Math.clamp((setting.value() - min) / range, 0.0, 1.0);
		int filledWidth = (int) Math.round(trackWidth * fraction);

		if (filledWidth > 0) {
			painter.fill(trackX, trackY, filledWidth, trackHeight, ClickGuiTheme.SLIDER_FILL);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height()) {
			if (button == 0) {
				dragging = true;
				updateValueFromMouse(mouseX, x, width);
				return true;
			} else if (button == 1) {
				// Right click: step up
				double next = setting.value() + setting.step();
				if (next > setting.maximum()) {
					next = setting.minimum();
				}
				setting.set(next);
				return true;
			}
		}
		return false;
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			dragging = false;
		}
	}

	@Override
	public void mouseDragged(
		double mouseX,
		double mouseY,
		double deltaX,
		double deltaY,
		int button,
		int x,
		int y,
		int width
	) {
		if (dragging) {
			updateValueFromMouse(mouseX, x, width);
		}
	}

	private void updateValueFromMouse(double mouseX, int x, int width) {
		int trackX = x + 4;
		int trackWidth = Math.max(1, width - 8);
		double ratio = Math.clamp((mouseX - trackX) / (double) trackWidth, 0.0, 1.0);
		double range = setting.maximum() - setting.minimum();
		double raw = setting.minimum() + ratio * range;
		setting.set(raw);
	}

	private static String formatValue(double value, double step) {
		if (step >= 1.0) {
			return Long.toString(Math.round(value));
		}
		if (step >= 0.1) {
			return String.format(Locale.ROOT, "%.1f", value);
		}
		return String.format(Locale.ROOT, "%.2f", value);
	}
}
