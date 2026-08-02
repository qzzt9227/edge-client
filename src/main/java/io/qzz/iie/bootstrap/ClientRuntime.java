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
import io.qzz.iie.module.impl.player.autolibrarian.AutoLibrarianModule;
import io.qzz.iie.module.impl.player.autolibrarian.AutoLibrarianTradeReporter;
import io.qzz.iie.module.impl.player.invertmouse.InvertMouseModule;
import io.qzz.iie.module.impl.player.invertmouse.InvertMousePitchModule;
import io.qzz.iie.module.impl.player.invertmouse.InvertMouseHooks;
import io.qzz.iie.setting.KeybindActionDispatcher;
import io.qzz.iie.ui.screen.ClickGuiScreenFactory;
import io.qzz.iie.ui.message.MessageBoxAppearance;
import io.qzz.iie.ui.message.MessageBoxHudRenderer;
import io.qzz.iie.ui.message.MessageBoxManager;
import io.qzz.iie.ui.message.ModuleStateMessageNotifier;
import io.qzz.iie.ui.hud.HudPositionEditorManager;
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
		registerBuiltInSettingEditors(settingEditors);
		ClickGuiModule clickGui = registerBuiltInModules(messages);
		BetterHealthBarModule betterHealthBar = betterHealthBarModule();
		AutoLibrarianModule autoLibrarian = autoLibrarianModule();
		InvertMouseModule invertMouse = invertMouseModule();
		InvertMousePitchModule invertMousePitch = invertMousePitchModule();
		InvertMouseHooks.install(invertMouse);
		InvertMouseHooks.installPitch(invertMousePitch);
		hudPositions.installVanillaVisibility();
		loadExtensions(messages, hudPositions, settingEditors);
		JsonConfigService config = JsonConfigService.atDefaultPath(moduleManager);
		config.load();
		ModuleStateMessageNotifier.attach(moduleManager, messages);
		registerBetterHealthBar(betterHealthBar, hudPositions);

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
		SettingEditorApi settingEditors
	) {
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
