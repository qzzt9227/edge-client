package io.qzz.iie.module.impl.render.freelook;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.KeybindSetting;
import io.qzz.iie.setting.KeybindValue;

/**
 * 自由视角渲染模块：允许玩家 360 度自由旋转相机视角而不改变角色身体与移动朝向。
 */
public final class FreeLookModule extends Module {
	private final FreeLookPolicy policy = new FreeLookPolicy();

	private final BooleanSetting autoThirdPerson = setting(new BooleanSetting(
		"auto_third_person",
		"client.setting.free_look.auto_third_person",
		true
	));

	private final BooleanSetting holdMode = setting(new BooleanSetting(
		"hold_mode",
		"client.setting.free_look.hold_mode",
		false
	));

	private final BooleanSetting smoothTransition = setting(new BooleanSetting(
		"smooth_transition",
		"client.setting.free_look.smooth_transition",
		true
	));

	private final BooleanSetting invertPitch = setting(new BooleanSetting(
		"invert_pitch",
		"client.setting.free_look.invert_pitch",
		false
	));

	private final BooleanSetting invertYaw = setting(new BooleanSetting(
		"invert_yaw",
		"client.setting.free_look.invert_yaw",
		false
	));

	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public FreeLookModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "free_look"),
			"client.module.free_look.name",
			"client.module.free_look.description",
			240
		));
	}

	public FreeLookPolicy policy() {
		return policy;
	}

	public BooleanSetting autoThirdPerson() {
		return autoThirdPerson;
	}

	public BooleanSetting holdMode() {
		return holdMode;
	}

	public BooleanSetting smoothTransition() {
		return smoothTransition;
	}

	public BooleanSetting invertPitch() {
		return invertPitch;
	}

	public BooleanSetting invertYaw() {
		return invertYaw;
	}

	public void disableModule() {
		requestDisable();
	}

	@Override
	protected void onEnable() {
		FreeLookHooks.onModuleEnabled(this);
	}

	@Override
	protected void onDisable() {
		FreeLookHooks.onModuleDisabled(this);
	}

	@Override
	protected void onClientTick() {
		FreeLookHooks.onClientTick(this);
	}
}
