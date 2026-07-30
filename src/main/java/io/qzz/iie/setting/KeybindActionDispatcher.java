package io.qzz.iie.setting;

import java.util.Objects;
import java.util.function.IntPredicate;

/**
 * 将一个可配置快捷键的物理按下边沿转换为一次动作触发。
 *
 * <p>调用方负责决定动作当前是否可用；即使动作被禁用，本调度器仍会记录按键状态，
 * 避免玩家在关闭 GUI 后因仍按住同一按键而意外触发。</p>
 */
public final class KeybindActionDispatcher {
	private final KeybindSetting setting;
	private boolean pressed;

	public KeybindActionDispatcher(KeybindSetting setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	/**
	 * 采样当前快捷键，并在允许状态下仅对新的按下边沿返回 {@code true}。
	 */
	public boolean update(boolean actionActive, IntPredicate isKeyDown) {
		Objects.requireNonNull(isKeyDown, "isKeyDown");
		KeybindValue keybind = setting.value();
		boolean currentlyPressed =
			keybind.isBound() && isKeyDown.test(keybind.keyCode());
		boolean triggered = actionActive && currentlyPressed && !pressed;
		pressed = currentlyPressed;
		return triggered;
	}
}
