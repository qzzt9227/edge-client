package io.qzz.iie.ui.component.control;

import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.ui.binding.BindingUpdateResult;
import io.qzz.iie.ui.binding.RangedDoubleBinding;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;

import java.util.Objects;

public final class SliderControl implements UiInputTarget {
	private final RangedDoubleBinding binding;
	private Rect bounds = new Rect(0, 0, 0, 0);

	public SliderControl(DoubleSetting setting) {
		Objects.requireNonNull(setting, "setting");
		this.binding = new RangedDoubleBinding() {
			@Override
			public Double get() {
				return setting.value();
			}

			@Override
			public BindingUpdateResult set(Double value) {
				setting.set(value);
				return new BindingUpdateResult.Accepted();
			}

			@Override
			public double minimum() {
				return setting.minimum();
			}

			@Override
			public double maximum() {
				return setting.maximum();
			}

			@Override
			public double step() {
				return setting.step();
			}
		};
	}

	public SliderControl(RangedDoubleBinding binding) {
		this.binding = Objects.requireNonNull(binding, "binding");
	}

	public void layout(Rect bounds) {
		this.bounds = Objects.requireNonNull(bounds, "bounds");
	}

	public double value() {
		return binding.get();
	}

	public double fraction() {
		return binding.fraction();
	}

	public double step() {
		return binding.step();
	}

	@Override
	public Rect inputBounds() {
		return bounds;
	}

	@Override
	public InputResult handleInput(UiInputEvent event) {
		if (event instanceof UiInputEvent.PointerPressed pressed
			&& pressed.button() == MouseButton.LEFT) {
			update(pressed.x());
			return InputResult.CAPTURE_POINTER;
		}
		if (event instanceof UiInputEvent.PointerDragged dragged
			&& dragged.button() == MouseButton.LEFT) {
			update(dragged.x());
			return InputResult.CONSUMED;
		}
		if (event instanceof UiInputEvent.PointerReleased released
			&& released.button() == MouseButton.LEFT) {
			return InputResult.CONSUMED;
		}
		return InputResult.IGNORED;
	}

	private void update(double pointerX) {
		if (bounds.width() <= 0.0) {
			binding.setFraction(0.0);
			return;
		}
		binding.setFraction((pointerX - bounds.x()) / bounds.width());
	}
}
