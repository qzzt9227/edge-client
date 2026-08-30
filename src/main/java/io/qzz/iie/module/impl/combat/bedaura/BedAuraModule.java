package io.qzz.iie.module.impl.combat.bedaura;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;

public final class BedAuraModule extends Module {
	private final BedAuraController controller = new BedAuraController();

	private final DoubleSetting range = setting(new DoubleSetting(
		"range",
		"client.setting.bed_aura.range",
		4.5,
		1.0,
		6.0,
		0.1
	));

	private final DoubleSetting placeInterval = setting(new DoubleSetting(
		"place_interval",
		"client.setting.bed_aura.place_interval",
		2.0,
		0.0,
		20.0,
		1.0
	));

	private final DoubleSetting breakInterval = setting(new DoubleSetting(
		"break_interval",
		"client.setting.bed_aura.break_interval",
		2.0,
		0.0,
		20.0,
		1.0
	));

	private final BooleanSetting onlyNether = setting(new BooleanSetting(
		"only_nether",
		"client.setting.bed_aura.only_nether",
		true
	));

	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public BedAuraModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "bed_aura"),
			"client.module.bed_aura.name",
			"client.module.bed_aura.description",
			110
		));
	}

	public DoubleSetting range() {
		return range;
	}

	public DoubleSetting placeInterval() {
		return placeInterval;
	}

	public DoubleSetting breakInterval() {
		return breakInterval;
	}

	public BooleanSetting onlyNether() {
		return onlyNether;
	}

	@Override
	protected void onClientTick() {
		controller.tick(this);
	}

	@Override
	protected void onDisable() {
		controller.reset();
	}
}
