package io.qzz.iie.module.impl.input.specialflip;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.KeybindSetting;

/**
 * 特殊翻转鼠标视角：把鼠标水平/垂直位移映射旋转 90°。
 *
 * <p>开启后左移鼠标视角上移、右移视角下移、上移视角右移、下移视角左移。
 * 模块只声明开关与快捷键；实际映射由 {@link SpecialFlipHooks} 在
 * {@code MouseHandler} 的 Mixin 注入点完成。</p>
 */
public final class SpecialFlipModule extends Module {
	private final KeybindSetting shortcut = keybind(
		new KeybindSetting("keybind", "client.setting.module_keybind")
	);

	public SpecialFlipModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "special_flip"),
			"client.module.special_flip.name",
			"client.module.special_flip.description",
			100
		));
	}
}
