package io.qzz.iie.bootstrap;

import io.qzz.iie.api.message.MessageBoxApi;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.module.impl.movement.autowalk.AutoWalkModule;
import io.qzz.iie.module.impl.movement.flight.FlightModule;
import io.qzz.iie.module.impl.movement.safewalkplus.SafeWalkPlusModule;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebModule;
import io.qzz.iie.module.impl.combat.autoclicker.AutoClickerModule;
import io.qzz.iie.module.impl.combat.bedaura.BedAuraModule;
import io.qzz.iie.module.impl.combat.autototem.AutoTotemModule;
import io.qzz.iie.module.impl.gui.clickgui.ClickGuiModule;
import io.qzz.iie.module.impl.render.betterhealth.BetterHealthBarModule;
import io.qzz.iie.module.impl.render.crystalanimation.CrystalAnimationModule;
import io.qzz.iie.module.impl.render.droppoint.DropPointModule;
import io.qzz.iie.module.impl.render.explosionwarning.ExplosionWarningModule;
import io.qzz.iie.module.impl.render.freelook.FreeLookModule;
import io.qzz.iie.module.impl.render.fullbright.FullbrightModule;
import io.qzz.iie.module.impl.render.itemrendermode.ItemRenderModeModule;
import io.qzz.iie.module.impl.render.norender.NoRenderModule;
import io.qzz.iie.module.impl.render.zoom.ZoomModule;
import io.qzz.iie.module.impl.player.airplace.AirPlaceModule;
import io.qzz.iie.module.impl.player.antiquit.AntiQuitModule;
import io.qzz.iie.module.impl.player.autolibrarian.AutoLibrarianModule;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteModule;
import io.qzz.iie.module.impl.player.copynbt.CopyNbtModule;
import io.qzz.iie.module.impl.player.packetmine.PacketMineModule;
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
		manager.register(new AutoWalkModule());
		manager.register(new SafeWalkPlusModule());
		manager.register(new FlightModule());
		manager.register(new AutoWebModule());
		manager.register(new AutoClickerModule());
		manager.register(new BedAuraModule());
		manager.register(new AutoTotemModule(
			Objects.requireNonNull(messages, "messages")
		));
		manager.register(new AirPlaceModule());
		manager.register(new AutoIgniteModule());
		manager.register(new PacketMineModule());
		manager.register(new CopyNbtModule());
		manager.register(new AntiQuitModule());
		manager.register(new AutoLibrarianModule(
			Objects.requireNonNull(messages, "messages")
		));
		manager.register(new InvertMouseModule());
		manager.register(new InvertMousePitchModule());
		manager.register(new SpecialFlipModule());
		manager.register(new FullbrightModule());
		manager.register(new BetterHealthBarModule());
		manager.register(new CrystalAnimationModule());
		manager.register(new ItemRenderModeModule());
		manager.register(new DropPointModule(messages));
		manager.register(new ExplosionWarningModule(messages));
		manager.register(new ZoomModule());
		manager.register(new FreeLookModule());
		manager.register(new NoRenderModule());
		return manager.register(new ClickGuiModule());
	}
}
