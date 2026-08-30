package io.qzz.iie.ui.panel;

import io.qzz.iie.setting.DoubleRangeSetting;
import io.qzz.iie.ui.component.control.RangeSliderControl.DragTarget;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class DoubleRangeSettingItem implements InlineSettingItem {
	private final DoubleRangeSetting setting;
	private DragTarget draggingTarget = DragTarget.NONE;

	public DoubleRangeSettingItem(DoubleRangeSetting setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	@Override
	public DoubleRangeSetting setting() {
		return setting;
	}

	@Override
	public int height() {
		return 20;
	}

	@Override
	public int preferredWidth(UiPainter painter) {
		String label = io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey()) + ": ";
		String valStr = formatRange(setting.minimum(), setting.maximum(), setting.step());
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
		String valStr = formatRange(setting.minimum(), setting.maximum(), setting.step());
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

		int trackX = x + 4;
		int trackY = y + 15;
		int trackWidth = Math.max(4, width - 8);
		int trackHeight = 3;

		// 轨道底色
		painter.fill(trackX, trackY, trackWidth, trackHeight, ClickGuiTheme.SLIDER_TRACK);

		double minFrac = setting.minFraction();
		double maxFrac = setting.maxFraction();

		int startX = trackX + (int) Math.round(trackWidth * minFrac);
		int endX = trackX + (int) Math.round(trackWidth * maxFrac);
		int fillWidth = Math.max(2, endX - startX);

		// 区间填充条
		painter.fill(startX, trackY, fillWidth, trackHeight, ClickGuiTheme.SLIDER_FILL);

		// 绘制起止手柄微指示块（上下微凸，突出手柄位置）
		painter.fill(Math.max(trackX, startX - 1), trackY - 1, 2, trackHeight + 2, ClickGuiTheme.SETTING_TEXT);
		painter.fill(Math.min(trackX + trackWidth - 2, endX), trackY - 1, 2, trackHeight + 2, ClickGuiTheme.SETTING_TEXT);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height()) {
			if (button == 0) {
				int trackX = x + 4;
				int trackWidth = Math.max(1, width - 8);
				double ratio = Math.clamp((mouseX - trackX) / (double) trackWidth, 0.0, 1.0);

				double distMin = Math.abs(ratio - setting.minFraction());
				double distMax = Math.abs(ratio - setting.maxFraction());

				if (distMin < distMax || (distMin == distMax && ratio <= setting.minFraction())) {
					draggingTarget = DragTarget.MIN;
					setting.setMinFraction(ratio);
				} else {
					draggingTarget = DragTarget.MAX;
					setting.setMaxFraction(ratio);
				}
				return true;
			} else if (button == 1) {
				// 右键：将范围上限微调步进一步
				double nextMax = setting.maximum() + setting.step();
				if (nextMax > setting.rangeMaximum()) {
					nextMax = setting.minimum();
				}
				setting.setMax(nextMax);
				return true;
			}
		}
		return false;
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			draggingTarget = DragTarget.NONE;
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
		if (draggingTarget != DragTarget.NONE) {
			int trackX = x + 4;
			int trackWidth = Math.max(1, width - 8);
			double ratio = Math.clamp((mouseX - trackX) / (double) trackWidth, 0.0, 1.0);

			if (draggingTarget == DragTarget.MIN) {
				setting.setMinFraction(ratio);
			} else if (draggingTarget == DragTarget.MAX) {
				setting.setMaxFraction(ratio);
			}
		}
	}

	private static String formatRange(double min, double max, double step) {
		return formatNumber(min, step) + " - " + formatNumber(max, step);
	}

	private static String formatNumber(double value, double step) {
		if (step >= 1.0) {
			return Long.toString(Math.round(value));
		}
		if (step >= 0.1) {
			return String.format(Locale.ROOT, "%.1f", value);
		}
		return String.format(Locale.ROOT, "%.2f", value);
	}
}
