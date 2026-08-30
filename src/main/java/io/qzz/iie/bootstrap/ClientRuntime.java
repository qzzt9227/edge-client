package io.qzz.iie.bootstrap;

import io.qzz.iie.Client;
import io.qzz.iie.api.EdgeClientExtension;
import io.qzz.iie.api.EdgeClientExtensionContext;
import io.qzz.iie.api.message.MessageBoxApi;
import io.qzz.iie.api.hud.HudPositionEditorApi;
import io.qzz.iie.config.JsonConfigService;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.module.ModuleShortcutDispatcher;
import io.qzz.iie.module.impl.gui.clickgui.ClickGuiModule;
import io.qzz.iie.module.impl.render.betterhealth.BetterHealthBarHooks;
import io.qzz.iie.module.impl.render.betterhealth.BetterHealthBarHudRenderer;
import io.qzz.iie.module.impl.render.betterhealth.BetterHealthBarModule;
import io.qzz.iie.module.impl.render.crystalanimation.CrystalAnimationHooks;
import io.qzz.iie.module.impl.render.crystalanimation.CrystalAnimationModule;
import io.qzz.iie.module.impl.render.itemrendermode.ItemRenderModeHooks;
import io.qzz.iie.module.impl.render.itemrendermode.ItemRenderModeModule;
import io.qzz.iie.module.impl.render.droppoint.DropPointHooks;
import io.qzz.iie.module.impl.render.droppoint.DropPointModule;
import io.qzz.iie.module.impl.render.droppoint.DropPointWorldRenderer;
import io.qzz.iie.module.impl.render.explosionwarning.ExplosionWarningHooks;
import io.qzz.iie.module.impl.render.explosionwarning.ExplosionWarningModule;
import io.qzz.iie.module.impl.render.explosionwarning.ExplosionWarningWorldRenderer;
import io.qzz.iie.module.impl.render.freelook.FreeLookHooks;
import io.qzz.iie.module.impl.render.freelook.FreeLookModule;
import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import io.qzz.iie.module.impl.render.norender.NoRenderModule;
import io.qzz.iie.module.impl.render.zoom.ZoomHooks;
import io.qzz.iie.module.impl.render.zoom.ZoomModule;
import io.qzz.iie.module.impl.player.packetmine.PacketMineModule;
import io.qzz.iie.module.impl.player.packetmine.PacketMineHooks;
import io.qzz.iie.module.impl.player.packetmine.PacketMineWorldRenderer;
import io.qzz.iie.module.impl.player.autolibrarian.AutoLibrarianModule;
import io.qzz.iie.module.impl.player.autolibrarian.AutoLibrarianTradeReporter;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteHooks;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteModule;
import io.qzz.iie.module.impl.input.invertmouse.InvertMouseModule;
import io.qzz.iie.module.impl.input.invertmouse.InvertMousePitchModule;
import io.qzz.iie.module.impl.input.invertmouse.InvertMouseHooks;
import io.qzz.iie.module.impl.input.specialflip.SpecialFlipModule;
import io.qzz.iie.module.impl.input.specialflip.SpecialFlipHooks;
import io.qzz.iie.setting.KeybindActionDispatcher;
import io.qzz.iie.ui.screen.ClickGuiScreenFactory;
import io.qzz.iie.ui.message.MessageBoxAppearance;
import io.qzz.iie.ui.message.MessageBoxHudRenderer;
import io.qzz.iie.ui.message.MessageBoxManager;
import io.qzz.iie.ui.message.ModuleStateMessageNotifier;
import io.qzz.iie.ui.hud.HudPositionEditorManager;
import io.qzz.iie.ui.hud.ArmorDurabilityHudRenderer;
import io.qzz.iie.ui.hud.PotionEffectsHudRenderer;
import io.qzz.iie.ui.hud.ActiveModulesHudRenderer;
import io.qzz.iie.ui.hud.CpsHudRenderer;
import io.qzz.iie.ui.hud.VersionHudRenderer;
import io.qzz.iie.ui.screen.HudEditorScreen;
import io.qzz.iie.module.impl.gui.clickgui.HudEditorSetting;
import io.qzz.iie.ui.setting.SettingEditorManager;
import io.qzz.iie.ui.screen.AutoLibrarianTargetEditorScreen;
import io.qzz.iie.module.impl.player.autolibrarian.EnchantmentTargetsSetting;
import io.qzz.iie.api.setting.SettingEditorApi;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

/**
 * Composes the client services and owns Minecraft/Fabric integration.
 */
public final class ClientRuntime {
	private final ModuleManager moduleManager = new ModuleManager();

	public void start() {
		MessageBoxManager messages = new MessageBoxManager();
		HudPositionEditorManager hudPositions = new HudPositionEditorManager();
		SettingEditorManager settingEditors =
			new SettingEditorManager(Minecraft.getInstance());
		registerBuiltInSettingEditors(settingEditors, hudPositions);
		ClickGuiModule clickGui = registerBuiltInModules(messages);
		BetterHealthBarModule betterHealthBar = betterHealthBarModule();
		AutoLibrarianModule autoLibrarian = autoLibrarianModule();
		AutoIgniteModule autoIgnite = autoIgniteModule();
		AutoIgniteHooks.install(autoIgnite);
		InvertMouseModule invertMouse = invertMouseModule();
		InvertMousePitchModule invertMousePitch = invertMousePitchModule();
		SpecialFlipModule specialFlip = specialFlipModule();
		InvertMouseHooks.install(invertMouse);
		InvertMouseHooks.installPitch(invertMousePitch);
		SpecialFlipHooks.install(specialFlip);
		CrystalAnimationModule crystalAnimation = crystalAnimationModule();
		CrystalAnimationHooks.install(crystalAnimation);
		ItemRenderModeModule itemRenderMode = itemRenderModeModule();
		ItemRenderModeHooks.install(itemRenderMode);
		DropPointModule dropPoint = dropPointModule();
		DropPointHooks.install(dropPoint);
		DropPointWorldRenderer.install(dropPoint);
		ExplosionWarningModule explosionWarning = explosionWarningModule();
		ExplosionWarningHooks.install(explosionWarning);
		ExplosionWarningWorldRenderer.install(explosionWarning);
		ZoomModule zoom = zoomModule();
		ZoomHooks.install(zoom);
		FreeLookModule freeLook = freeLookModule();
		FreeLookHooks.install(freeLook);
		NoRenderModule noRender = noRenderModule();
		NoRenderHooks.install(noRender);
		PacketMineModule packetMine = packetMineModule();
		PacketMineHooks.install(packetMine);
		PacketMineWorldRenderer.install(packetMine);
		hudPositions.installVanillaVisibility();
		loadExtensions(messages, hudPositions, settingEditors);
		io.qzz.iie.font.ClientFontManager.ensureDirectoriesExist();
		JsonConfigService config = JsonConfigService.atDefaultPath(moduleManager);
		config.load();
		io.qzz.iie.font.ClientFontManager.applyFont(clickGui.customFont().value());
		ModuleStateMessageNotifier.attach(moduleManager, messages);
		registerBetterHealthBar(betterHealthBar, hudPositions);
		registerBuiltInHuds(moduleManager, clickGui, hudPositions);

		ClickGuiScreenFactory screenFactory = new ClickGuiScreenFactory(
			moduleManager,
			currentVersion(),
			() -> clickGui.guiTextScale().value(),
			hudPositions,
			settingEditors
		);
		registerMessageBoxes(messages, clickGui, hudPositions);
		KeybindActionDispatcher openGui =
			new KeybindActionDispatcher(clickGui.openShortcut());
		ModuleShortcutDispatcher shortcuts = new ModuleShortcutDispatcher(moduleManager);
		AutoLibrarianTradeReporter tradeReporter = new AutoLibrarianTradeReporter(
			Minecraft.getInstance(),
			autoLibrarian,
			messages
		);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			DropPointHooks.tick(client);
			ExplosionWarningHooks.tick(client);
			boolean gameplayActive = client.gui.screen() == null;
			shortcuts.update(
				gameplayActive,
				keyCode -> InputConstants.isKeyDown(client.getWindow(), keyCode)
			);
			tradeReporter.tick();
			moduleManager.tickEnabledModules();
			openGuiWhenRequested(client, gameplayActive, openGui, screenFactory);
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> config.saveNow());

		Client.LOGGER.info(
			"Edge Client initialized with {} module(s); config path: {}",
			moduleManager.modules().size(),
			config.configPath()
		);
	}

	private ClickGuiModule registerBuiltInModules(MessageBoxApi messages) {
		return BuiltInModules.register(moduleManager, messages);
	}

	private BetterHealthBarModule betterHealthBarModule() {
		return moduleManager.modules().stream()
			.filter(BetterHealthBarModule.class::isInstance)
			.map(BetterHealthBarModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private AutoLibrarianModule autoLibrarianModule() {
		return moduleManager.modules().stream()
			.filter(AutoLibrarianModule.class::isInstance)
			.map(AutoLibrarianModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private AutoIgniteModule autoIgniteModule() {
		return moduleManager.modules().stream()
			.filter(AutoIgniteModule.class::isInstance)
			.map(AutoIgniteModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private InvertMouseModule invertMouseModule() {
		return moduleManager.modules().stream()
			.filter(InvertMouseModule.class::isInstance)
			.map(InvertMouseModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private InvertMousePitchModule invertMousePitchModule() {
		return moduleManager.modules().stream()
			.filter(InvertMousePitchModule.class::isInstance)
			.map(InvertMousePitchModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private SpecialFlipModule specialFlipModule() {
		return moduleManager.modules().stream()
			.filter(SpecialFlipModule.class::isInstance)
			.map(SpecialFlipModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private CrystalAnimationModule crystalAnimationModule() {
		return moduleManager.modules().stream()
			.filter(CrystalAnimationModule.class::isInstance)
			.map(CrystalAnimationModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private ItemRenderModeModule itemRenderModeModule() {
		return moduleManager.modules().stream()
			.filter(ItemRenderModeModule.class::isInstance)
			.map(ItemRenderModeModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private DropPointModule dropPointModule() {
		return moduleManager.modules().stream()
			.filter(DropPointModule.class::isInstance)
			.map(DropPointModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private ExplosionWarningModule explosionWarningModule() {
		return moduleManager.modules().stream()
			.filter(ExplosionWarningModule.class::isInstance)
			.map(ExplosionWarningModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private ZoomModule zoomModule() {
		return moduleManager.modules().stream()
			.filter(ZoomModule.class::isInstance)
			.map(ZoomModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private FreeLookModule freeLookModule() {
		return moduleManager.modules().stream()
			.filter(FreeLookModule.class::isInstance)
			.map(FreeLookModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private NoRenderModule noRenderModule() {
		return moduleManager.modules().stream()
			.filter(NoRenderModule.class::isInstance)
			.map(NoRenderModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private PacketMineModule packetMineModule() {
		return moduleManager.modules().stream()
			.filter(PacketMineModule.class::isInstance)
			.map(PacketMineModule.class::cast)
			.findFirst()
			.orElseThrow();
	}

	private void loadExtensions(
		MessageBoxApi messages,
		HudPositionEditorApi hudPositions,
		SettingEditorApi settingEditors
	) {
		EdgeClientExtensionContext context = new EdgeClientExtensionContext() {
			@Override
			public void registerModule(io.qzz.iie.module.Module module) {
				moduleManager.register(module);
			}

			@Override
			public MessageBoxApi messages() {
				return messages;
			}

			@Override
			public HudPositionEditorApi hudPositions() {
				return hudPositions;
			}

			@Override
			public SettingEditorApi settingEditors() {
				return settingEditors;
			}
		};
		for (EntrypointContainer<EdgeClientExtension> container
			: FabricLoader.getInstance().getEntrypointContainers(
			EdgeClientExtension.ENTRYPOINT_ID,
			EdgeClientExtension.class
		)) {
			try {
				container.getEntrypoint().initialize(context);
			} catch (VirtualMachineError fatalError) {
				throw fatalError;
			} catch (Throwable cause) {
				Client.LOGGER.error(
					"Edge Client extension from mod '{}' failed during module registration",
					container.getProvider().getMetadata().getId(),
					cause
				);
			}
		}
	}

	private static void registerBuiltInSettingEditors(
		SettingEditorApi settingEditors,
		HudPositionEditorManager hudPositions
	) {
		settingEditors.register(
			HudEditorSetting.EDITOR_ID,
			(setting, parent) -> new HudEditorScreen(hudPositions, parent)
		);
		settingEditors.register(
			EnchantmentTargetsSetting.EDITOR_ID,
			(setting, parent) -> {
				if (!(setting instanceof EnchantmentTargetsSetting targets)) {
					throw new IllegalArgumentException(
						"Auto Librarian target editor requires EnchantmentTargetsSetting"
					);
				}
				return new AutoLibrarianTargetEditorScreen(targets, parent);
			}
		);
	}

	private static void registerBuiltInHuds(
		ModuleManager moduleManager,
		ClickGuiModule clickGui,
		HudPositionEditorManager hudPositions
	) {
		// 1. 盔甲耐久 HUD
		ArmorDurabilityHudRenderer armorRenderer = new ArmorDurabilityHudRenderer(
			clickGui.armorHudEnabled(),
			clickGui.armorHudPosition(),
			hudPositions::isEditing
		);
		hudPositions.register(clickGui.armorHudPosition(), armorRenderer);
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.ARMOR_BAR,
			Client.id("armor_durability"),
			armorRenderer::extract
		);

		// 2. 药水效果 HUD
		PotionEffectsHudRenderer potionRenderer = new PotionEffectsHudRenderer(
			clickGui.potionHudEnabled(),
			clickGui.potionHudPosition(),
			hudPositions::isEditing
		);
		hudPositions.register(clickGui.potionHudPosition(), potionRenderer);
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.MOB_EFFECTS,
			Client.id("potion_effects"),
			potionRenderer::extract
		);

		// 3. 启用功能列表 (ArrayList) HUD
		ActiveModulesHudRenderer arrayListRenderer = new ActiveModulesHudRenderer(
			moduleManager,
			clickGui.arrayListEnabled(),
			clickGui.arrayListPosition(),
			hudPositions::isEditing
		);
		hudPositions.register(clickGui.arrayListPosition(), arrayListRenderer);
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.SCOREBOARD,
			Client.id("array_list"),
			arrayListRenderer::extract
		);

		// 4. CPS 显示 HUD
		CpsHudRenderer cpsRenderer = new CpsHudRenderer(
			clickGui.cpsHudEnabled(),
			clickGui.cpsHudPosition(),
			hudPositions::isEditing
		);
		hudPositions.register(clickGui.cpsHudPosition(), cpsRenderer);
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.SCOREBOARD,
			Client.id("cps"),
			cpsRenderer::extract
		);

		// 5. 客户端版本 HUD
		VersionHudRenderer versionRenderer = new VersionHudRenderer(
			clickGui.versionHudEnabled(),
			clickGui.versionHudPosition(),
			hudPositions::isEditing
		);
		hudPositions.register(clickGui.versionHudPosition(), versionRenderer);
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.SCOREBOARD,
			Client.id("version_hud"),
			versionRenderer::extract
		);
	}

	private static void registerMessageBoxes(
		MessageBoxManager messages,
		ClickGuiModule clickGui,
		HudPositionEditorManager hudPositions
	) {
		MessageBoxHudRenderer renderer = new MessageBoxHudRenderer(
			messages,
			() -> new MessageBoxAppearance(
				clickGui.messageBoxScale().value(),
				clickGui.messageTextScale().value(),
				clickGui.messageOpacity().value(),
				clickGui.messageTextColor(),
				clickGui.messageFont().value()
			),
			hudPositions::isEditing
		);
		HudElementRegistry.attachElementBefore(
			VanillaHudElements.CHAT,
			Client.id("message_boxes"),
			renderer::extract
		);
	}

	private static void registerBetterHealthBar(
		BetterHealthBarModule module,
		HudPositionEditorManager hudPositions
	) {
		BetterHealthBarHooks.install(module);
		BetterHealthBarHudRenderer renderer = new BetterHealthBarHudRenderer(
			module,
			hudPositions::isEditing
		);
		hudPositions.register(module.numberPosition(), renderer);
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.HEALTH_BAR,
			Client.id("better_health_number"),
			renderer::extract
		);
	}

	private static void openGuiWhenRequested(
		Minecraft client,
		boolean gameplayActive,
		KeybindActionDispatcher openGui,
		ClickGuiScreenFactory screenFactory
	) {
		if (openGui.update(
			gameplayActive,
			keyCode -> InputConstants.isKeyDown(client.getWindow(), keyCode)
		)) {
			client.setScreenAndShow(screenFactory.create(null));
		}
	}

	private static String currentVersion() {
		return FabricLoader.getInstance()
			.getModContainer(Client.MOD_ID)
			.map(container -> container.getMetadata().getVersion().getFriendlyString())
			.orElse("dev");
	}
}
