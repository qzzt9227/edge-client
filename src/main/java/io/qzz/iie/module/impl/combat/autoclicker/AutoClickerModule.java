package io.qzz.iie.module.impl.combat.autoclicker;

import io.qzz.iie.mixin.MinecraftAccessor;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.DoubleRangeSetting;
import io.qzz.iie.setting.KeybindSetting;
import net.minecraft.client.Minecraft;

public final class AutoClickerModule extends Module {
	private final AutoClickerController controller = new AutoClickerController();

	private final BooleanSetting leftClick = setting(new BooleanSetting(
		"left_click",
		"client.setting.auto_clicker.left_click",
		true
	));
	private final DoubleRangeSetting leftCps = setting(new DoubleRangeSetting(
		"left_cps",
		"client.setting.auto_clicker.left_cps",
		8.0,
		14.0,
		1.0,
		100.0,
		1.0
	));
	private final BooleanSetting rightClick = setting(new BooleanSetting(
		"right_click",
		"client.setting.auto_clicker.right_click",
		false
	));
	private final DoubleRangeSetting rightCps = setting(new DoubleRangeSetting(
		"right_cps",
		"client.setting.auto_clicker.right_cps",
		10.0,
		20.0,
		1.0,
		100.0,
		1.0
	));
	private final BooleanSetting holdOnly = setting(new BooleanSetting(
		"hold_only",
		"client.setting.auto_clicker.hold_only",
		true
	));
	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public AutoClickerModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "auto_clicker"),
			"client.module.auto_clicker.name",
			"client.module.auto_clicker.description",
			150
		));
	}

	public BooleanSetting leftClick() {
		return leftClick;
	}

	public DoubleRangeSetting leftCps() {
		return leftCps;
	}

	public BooleanSetting rightClick() {
		return rightClick;
	}

	public DoubleRangeSetting rightCps() {
		return rightCps;
	}

	public BooleanSetting holdOnly() {
		return holdOnly;
	}

	public AutoClickerController controller() {
		return controller;
	}

	@Override
	protected void onClientTick() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.gui.screen() != null) {
			return;
		}

		long now = System.currentTimeMillis();
		if (controller.checkAndScheduleLeft(
			now,
			leftClick.value(),
			client.options.keyAttack.isDown(),
			holdOnly.value(),
			leftCps.value()
		)) {
			((MinecraftAccessor) client).invokeStartAttack();
			io.qzz.iie.ui.hud.CpsTracker.recordLeftClick();
		}

		if (controller.checkAndScheduleRight(
			now,
			rightClick.value(),
			client.options.keyUse.isDown(),
			holdOnly.value(),
			rightCps.value()
		)) {
			((MinecraftAccessor) client).setRightClickDelay(0);
			((MinecraftAccessor) client).invokeStartUseItem();
			io.qzz.iie.ui.hud.CpsTracker.recordRightClick();
		}
	}

	@Override
	protected void onDisable() {
		controller.reset();
	}
}
