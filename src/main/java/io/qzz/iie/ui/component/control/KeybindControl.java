package io.qzz.iie.ui.component.control;

import io.qzz.iie.setting.KeybindSetting;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;

import java.util.Objects;

public final class KeybindControl implements UiInputTarget {
	public static final int KEY_ESCAPE = 256;
	public static final int KEY_BACKSPACE = 259;

	private final KeybindSetting setting;
	private Rect bounds = new Rect(0, 0, 0, 0);
	private boolean listening;

	public KeybindControl(KeybindSetting setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	public void layout(Rect bounds) {
		this.bounds = Objects.requireNonNull(bounds, "bounds");
	}

	public KeybindSetting setting() {
		return setting;
	}

	public boolean isListening() {
		return listening;
	}

	@Override
	public Rect inputBounds() {
		return bounds;
	}

	@Override
	public InputResult handleInput(UiInputEvent event) {
		if (event instanceof UiInputEvent.PointerPressed pressed
			&& pressed.button() == MouseButton.LEFT) {
			listening = true;
			return InputResult.CONSUMED;
		}
		if (!(event instanceof UiInputEvent.KeyPressed pressed) || !listening) {
			return InputResult.IGNORED;
		}
		if (pressed.key() == KEY_ESCAPE) {
			listening = false;
			return InputResult.CONSUMED;
		}
		if (pressed.key() == KEY_BACKSPACE) {
			setting.clear();
			listening = false;
			return InputResult.CONSUMED;
		}
		if (pressed.key() < 0) {
			return InputResult.CONSUMED;
		}

		setting.bind(pressed.key());
		listening = false;
		return InputResult.CONSUMED;
	}

	@Override
	public void onFocusChanged(boolean focused) {
		if (!focused) {
			listening = false;
		}
	}
}
