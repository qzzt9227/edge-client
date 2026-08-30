package io.qzz.iie.ui.component.control;

import io.qzz.iie.setting.DoubleRangeSetting;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;

import java.util.Objects;

public final class RangeSliderControl implements UiInputTarget {
	public enum DragTarget {
		NONE,
		MIN,
		MAX
	}

	private final DoubleRangeSetting setting;
	private Rect bounds = new Rect(0, 0, 0, 0);
	private DragTarget activeTarget = DragTarget.NONE;

	public RangeSliderControl(DoubleRangeSetting setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	public void layout(Rect bounds) {
		this.bounds = Objects.requireNonNull(bounds, "bounds");
	}

	public DoubleRangeSetting setting() {
		return setting;
	}

	public double minimum() {
		return setting.minimum();
	}

	public double maximum() {
		return setting.maximum();
	}

	public double minFraction() {
		return setting.minFraction();
	}

	public double maxFraction() {
		return setting.maxFraction();
	}

	public DragTarget activeTarget() {
		return activeTarget;
	}

	@Override
	public Rect inputBounds() {
		return bounds;
	}

	@Override
	public InputResult handleInput(UiInputEvent event) {
		if (event instanceof UiInputEvent.PointerPressed pressed
			&& pressed.button() == MouseButton.LEFT) {
			double fraction = calculateFraction(pressed.x());
			double distMin = Math.abs(fraction - setting.minFraction());
			double distMax = Math.abs(fraction - setting.maxFraction());

			if (distMin < distMax || (distMin == distMax && fraction <= setting.minFraction())) {
				activeTarget = DragTarget.MIN;
				setting.setMinFraction(fraction);
			} else {
				activeTarget = DragTarget.MAX;
				setting.setMaxFraction(fraction);
			}
			return InputResult.CAPTURE_POINTER;
		}

		if (event instanceof UiInputEvent.PointerDragged dragged
			&& dragged.button() == MouseButton.LEFT) {
			double fraction = calculateFraction(dragged.x());
			if (activeTarget == DragTarget.MIN) {
				setting.setMinFraction(fraction);
			} else if (activeTarget == DragTarget.MAX) {
				setting.setMaxFraction(fraction);
			}
			return InputResult.CONSUMED;
		}

		if (event instanceof UiInputEvent.PointerReleased released
			&& released.button() == MouseButton.LEFT) {
			activeTarget = DragTarget.NONE;
			return InputResult.CONSUMED;
		}

		return InputResult.IGNORED;
	}

	private double calculateFraction(double pointerX) {
		if (bounds.width() <= 0.0) {
			return 0.0;
		}
		return Math.clamp((pointerX - bounds.x()) / bounds.width(), 0.0, 1.0);
	}
}
