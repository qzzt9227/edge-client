package io.qzz.iie.module.impl.player.invertmouse;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleCategories;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.KeybindSetting;

/**
 * 反转鼠标水平视角（XZ 平面 / yaw）的纯客户端模块。
 *
 * <p>模块只声明开关与快捷键；实际视角修改由 {@link InvertMouseHooks}
 * 在 {@code MouseHandler} 的 Mixin 注入点完成。</p>
 */
public final class InvertMouseModule extends Module {
	private final KeybindSetting shortcut = keybind(
		new KeybindSetting("keybind", "client.setting.module_keybind")
	);

	public InvertMouseModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "invert_mouse"),
			"client.module.invert_mouse.name",
			"client.module.invert_mouse.description",
			ModuleCategories.PLAYER,
			100
		));
	}
}
