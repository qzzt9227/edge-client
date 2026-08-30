package io.qzz.iie.ui.panel;

import io.qzz.iie.setting.ColorSetting;
import io.qzz.iie.ui.component.control.ColorPickerControl;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClickGUI 面板行内颜色设置项，点击展开 PS 风格调色盘、色相条、纯色色块与 Hex 框。
 */
public final class ColorSettingItem implements InlineSettingItem {
	private static final int HEADER_HEIGHT = 14;
	private static final int EXPANDED_BODY_HEIGHT = 100;

	private final ColorSetting setting;
	private final ColorPickerControl picker;
	private boolean expanded;

	public ColorSettingItem(ColorSetting setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
		this.picker = new ColorPickerControl(setting);
	}

	@Override
	public ColorSetting setting() {
		return setting;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
		if (expanded) {
			picker.syncHsvFromSetting();
		}
	}

	@Override
	public int height() {
		return expanded ? (HEADER_HEIGHT + EXPANDED_BODY_HEIGHT) : HEADER_HEIGHT;
	}

	@Override
	public int preferredWidth(UiPainter painter) {
		String label = io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey());
		return painter.textWidth(label) + 60;
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
			painter.fill(x, y, width, HEADER_HEIGHT, ClickGuiTheme.ROW_HOVER);
		}

		String label = io.qzz.iie.i18n.ClientI18n.translate(setting.translationKey());
		int textY = y + (HEADER_HEIGHT - painter.lineHeight()) / 2;

		// 1. 标题文字
		int chipWidth = 14;
		int chipHeight = 8;
		int chipX = x + width - chipWidth - 4;
		int chipY = y + (HEADER_HEIGHT - chipHeight) / 2;

		int availableTextWidth = width - chipWidth - 12;
		painter.marqueeText(label, x + 4, textY, availableTextWidth, ClickGuiTheme.SETTING_TEXT, hoveredHeader, time);

		// 2. 右侧颜色预览色块
		painter.fill(chipX, chipY, chipWidth, chipHeight, setting.argb());
		painter.outline(chipX, chipY, chipWidth, chipHeight, ClickGuiTheme.OUTLINE);

		// 3. 展开后的调色盘正方形主体
		if (expanded) {
			int bodyY = y + HEADER_HEIGHT;
			painter.fill(x, bodyY, width, EXPANDED_BODY_HEIGHT, ClickGuiTheme.PANEL_BODY);

			picker.layout(new Rect(x + 2, bodyY + 2, width - 4, EXPANDED_BODY_HEIGHT - 4));
			picker.render(painter, mouseX, mouseY, 0.0f, time);
		}
	}

	@Override
	public boolean mouseClicked(
		double mouseX,
		double mouseY,
		int button,
		int x,
		int y,
		int width
	) {
		// 点击头部：展开/收起
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + HEADER_HEIGHT) {
			if (button == 0 || button == 1) {
				setExpanded(!expanded);
				return true;
			}
		}

		// 点击展开区域：路由到调色盘控件
		if (expanded) {
			int bodyY = y + HEADER_HEIGHT;
			if (mouseX >= x && mouseX <= x + width && mouseY >= bodyY && mouseY < bodyY + EXPANDED_BODY_HEIGHT) {
				picker.layout(new Rect(x + 2, bodyY + 2, width - 4, EXPANDED_BODY_HEIGHT - 4));
				picker.handleInput(new UiInputEvent.PointerPressed(
					mouseX,
					mouseY,
					MouseButton.fromCode(button),
					0
				));
				return true;
			}
		}
		return false;
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		if (expanded) {
			picker.handleInput(new UiInputEvent.PointerReleased(
				mouseX,
				mouseY,
				MouseButton.fromCode(button),
				0
			));
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
		if (expanded) {
			picker.handleInput(new UiInputEvent.PointerDragged(
				mouseX,
				mouseY,
				deltaX,
				deltaY,
				MouseButton.fromCode(button),
				0
			));
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scancode, int modifiers) {
		if (expanded) {
			return picker.handleInput(new UiInputEvent.KeyPressed(
				keyCode,
				scancode,
				modifiers
			)) != InputResult.IGNORED;
		}
		return false;
	}

	@Override
	public boolean charTyped(char chr) {
		if (expanded) {
			return picker.handleInput(new UiInputEvent.CharacterTyped(chr)) != InputResult.IGNORED;
		}
		return false;
	}
}
