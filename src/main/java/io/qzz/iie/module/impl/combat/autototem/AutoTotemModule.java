package io.qzz.iie.module.impl.combat.autototem;

import io.qzz.iie.api.message.MessageBoxApi;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.module.impl.combat.autototem.AutoTotemTypes.OffhandMode;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;

import java.util.List;
import java.util.Objects;

public final class AutoTotemModule extends Module {
	private final AutoTotemController controller;

	private final DoubleSetting healthThreshold = setting(new DoubleSetting(
		"health_threshold",
		"client.setting.auto_totem.health_threshold",
		10.0,
		1.0,
		20.0,
		0.5
	));

	private final DoubleSetting fallDamageThreshold = setting(new DoubleSetting(
		"fall_damage_threshold",
		"client.setting.auto_totem.fall_damage_threshold",
		10.0,
		0.0,
		40.0,
		0.5
	));

	private final DoubleSetting delay = setting(new DoubleSetting(
		"delay",
		"client.setting.auto_totem.delay",
		0.0,
		0.0,
		20.0,
		1.0
	));

	private final DoubleSetting randomDelay = setting(new DoubleSetting(
		"random_delay",
		"client.setting.auto_totem.random_delay",
		0.0,
		0.0,
		10.0,
		1.0
	));

	private final BooleanSetting checkEffects = setting(new BooleanSetting(
		"check_effects",
		"client.setting.auto_totem.check_effects",
		true
	));

	private final BooleanSetting onlyOnLowHealth = setting(new BooleanSetting(
		"only_on_low_health",
		"client.setting.auto_totem.only_on_low_health",
		false
	));

	private final ChoiceSetting<OffhandMode> offhandMode = setting(new ChoiceSetting<>(
		"offhand_mode",
		"client.setting.auto_totem.offhand_mode",
		OffhandMode.SWAP,
		List.of(
			option("swap", OffhandMode.SWAP),
			option("drop", OffhandMode.DROP),
			option("restore", OffhandMode.RESTORE)
		)
	));

	private final BooleanSetting alerts = setting(new BooleanSetting(
		"alerts",
		"client.setting.auto_totem.alerts",
		false
	));

	private final BooleanSetting fallbackShield = setting(new BooleanSetting(
		"fallback_shield",
		"client.setting.auto_totem.fallback_shield",
		false
	));

	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public AutoTotemModule() {
		this(MessageBoxApi.noop());
	}

	public AutoTotemModule(MessageBoxApi messages) {
		super(new ModuleMetadata(
			ModuleId.of("client", "auto_totem"),
			"client.module.auto_totem.name",
			"client.module.auto_totem.description",
			120
		));
		this.controller = new AutoTotemController(Objects.requireNonNull(messages, "messages"));
	}

	public DoubleSetting healthThreshold() {
		return healthThreshold;
	}

	public DoubleSetting fallDamageThreshold() {
		return fallDamageThreshold;
	}

	public DoubleSetting delay() {
		return delay;
	}

	public DoubleSetting randomDelay() {
		return randomDelay;
	}

	public BooleanSetting checkEffects() {
		return checkEffects;
	}

	public BooleanSetting onlyOnLowHealth() {
		return onlyOnLowHealth;
	}

	public ChoiceSetting<OffhandMode> offhandMode() {
		return offhandMode;
	}

	public BooleanSetting alerts() {
		return alerts;
	}

	public BooleanSetting fallbackShield() {
		return fallbackShield;
	}

	@Override
	protected void onClientTick() {
		controller.tick(this);
	}

	@Override
	protected void onDisable() {
		controller.reset();
	}

	private static ChoiceOption<OffhandMode> option(String id, OffhandMode value) {
		return new ChoiceOption<>(
			id,
			"client.option.auto_totem." + id,
			value
		);
	}
}
