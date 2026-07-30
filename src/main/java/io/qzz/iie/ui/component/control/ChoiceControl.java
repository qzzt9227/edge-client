package io.qzz.iie.ui.component.control;

import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.ui.animation.AnimatedDouble;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.theme.ClickGuiMotion;

import java.util.Objects;

public final class ChoiceControl implements UiInputTarget {
	public static final int KEY_ESCAPE = 256;

	private final ChoiceSetting<?> setting;
	private final AnimatedDouble drawerAnimation =
		new AnimatedDouble(0.0, ClickGuiMotion.CHOICE_DRAWER);
	private Rect bounds = new Rect(0, 0, 0, 0);
	private int optionHeight;
	private boolean expanded;
	private int hoveredOptionIndex = -1;

	public ChoiceControl(ChoiceSetting<?> setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	public void layout(Rect bounds, int optionHeight) {
		this.bounds = Objects.requireNonNull(bounds, "bounds");
		if (optionHeight <= 0) {
			throw new IllegalArgumentException("optionHeight must be positive");
		}
		this.optionHeight = optionHeight;
		hoveredOptionIndex = -1;
	}

	public ChoiceSetting<?> setting() {
		return setting;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public int hoveredOptionIndex() {
		return hoveredOptionIndex;
	}

	public double advanceAnimation(double deltaSeconds) {
		drawerAnimation.animateTo(expanded ? 1.0 : 0.0);
		return drawerAnimation.advance(deltaSeconds);
	}

	public double drawerProgress() {
		return drawerAnimation.value();
	}

	public Rect collapsedBounds() {
		return bounds;
	}

	public Rect optionBounds(int index) {
		if (index < 0 || index >= setting.options().size()) {
			throw new IndexOutOfBoundsException(index);
		}
		return new Rect(
			bounds.x(),
			bounds.bottom() + index * optionHeight,
			bounds.width(),
			optionHeight
		);
	}

	public Rect drawerBounds() {
		return new Rect(
			bounds.x(),
			bounds.bottom(),
			bounds.width(),
			(double) optionHeight * setting.options().size()
		);
	}

	/**
	 * 判断指针是否位于展开抽屉的可交互区域。
	 * 页面可以用它阻止抽屉下方控件显示悬停效果。
	 */
	public boolean coversDrawer(double pointerX, double pointerY) {
		return (expanded || drawerAnimation.value() > 0.0)
			&& drawerBounds().contains(pointerX, pointerY);
	}

	@Override
	public Rect inputBounds() {
		return expanded
			? new Rect(
				bounds.x(),
				bounds.y(),
				bounds.width(),
				bounds.height() + (double) optionHeight * setting.options().size()
			)
			: bounds;
	}

	@Override
	public InputResult handleInput(UiInputEvent event) {
		if (event instanceof UiInputEvent.PointerMoved moved) {
			updateHoveredOption(moved.x(), moved.y());
			return expanded && inputBounds().contains(moved.x(), moved.y())
				? InputResult.CONSUMED
				: InputResult.IGNORED;
		}
		if (event instanceof UiInputEvent.KeyPressed pressed
			&& pressed.key() == KEY_ESCAPE
			&& expanded) {
			expanded = false;
			hoveredOptionIndex = -1;
			return InputResult.CONSUMED;
		}
		if (!(event instanceof UiInputEvent.PointerPressed pressed)
			|| pressed.button() != MouseButton.LEFT) {
			return InputResult.IGNORED;
		}
		if (bounds.contains(pressed.x(), pressed.y())) {
			expanded = !expanded;
			hoveredOptionIndex = -1;
			return InputResult.CONSUMED;
		}
		if (!expanded || !inputBounds().contains(pressed.x(), pressed.y())) {
			return InputResult.IGNORED;
		}

		int index = (int) ((pressed.y() - bounds.bottom()) / optionHeight);
		setting.selectOption(index);
		expanded = false;
		hoveredOptionIndex = -1;
		return InputResult.CONSUMED;
	}

	@Override
	public void onFocusChanged(boolean focused) {
		if (!focused) {
			expanded = false;
			hoveredOptionIndex = -1;
		}
	}

	private void updateHoveredOption(double mouseX, double mouseY) {
		hoveredOptionIndex = -1;
		if (!expanded || !drawerBounds().contains(mouseX, mouseY)) {
			return;
		}
		int index = (int) ((mouseY - bounds.bottom()) / optionHeight);
		if (index >= 0 && index < setting.options().size()) {
			hoveredOptionIndex = index;
		}
	}
}
