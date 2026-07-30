package io.qzz.iie.ui.input;

import java.util.List;
import java.util.Objects;

public final class UiInputRouter {
	private UiInputTarget pointerCapture;
	private MouseButton pointerCaptureButton;
	private UiInputTarget focused;

	public boolean route(UiInputEvent event, List<? extends UiInputTarget> targets) {
		Objects.requireNonNull(event, "event");
		Objects.requireNonNull(targets, "targets");

		if (pointerCapture != null && isCapturedPointerEvent(event)) {
			InputResult result = pointerCapture.handleInput(event);
			if (event instanceof UiInputEvent.PointerReleased released
				&& released.button() == pointerCaptureButton) {
				pointerCapture = null;
				pointerCaptureButton = null;
			}
			return result != InputResult.IGNORED;
		}

		if (event instanceof UiInputEvent.PointerPressed pressed) {
			return routeAt(pressed.x(), pressed.y(), event, targets, true);
		}
		if (event instanceof UiInputEvent.Scroll scroll) {
			return routeAt(scroll.x(), scroll.y(), event, targets, false);
		}
		if (event instanceof UiInputEvent.PointerMoved moved) {
			if (focused != null && focused.acceptsInput()) {
				InputResult focusedResult = focused.handleInput(event);
				if (focusedResult != InputResult.IGNORED) {
					return true;
				}
			}
			return routeAt(moved.x(), moved.y(), event, targets, false);
		}
		if (focused != null && focused.acceptsInput()) {
			return focused.handleInput(event) != InputResult.IGNORED;
		}
		return false;
	}

	public boolean hasPointerCapture() {
		return pointerCapture != null;
	}

	public boolean isFocused(UiInputTarget target) {
		return focused == target;
	}

	/**
	 * 在动态重建控件列表后恢复仍然有效的键盘焦点。
	 */
	public void focus(UiInputTarget target) {
		if (target != null && !target.acceptsInput()) {
			throw new IllegalArgumentException("Cannot focus a disabled input target");
		}
		setFocused(target);
	}

	public void clear() {
		pointerCapture = null;
		pointerCaptureButton = null;
		setFocused(null);
	}

	private boolean routeAt(
		double x,
		double y,
		UiInputEvent event,
		List<? extends UiInputTarget> targets,
		boolean updateFocus
	) {
		if (updateFocus
			&& focused != null
			&& focused.acceptsInput()
			&& focused.inputBounds().contains(x, y)) {
			InputResult focusedResult = focused.handleInput(event);
			if (focusedResult != InputResult.IGNORED) {
				return true;
			}
		}

		for (int index = targets.size() - 1; index >= 0; index--) {
			UiInputTarget target = targets.get(index);
			if (target == focused) {
				continue;
			}
			if (!target.acceptsInput() || !target.inputBounds().contains(x, y)) {
				continue;
			}

			InputResult result = target.handleInput(event);
			if (result == InputResult.IGNORED) {
				continue;
			}
			if (updateFocus) {
				setFocused(target);
			}
			if (result == InputResult.CAPTURE_POINTER) {
				if (!(event instanceof UiInputEvent.PointerPressed pressed)) {
					throw new IllegalStateException(
						"Pointer capture can only begin from a pointer press"
					);
				}
				pointerCapture = target;
				pointerCaptureButton = pressed.button();
			}
			return true;
		}
		if (updateFocus) {
			setFocused(null);
		}
		return false;
	}

	private void setFocused(UiInputTarget requestedFocus) {
		if (focused == requestedFocus) {
			return;
		}
		if (focused != null) {
			focused.onFocusChanged(false);
		}
		focused = requestedFocus;
		if (focused != null) {
			focused.onFocusChanged(true);
		}
	}

	private static boolean isCapturedPointerEvent(UiInputEvent event) {
		return event instanceof UiInputEvent.PointerDragged
			|| event instanceof UiInputEvent.PointerReleased
			|| event instanceof UiInputEvent.PointerMoved;
	}
}
