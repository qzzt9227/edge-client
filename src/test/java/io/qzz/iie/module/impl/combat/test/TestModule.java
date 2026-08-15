package io.qzz.iie.module.impl.combat.test;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.DoubleSetting;

public final class TestModule extends Module {
	private final BooleanSetting toggle = setting(new BooleanSetting("toggle", "client.setting.toggle", true));
	private final DoubleSetting amount = setting(new DoubleSetting("amount", "client.setting.amount", 5.0, 0.0, 10.0, 1.0));

	public TestModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "test_module"),
			"client.module.test_module.name",
			"client.module.test_module.description",
			100
		));
	}

	public BooleanSetting toggle() {
		return toggle;
	}

	public DoubleSetting amount() {
		return amount;
	}
}
