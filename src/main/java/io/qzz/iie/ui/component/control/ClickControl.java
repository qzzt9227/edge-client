package io.qzz.iie.ui.component.control;

import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class ClickControl implements UiInputTarget {
	private final Map<MouseButton, Runnable> actions = new EnumMap<>(MouseButton.class);
	private Rect bounds = new Rect(0, 0, 0, 0);
	private boolean enabled = true;

	public ClickControl on(MouseButton button, Runnable action) {
		actions.put(Objects.requireNonNull(button, "button"), Objects.requireNonNull(action, "action"));
		return this;
	}

	public void layout(Rect bounds) {
		this.bounds = Objects.requireNonNull(bounds, "bounds");
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public Rect inputBounds() {
		return bounds;
	}

	@Override
	public InputResult handleInput(UiInputEvent event) {
		if (event instanceof UiInputEvent.PointerPressed pressed) {
			Runnable action = actions.get(pressed.button());
			if (action != null) {
				action.run();
				return InputResult.CONSUMED;
			}
		}
		return InputResult.IGNORED;
	}

	@Override
	public boolean acceptsInput() {
		return enabled;
	}
}
