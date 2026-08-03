package io.qzz.iie.bootstrap;

import io.qzz.iie.api.message.MessageBoxApi;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebModule;
import io.qzz.iie.module.impl.gui.clickgui.ClickGuiModule;
import io.qzz.iie.module.impl.render.fullbright.FullbrightModule;
import io.qzz.iie.module.impl.render.betterhealth.BetterHealthBarModule;
import io.qzz.iie.module.impl.render.itemrendermode.ItemRenderModeModule;
import io.qzz.iie.module.impl.player.autolibrarian.AutoLibrarianModule;
import io.qzz.iie.module.impl.input.invertmouse.InvertMouseModule;
import io.qzz.iie.module.impl.input.invertmouse.InvertMousePitchModule;
import io.qzz.iie.module.impl.input.specialflip.SpecialFlipModule;

import java.util.Objects;

public final class BuiltInModules {
	private BuiltInModules() {
	}

	public static ClickGuiModule register(ModuleManager moduleManager) {
		return register(moduleManager, MessageBoxApi.noop());
	}

	public static ClickGuiModule register(
		ModuleManager moduleManager,
		MessageBoxApi messages
	) {
		ModuleManager manager = Objects.requireNonNull(moduleManager, "moduleManager");
		manager.register(new AutoWebModule());
		manager.register(new AutoLibrarianModule(
			Objects.requireNonNull(messages, "messages")
		));
		manager.register(new InvertMouseModule());
		manager.register(new InvertMousePitchModule());
		manager.register(new SpecialFlipModule());
		manager.register(new FullbrightModule());
		manager.register(new BetterHealthBarModule());
		manager.register(new ItemRenderModeModule());
		return manager.register(new ClickGuiModule());
	}
}
