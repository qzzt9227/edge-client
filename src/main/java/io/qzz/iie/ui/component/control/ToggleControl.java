package io.qzz.iie.ui.component.control;

import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.ui.animation.AnimatedDouble;
import io.qzz.iie.ui.binding.SettingValueBinding;
import io.qzz.iie.ui.binding.ValueBinding;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.theme.ClickGuiMotion;

import java.util.Objects;

public final class ToggleControl implements UiInputTarget {
	private final ValueBinding<Boolean> binding;
	private final AnimatedDouble animation;
	private Rect bounds = new Rect(0, 0, 0, 0);

	public ToggleControl(BooleanSetting setting) {
		this(new SettingValueBinding<>(setting));
	}

	public ToggleControl(ValueBinding<Boolean> binding) {
		this.binding = Objects.requireNonNull(binding, "binding");
		animation = new AnimatedDouble(binding.get() ? 1.0 : 0.0, ClickGuiMotion.TOGGLE);
	}

	public void layout(Rect bounds) {
		this.bounds = Objects.requireNonNull(bounds, "bounds");
	}

	public boolean value() {
		return binding.get();
	}

	public double advanceAnimation(double deltaSeconds) {
		animation.animateTo(binding.get() ? 1.0 : 0.0);
		return animation.advance(deltaSeconds);
	}

	@Override
	public Rect inputBounds() {
		return bounds;
	}

	@Override
	public InputResult handleInput(UiInputEvent event) {
		if (event instanceof UiInputEvent.PointerPressed pressed
			&& pressed.button() == MouseButton.LEFT
			&& binding.isEnabled()) {
			return binding.set(!binding.get()).accepted()
				? InputResult.CONSUMED
				: InputResult.IGNORED;
		}
		return InputResult.IGNORED;
	}

	@Override
	public boolean acceptsInput() {
		return binding.isEnabled();
	}
}
