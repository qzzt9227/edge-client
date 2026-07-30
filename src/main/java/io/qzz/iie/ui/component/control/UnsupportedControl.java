package io.qzz.iie.ui.component.control;

import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;

import java.util.Objects;

/**
 * Explicit, inert fallback for setting types without a registered GUI control.
 */
public final class UnsupportedControl implements UiInputTarget {
	private Rect bounds = new Rect(0, 0, 0, 0);

	public void layout(Rect bounds) {
		this.bounds = Objects.requireNonNull(bounds, "bounds");
	}

	@Override
	public Rect inputBounds() {
		return bounds;
	}

	@Override
	public InputResult handleInput(UiInputEvent event) {
		return InputResult.IGNORED;
	}

	@Override
	public boolean acceptsInput() {
		return false;
	}
}
