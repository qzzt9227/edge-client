package io.qzz.iie.ui.component.control;

import io.qzz.iie.ui.binding.ValueBinding;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;

import java.util.Objects;

public final class TextFieldControl implements UiInputTarget {
	public static final int KEY_BACKSPACE = 259;
	public static final int KEY_DELETE = 261;

	private final ValueBinding<String> binding;
	private final int maximumLength;
	private Rect bounds = new Rect(0, 0, 0, 0);

	public TextFieldControl(ValueBinding<String> binding, int maximumLength) {
		this.binding = Objects.requireNonNull(binding, "binding");
		if (maximumLength < 0) {
			throw new IllegalArgumentException("maximumLength must be non-negative");
		}
		this.maximumLength = maximumLength;
	}

	public void layout(Rect bounds) {
		this.bounds = Objects.requireNonNull(bounds, "bounds");
	}

	public String value() {
		return binding.get();
	}

	@Override
	public Rect inputBounds() {
		return bounds;
	}

	@Override
	public InputResult handleInput(UiInputEvent event) {
		if (event instanceof UiInputEvent.PointerPressed pressed) {
			return pressed.button() == MouseButton.LEFT
				? InputResult.CONSUMED
				: InputResult.IGNORED;
		}
		if (event instanceof UiInputEvent.CharacterTyped typed) {
			String character = Character.toString(typed.codePoint());
			if (!Character.isISOControl(typed.codePoint())
				&& binding.get().length() + character.length() <= maximumLength) {
				binding.set(binding.get() + character);
			}
			return InputResult.CONSUMED;
		}
		if (event instanceof UiInputEvent.KeyPressed pressed
			&& (pressed.key() == KEY_BACKSPACE || pressed.key() == KEY_DELETE)) {
			String value = binding.get();
			if (!value.isEmpty()) {
				int end = value.offsetByCodePoints(value.length(), -1);
				binding.set(value.substring(0, end));
			}
			return InputResult.CONSUMED;
		}
		return InputResult.IGNORED;
	}

	@Override
	public boolean acceptsInput() {
		return binding.isEnabled();
	}
}
