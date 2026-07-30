package io.qzz.iie.ui.component.control;

import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;

import java.util.Objects;

/**
 * 打开通用 HUD 定位编辑器的声明式设置控件。
 */
public final class HudPositionControl implements UiInputTarget {
	private final HudPositionSetting setting;
	private final Runnable editAction;
	private Rect bounds = new Rect(0, 0, 0, 0);

	public HudPositionControl(HudPositionSetting setting, Runnable editAction) {
		this.setting = Objects.requireNonNull(setting, "setting");
		this.editAction = Objects.requireNonNull(editAction, "editAction");
	}

	public HudPositionSetting setting() {
		return setting;
	}

	public void layout(Rect bounds) {
		this.bounds = Objects.requireNonNull(bounds, "bounds");
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
}
