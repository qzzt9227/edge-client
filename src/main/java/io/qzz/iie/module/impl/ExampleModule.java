package io.qzz.iie.module.impl;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleCategories;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.DoubleSetting;

/**
 * Demonstrates the logic-only module authoring surface.
 */
public final class ExampleModule extends Module {
	private final BooleanSetting showStatus = setting(
		new BooleanSetting(
			"show_status",
			"client.setting.example.show_status",
			true
		)
	);
	private final DoubleSetting strength = setting(
		new DoubleSetting(
			"strength",
			"client.setting.example.strength",
			0.5,
			0.0,
			1.0,
			0.05
		)
	);

	public ExampleModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "example"),
			"client.module.example.name",
			"client.module.example.description",
			ModuleCategories.COMBAT,
			100
		));
	}

	public boolean shouldShowStatus() {
		return isEnabled() && showStatus.value();
	}

	public double strength() {
		return strength.value();
	}
}
