package io.qzz.iie.ui.component.control;

import io.qzz.iie.setting.EditorSetting;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;

import java.util.Objects;

/**
 * 由编辑器注册表打开复杂设置子页的通用入口控件。
 */
public final class EditorSettingControl implements UiInputTarget {
	private final EditorSetting<?> setting;
	private final Runnable editAction;
	private Rect bounds = new Rect(0, 0, 0, 0);
	private boolean enabled = true;

	public EditorSettingControl(EditorSetting<?> setting, Runnable editAction) {
		this.setting = Objects.requireNonNull(setting, "setting");
		this.editAction = Objects.requireNonNull(editAction, "editAction");
	}

	public EditorSetting<?> setting() {
		return setting;
	}

	public void layout(Rect bounds) {
		this.bounds = Objects.requireNonNull(bounds, "bounds");
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public Rect inputBounds() {
		return bounds;
	}

	@Override
	public InputResult handleInput(UiInputEvent event) {
		if (event instanceof UiInputEvent.PointerPressed pressed
			&& pressed.button() == MouseButton.LEFT) {
			editAction.run();
			return InputResult.CONSUMED;
		}
		return InputResult.IGNORED;
	}

	@Override
	public boolean acceptsInput() {
		return enabled;
	}
}
