package io.qzz.iie.ui.input;

public sealed interface UiInputEvent {
	record PointerPressed(
		double x,
		double y,
		MouseButton button,
		int modifiers
	) implements UiInputEvent {
	}

	record PointerReleased(
		double x,
		double y,
		MouseButton button,
		int modifiers
	) implements UiInputEvent {
	}

	record PointerDragged(
		double x,
		double y,
		double deltaX,
		double deltaY,
		MouseButton button,
		int modifiers
	) implements UiInputEvent {
	}

	record PointerMoved(double x, double y) implements UiInputEvent {
	}

	record Scroll(
		double x,
		double y,
		double horizontal,
		double vertical
	) implements UiInputEvent {
	}

	record KeyPressed(int key, int scanCode, int modifiers) implements UiInputEvent {
	}

	record KeyReleased(int key, int scanCode, int modifiers) implements UiInputEvent {
	}

	record CharacterTyped(int codePoint) implements UiInputEvent {
	}
}
