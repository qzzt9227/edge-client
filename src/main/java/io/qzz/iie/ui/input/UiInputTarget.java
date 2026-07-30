package io.qzz.iie.ui.input;

import io.qzz.iie.ui.layout.Rect;

public interface UiInputTarget {
	Rect inputBounds();

	InputResult handleInput(UiInputEvent event);

	default boolean acceptsInput() {
		return true;
	}

	default void onFocusChanged(boolean focused) {
	}
}
