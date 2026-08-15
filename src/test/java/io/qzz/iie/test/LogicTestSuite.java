package io.qzz.iie.test;

import io.qzz.iie.api.EdgeClientExtension;
import io.qzz.iie.api.EdgeClientExtensionContext;
import io.qzz.iie.api.message.MessageBoxApi;
import io.qzz.iie.api.hud.HudPosition;
import io.qzz.iie.api.hud.HudPositionDrag;
import io.qzz.iie.api.hud.HudPositionLayout;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.bootstrap.BuiltInModules;
import io.qzz.iie.config.JsonConfigService;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleChangeResult;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.module.ModuleShortcutDispatcher;
import io.qzz.iie.module.impl.movement.autowalk.AutoWalkModule;
import io.qzz.iie.module.impl.movement.safewalkplus.SafeWalkPlusModule;
import io.qzz.iie.module.impl.movement.safewalkplus.SafeWalkPlusPolicy;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebModule;
import io.qzz.iie.module.impl.gui.clickgui.ClickGuiModule;
import io.qzz.iie.module.impl.player.autolibrarian.EnchantmentTarget;
import io.qzz.iie.module.impl.player.autolibrarian.EnchantmentTargetsSetting;
import io.qzz.iie.module.impl.player.autolibrarian.AutoLibrarianLogicContract;
import io.qzz.iie.module.impl.player.autolibrarian.AutoLibrarianModule;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteModule;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteItemPolicy;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteRotation;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTargetQueue;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteVisualState;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.IgnitionItem;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.ItemPriority;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.ItemSource;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.TargetHandling;
import io.qzz.iie.module.impl.input.invertmouse.InvertMouseHooks;
import io.qzz.iie.module.impl.input.invertmouse.InvertMouseModule;
import io.qzz.iie.module.impl.input.invertmouse.InvertMousePitchModule;
import io.qzz.iie.module.impl.input.specialflip.SpecialFlipHooks;
import io.qzz.iie.module.impl.input.specialflip.SpecialFlipModule;
import io.qzz.iie.module.impl.render.betterhealth.BetterHealthBarModule;
import io.qzz.iie.module.impl.render.betterhealth.BetterHealthBarPolicy;
import io.qzz.iie.module.impl.render.betterhealth.BetterHealthBarHooks;
import io.qzz.iie.module.impl.render.itemrendermode.ItemRenderMode;
import io.qzz.iie.module.impl.render.itemrendermode.ItemRenderModeHooks;
import io.qzz.iie.module.impl.render.itemrendermode.ItemRenderModeModule;
import io.qzz.iie.module.impl.render.itemrendermode.ItemRenderModeRenderState;
import io.qzz.iie.module.impl.render.droppoint.DropPointBlockKind;
import io.qzz.iie.module.impl.render.droppoint.DropPointColor;
import io.qzz.iie.module.impl.render.droppoint.DropPointFootprint;
import io.qzz.iie.module.impl.render.droppoint.DropPointFallDamage;
import io.qzz.iie.module.impl.render.droppoint.DropPointPolicy;
import io.qzz.iie.module.impl.render.droppoint.DropPointRole;
import io.qzz.iie.module.impl.render.droppoint.DropPointModule;
import io.qzz.iie.module.impl.render.explosionwarning.ExplosionCountdown;
import io.qzz.iie.module.impl.render.explosionwarning.ExplosionWarningPlacement;
import io.qzz.iie.module.impl.render.explosionwarning.ExplosionTargetKind;
import io.qzz.iie.module.impl.render.explosionwarning.ExplosionWarningEvent;
import io.qzz.iie.module.impl.render.explosionwarning.ExplosionWarningTracker;
import io.qzz.iie.module.impl.render.explosionwarning.ExplosionWarningModule;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebPlanner;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebRotation;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.BlockCell;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.HotbarMode;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.InventoryMode;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.PlacementCadence;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.PlacementPattern;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.TargetPriority;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.TargetSnapshot;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.TargetType;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;
import io.qzz.iie.setting.KeybindActionDispatcher;
import io.qzz.iie.setting.KeybindValue;
import io.qzz.iie.ui.animation.AnimatedDouble;
import io.qzz.iie.ui.animation.AnimatedRect;
import io.qzz.iie.ui.animation.AnimatedScroll;
import io.qzz.iie.ui.animation.AnimationFrameClock;
import io.qzz.iie.ui.animation.AnimationSpec;
import io.qzz.iie.ui.animation.ArgbColor;
import io.qzz.iie.ui.animation.Easing;
import io.qzz.iie.ui.binding.ModuleEnabledBinding;
import io.qzz.iie.ui.binding.BindingUpdateResult;
import io.qzz.iie.ui.binding.RangedDoubleBinding;
import io.qzz.iie.ui.component.control.SliderControl;
import io.qzz.iie.ui.component.control.ChoiceControl;
import io.qzz.iie.ui.component.control.KeybindControl;
import io.qzz.iie.ui.component.control.HudPositionControl;
import io.qzz.iie.ui.component.control.EditorSettingControl;
import io.qzz.iie.ui.component.control.ToggleControl;
import io.qzz.iie.ui.component.control.UnsupportedControl;
import io.qzz.iie.ui.factory.SettingControlFactory;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputRouter;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.message.MessageBoxAppearance;
import io.qzz.iie.ui.message.MessageBoxManager;
import io.qzz.iie.ui.message.ModuleStateMessageNotifier;
import io.qzz.iie.ui.panel.PanelContract;
import io.qzz.iie.ui.screen.AutoLibrarianTargetEditorScreen;
import io.qzz.iie.ui.hud.HudPositionEditorVisibilityContract;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Duration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LogicTestSuite {
	private LogicTestSuite() {
	}

	public static void main(String[] args) {
		moduleIdsAreNamespacedAndValidated();
		moduleRegistrationRejectsDuplicateIds();
		moduleLifecycleIsIdempotentAndRollsBackFailures();
		builtInModulesRegisterFullbrightAndAutoWeb();
		autoWalkModuleMetadataAndLifecycle();
		safeWalkPlusCoverageCalculationAndDecision();
		safeWalkPlusModuleMetadataAndDefaults();
		invertMouseDeclaresDefaultsAndHooksFollowLifecycle();
		specialFlipDeclaresDefaultsAndHooksFollowLifecycle();
		betterHealthBarDeclaresDefaultsAndCapsExtraRows();
		itemRenderModeDeclaresDefaultsAndHooksFollowLifecycle();
		dropPointPolicyHonorsSpecialBlocksAndDistanceThreshold();
		dropPointFootprintSelectsTheLargestCoveredBlock();
		dropPointColorsClampToExplicitArgbChannels();
		dropPointModuleDeclaresIndependentColorOpacitySliders();
		explosionWarningCountdownUsesMillisecondPrecision();
		explosionWarningPlacementKeepsCreeperTextVisibleAndAppliesOffsets();
		explosionWarningTrackerReportsRangeAndImpendingTransitionsOnce();
		explosionWarningModuleDeclaresRecommendedDefaults();
		autoIgniteDeclaresInterviewedDefaults();
		autoIgnitePriorityAndTargetHandlingAreDeterministic();
		autoIgniteRotationIsSmoothAndUsesTheShortestPath();
		clickGuiAppearanceModuleDeclaresCustomizableDefaults();
		customFontManagerDirectoryCreationAndScan();
		messageBoxAppearancePreservesAspectRatio();
		messageBoxApiQueuesAndExpiresMessages();
		moduleStateChangesUseSharedMessageBoxApi();
		autoWebDeclaresConfirmedDefaults();
		autoWebPlacementPatternsAreDeterministic();
		autoWebTargetPrioritiesSelectExpectedCandidate();
		autoWebRotationUsesShortestWrappedPath();
		onlyEnabledModulesReceiveClientTicks();
		modulesCanRequestSafeDisableAfterTheirTick();
		settingsNormalizeValuesAndRejectDuplicateIds();
		choiceSettingsValidateOptionsAndSelection();
		choiceControlExpandsSelectsAndCancels();
		choiceControlCollapsesWhenFocusMoves();
		choiceHoverHighlightsOnlyDrawerOptions();
		PanelContract.verify();
		jsonConfigPersistsModulesSettingsAndKeybinds();
		autoLibrarianTargetsNormalizeAndPersist();
		AutoLibrarianLogicContract.verify();
		autoLibrarianDeclaresOriginalSettingsAndDefaults();
		editorSettingsReceiveReusableGuiControls();
		hudPositionsNormalizeLayoutAndPersist();
		hudPositionDragCommitsOnlyOnRelease();
		HudPositionEditorVisibilityContract.verify();
		clickGuiShortcutPersistsFromControl();
		keybindControlConfirmsCancelsAndClears();
		keybindActionsUsePressEdgesAndIgnoreOpenScreens();
		moduleShortcutsUsePressEdgesAndIgnoreOpenScreens();
		extensionsRegisterLogicOnlyModulesThroughContext();
		inputRouterDistinguishesRightClickAndCapturesDrag();
		slidersCanWriteCanonicalNestedValues();
		autoLibrarianSearchUsesNativeImeInput();
		moduleBindingPreservesManagerLifecycle();
		moduleControlsShareLiveManagerState();
		unsupportedSettingsDegradeWithoutCrashingTheGui();
		animationProgressesAndRetargetsWithoutJumping();
		smoothScrollAccumulatesAndReversesWithoutJumping();
		animatedGeometryAndColorInterpolateDeterministically();
		toggleAnimationFollowsItsBinding();
		animationClockUsesElapsedTimeAndClampsLongFrames();
		animationRejectsNonFiniteInputsAndEasingOutputs();
		System.out.println("LogicTestSuite: all tests passed");
	}

	private static void jsonConfigPersistsModulesSettingsAndKeybinds() {
		Path temporaryDirectory = null;
		try {
			temporaryDirectory = Files.createTempDirectory("edge-client-config-test");
			Path configPath = temporaryDirectory.resolve("edge-config.json");

			ModuleManager sourceManager = new ModuleManager();
			AutoWebModule source = sourceManager.register(new AutoWebModule());
			JsonConfigService sourceConfig = new JsonConfigService(sourceManager, configPath);
			check(sourceConfig.load(), "missing config must initialize a clean baseline");
			source.targetPriority().selectOption(2);
			source.range().set(5.0);
			source.rotationTicks().set(7.4);
			source.keybind().orElseThrow().bind(82);
			sourceManager.setEnabled(source.id(), true);

			check(Files.isRegularFile(configPath), "a model change must immediately create the config file");
			String json = Files.readString(configPath);
			check(json.contains("\"crosshair\""), "choices must persist by stable option ID");

			ModuleManager restoredManager = new ModuleManager();
			AutoWebModule restored = restoredManager.register(new AutoWebModule());
			JsonConfigService restoredConfig = new JsonConfigService(restoredManager, configPath);
			check(restoredConfig.load(), "valid config must load successfully");
			check(restored.isEnabled(), "module enabled state must be restored");
			check(
				restored.targetPriority().value() == TargetPriority.CROSSHAIR,
				"choice setting must be restored"
			);
			check(restored.range().value() == 5.0, "double setting must be restored");
			check(restored.rotationTicks().value() == 7.4, "fractional ticks must be restored");
			check(
				restored.keybind().orElseThrow().value().keyCode() == 82,
				"keybind must be restored"
			);
			check(!restoredConfig.saveIfChanged(), "unchanged state must not rewrite config");
			restored.range().set(4.0);
			check(
				Files.readString(configPath).contains("\"range\": 4.0"),
				"setting changes must save immediately"
			);
			check(
				!restoredConfig.saveIfChanged(),
				"immediate save must update the stored snapshot"
			);
		} catch (IOException error) {
			throw new AssertionError("config persistence test failed", error);
		} finally {
			if (temporaryDirectory != null) {
				try {
					Files.deleteIfExists(temporaryDirectory.resolve("edge-config.json"));
					Files.deleteIfExists(temporaryDirectory);
				} catch (IOException ignored) {
					// 临时测试目录由操作系统后续清理，不影响功能判断。
				}
			}
		}
	}

	private static void safeWalkPlusCoverageCalculationAndDecision() {
		// 玩家完全位于 (0, 0) 的单个固体方块上：碰撞箱 [0.2, 0.8] x [0.2, 0.8]
		double coverageFull = SafeWalkPlusPolicy.calculateSupportCoverage(
			0.2, 0.8, 0.2, 0.8,
			(x, z) -> x == 0 && z == 0
		);
		check(closeTo(coverageFull, 1.0), "fully supported player must have 100% coverage");
		check(!SafeWalkPlusPolicy.shouldForceSneak(coverageFull, 60.0, true), "100% coverage must not force sneak");

		// 玩家悬挂在方块边缘：碰撞箱 [0.7, 1.3] x [0.2, 0.8]，宽 0.6，位于方块 0 的长度为 0.3 (50%)，方块 1 为空气
		double coverageHalf = SafeWalkPlusPolicy.calculateSupportCoverage(
			0.7, 1.3, 0.2, 0.8,
			(x, z) -> x == 0 && z == 0
		);
		check(closeTo(coverageHalf, 0.5), "half supported player must have 50% coverage");
		check(SafeWalkPlusPolicy.shouldForceSneak(coverageHalf, 60.0, true), "50% coverage (< 60%) must force sneak");
		check(!SafeWalkPlusPolicy.shouldForceSneak(coverageHalf, 40.0, true), "50% coverage (>= 40%) must not force sneak when threshold is 40%");
		check(!SafeWalkPlusPolicy.shouldForceSneak(coverageHalf, 60.0, false), "airborne player must not force sneak");

		// 完全悬空处于空中：0% 覆盖率
		double coverageNone = SafeWalkPlusPolicy.calculateSupportCoverage(
			0.2, 0.8, 0.2, 0.8,
			(x, z) -> false
		);
		check(closeTo(coverageNone, 0.0), "unsupported player must have 0% coverage");
	}

	private static void safeWalkPlusModuleMetadataAndDefaults() {
		SafeWalkPlusModule module = new SafeWalkPlusModule();
		check(module.category().id().equals("movement"), "safe walk plus must reside in movement category");
		check(module.coverageThreshold().value() == 60.0, "coverage threshold must default to 60%");
		check(module.coverageThreshold().minimum() == 1.0, "coverage threshold minimum must be 1%");
		check(module.coverageThreshold().maximum() == 100.0, "coverage threshold maximum must be 100%");
		check(module.coverageThreshold().step() == 1.0, "coverage threshold step must be 1%");
	}

	private static void dropPointPolicyHonorsSpecialBlocksAndDistanceThreshold() {
		check(
			DropPointPolicy.decide(
				DropPointBlockKind.NORMAL, 4.0, 100.0, 10.0, false
			).role() == DropPointRole.NONE,
			"a drop point at exactly four blocks must stay hidden"
		);
		check(
			DropPointPolicy.decide(
				DropPointBlockKind.SAFE, 20.0, 100.0, 1.0, false
			).role() == DropPointRole.SAFE,
			"safe blocks must remain green even when a generic damage estimate is lethal"
		);
		check(
			DropPointPolicy.decide(
				DropPointBlockKind.SCAFFOLD, 20.0, 0.0, 20.0, false
			).role() == DropPointRole.SCAFFOLD_NEEDS_SNEAKING,
			"scaffolding must request sneaking when the player is not sneaking"
		);
		check(
			DropPointPolicy.decide(
				DropPointBlockKind.SCAFFOLD, 20.0, 0.0, 20.0, true
			).role() == DropPointRole.SCAFFOLD_SNEAKING,
			"sneaking on scaffolding must use the green role"
		);
		check(
			DropPointPolicy.decide(
				DropPointBlockKind.NORMAL, 20.0, 10.1, 10.0, false
			).role() == DropPointRole.DANGER,
			"lethal generic damage must use the danger role"
		);
		check(
			DropPointPolicy.decide(
				DropPointBlockKind.HAY_BALE, 20.0, DropPointFallDamage.calculate(20.0, 3.0, 1.0, 0.2), 10.0, false
			).role() == DropPointRole.DEFAULT,
			"hay bale damage reduction must allow non-lethal landings to use the default role"
		);
		check(
			DropPointFallDamage.calculate(20.0, 3.0, 1.0, 0.2) == 3.0,
			"hay bale fall damage must use the vanilla 0.2 landing multiplier"
		);
	}

	private static void dropPointFootprintSelectsTheLargestCoveredBlock() {
		DropPointFootprint.BlockCell selected = DropPointFootprint.largestCoveredCell(
			0.1, 1.9, 0.1, 1.2, 42
		);
		check(selected != null, "a non-empty collision footprint must select a block");
		check(selected.x() == 0 && selected.z() == 0, "the largest overlap must win");
		check(selected.y() == 42, "the selected cell must preserve the landing plane");
	}

	private static void dropPointColorsClampToExplicitArgbChannels() {
		DropPointColor color = new DropPointColor(0.0, 0.5, 1.0, 0.25);
		check(color.argb() == 0x400080FF, "RGBA slider values must produce the expected ARGB color");
		expectThrows(
			IllegalArgumentException.class,
			() -> new DropPointColor(1.1, 0.0, 0.0, 1.0),
			"color channels outside the normalized range must be rejected"
		);
	}

	private static void dropPointModuleDeclaresIndependentColorOpacitySliders() {
		DropPointModule module = new DropPointModule(MessageBoxApi.noop());
		check(module.settings().size() == 21, "drop point must expose four channels for each of five colors plus hint count");
		check(module.scaffoldHintCount().value() == 2.0, "scaffold hint count must default to two");
		check(module.scaffoldHintCount().minimum() == 1.0, "scaffold hint count minimum must be one");
		check(module.scaffoldHintCount().maximum() == 8.0, "scaffold hint count maximum must be eight");
		check(module.safeColor().opacity() == 0.4, "safe color opacity must be independently adjustable");
	}

	private static void explosionWarningCountdownUsesMillisecondPrecision() {
		check(
			ExplosionCountdown.tntRemainingMillis(20, 0.25F) == 988L,
			"TNT countdown must interpolate remaining fuse time to milliseconds"
		);
		check(
			ExplosionCountdown.creeperRemainingMillis(0.5F) == 800L,
			"creeper countdown must map vanilla swelling to the 30 tick fuse"
		);
		check(
			ExplosionCountdown.formatMillis(1234L).equals("1.234 s"),
			"countdown text must retain millisecond precision"
		);
	}

	private static void explosionWarningPlacementKeepsCreeperTextVisibleAndAppliesOffsets() {
		ExplosionWarningPlacement.Position creeper = ExplosionWarningPlacement.resolve(
			ExplosionTargetKind.CREEPER,
			0.0,
			65.0,
			0.0,
			0.3,
			0.3,
			3.0,
			4.0,
			0.1,
			0.25,
			-0.2
		);
		check(closeTo(creeper.x(), 0.397), "creeper text must move outside the collision box toward the camera before applying X offset");
		check(closeTo(creeper.y(), 65.25), "countdown Y offset must be added to the automatic anchor");
		check(closeTo(creeper.z(), 0.196), "countdown Z offset must be added after the automatic front placement");

		ExplosionWarningPlacement.Position tnt = ExplosionWarningPlacement.resolve(
			ExplosionTargetKind.TNT,
			10.0,
			70.0,
			20.0,
			0.49,
			0.49,
			0.0,
			0.0,
			-0.5,
			0.5,
			1.0
		);
		check(closeTo(tnt.x(), 9.5) && closeTo(tnt.y(), 70.5) && closeTo(tnt.z(), 21.0),
			"TNT countdown placement must preserve its automatic anchor and apply all three offsets");
	}

	private static void explosionWarningTrackerReportsRangeAndImpendingTransitionsOnce() {
		ExplosionWarningTracker tracker = new ExplosionWarningTracker();
		check(
			tracker.observe(7, ExplosionTargetKind.CREEPER, true, false)
				.contains(ExplosionWarningEvent.ENTERED_RANGE),
			"a creeper entering range must emit one range event"
		);
		check(
			tracker.observe(7, ExplosionTargetKind.CREEPER, true, false).isEmpty(),
			"the same creeper must not repeat the range event every tick"
		);
		check(
			tracker.observe(7, ExplosionTargetKind.CREEPER, true, true)
				.contains(ExplosionWarningEvent.IMPENDING_EXPLOSION),
			"the transition into the fuse state must emit one impending event"
		);
		check(
			tracker.observe(7, ExplosionTargetKind.CREEPER, true, true).isEmpty(),
			"the same fuse state must not repeat the impending event every tick"
		);
		tracker.observe(7, ExplosionTargetKind.CREEPER, false, false);
		check(
			tracker.observe(7, ExplosionTargetKind.CREEPER, true, false)
				.contains(ExplosionWarningEvent.ENTERED_RANGE),
			"leaving and re-entering range must arm a new range event"
		);
		check(
			tracker.observe(8, ExplosionTargetKind.TNT, true, true)
				.contains(ExplosionWarningEvent.IMPENDING_EXPLOSION),
			"TNT must emit the impending event without a creeper range event"
		);
		tracker.observe(9, ExplosionTargetKind.TNT, false, false);
		check(
			tracker.observe(9, ExplosionTargetKind.TNT, true, true)
				.contains(ExplosionWarningEvent.IMPENDING_EXPLOSION),
			"an already-primed TNT entering the radius must still warn once"
		);
	}

	private static void explosionWarningModuleDeclaresRecommendedDefaults() {
		ExplosionWarningModule module = new ExplosionWarningModule(MessageBoxApi.noop());
		check(module.radius().value() == 5.0, "explosion warning radius must default to five blocks");
		check(module.radius().minimum() == 1.0 && module.radius().maximum() == 10.0,
			"explosion warning radius must be constrained to one through ten blocks");
		check(module.impendingMessage().value(), "impending explosion messages must default to enabled");
		check(module.creeperRangeMessage().value(), "creeper range messages must default to enabled");
		check(module.messageColor() == 0xFFFF0000, "explosion warning message color must default to red");
		check(module.countdownOffsetX().value() == 0.0, "countdown X offset must default to zero");
		check(module.countdownOffsetY().value() == 0.0, "countdown Y offset must default to zero");
		check(module.countdownOffsetZ().value() == 0.0, "countdown Z offset must default to zero");
		check(module.countdownOffsetX().minimum() == -2.0 && module.countdownOffsetX().maximum() == 2.0,
			"countdown offsets must allow precise adjustment from minus two through two blocks");
	}

	private static void autoIgniteDeclaresInterviewedDefaults() {
		AutoIgniteModule module = new AutoIgniteModule();
		check(module.category().id().equals("player"), "auto ignite must appear in the player category");
		check(module.itemSource().value() == ItemSource.HOTBAR, "direct hotbar access must be the default source");
		check(module.itemPriority().value() == ItemPriority.FLINT_FIRST, "flint and steel must be preferred by default");
		check(module.restoreAfterFlint().value(), "flint ignition must return to the previous TNT slot by default");
		check(!module.cameraFollows().value(), "client camera following must default to disabled");
		check(module.rotationTicks().value() == 1.0, "rotation and restoration must default to one tick each");
		check(module.rotationTicks().minimum() == 1.0 && module.rotationTicks().maximum() == 10.0,
			"rotation time must be constrained to one through ten ticks");
		check(module.targetHandling().value() == TargetHandling.LATEST_ONLY,
			"new TNT placements must replace unfinished targets by default");
		check(!module.strictInteraction().value(), "strict reach and visibility checks must default to disabled");
		check(!module.keybind().orElseThrow().value().isBound(), "auto ignite shortcut must default to unbound");
	}

	private static void autoIgnitePriorityAndTargetHandlingAreDeterministic() {
		check(
			ItemPriority.FLINT_FIRST.order().equals(List.of(IgnitionItem.FLINT_AND_STEEL, IgnitionItem.FIRE_CHARGE)),
			"flint-first mode must use fire charges only as a fallback"
		);
		check(
			ItemPriority.FIRE_CHARGE_FIRST.order().equals(List.of(IgnitionItem.FIRE_CHARGE, IgnitionItem.FLINT_AND_STEEL)),
			"fire-charge-first mode must use flint only as a fallback"
		);
		check(ItemPriority.FLINT_ONLY.order().equals(List.of(IgnitionItem.FLINT_AND_STEEL)),
			"flint-only mode must not fall back to fire charges");
		check(ItemPriority.FIRE_CHARGE_ONLY.order().equals(List.of(IgnitionItem.FIRE_CHARGE)),
			"fire-charge-only mode must not fall back to flint");
		check(AutoIgniteItemPolicy.shouldRestoreSelection(
			ItemSource.HOTBAR, IgnitionItem.FLINT_AND_STEEL, true
		), "enabled flint restoration must return to the previous TNT slot");
		check(!AutoIgniteItemPolicy.shouldRestoreSelection(
			ItemSource.HOTBAR, IgnitionItem.FLINT_AND_STEEL, false
		), "disabled flint restoration must leave flint selected");
		check(AutoIgniteItemPolicy.shouldRestoreSelection(
			ItemSource.HOTBAR, IgnitionItem.FIRE_CHARGE, false
		), "fire charges must always return to the previous TNT slot");
		check(AutoIgniteItemPolicy.shouldRestoreSelection(
			ItemSource.SILENT_INVENTORY, IgnitionItem.FLINT_AND_STEEL, false
		), "silent inventory swaps must always restore their original slot");

		AutoIgniteTargetQueue targets = new AutoIgniteTargetQueue();
		targets.offer(10L, TargetHandling.QUEUE);
		targets.offer(20L, TargetHandling.QUEUE);
		check(targets.peek().orElseThrow() == 10L && targets.size() == 2,
			"queue mode must preserve TNT placement order");
		targets.offer(30L, TargetHandling.LATEST_ONLY);
		check(targets.peek().orElseThrow() == 30L && targets.size() == 1,
			"latest-only mode must replace all unfinished TNT targets");
		targets.clear();
		targets.offer(40L, TargetHandling.QUEUE);
		targets.offer(40L, TargetHandling.QUEUE);
		check(targets.size() == 2,
			"two confirmed TNT placements at the same block position must remain distinct tasks");
	}

	private static void autoIgniteRotationIsSmoothAndUsesTheShortestPath() {
		check(closeTo(AutoIgniteRotation.interpolateAngle(170.0F, -170.0F, 0.25), 173.125),
			"auto ignite yaw must ease across the wrapped shortest path");
		check(closeTo(AutoIgniteRotation.interpolateLinear(-20.0F, 20.0F, 0.25), -13.75),
			"auto ignite pitch must use the same smooth interpolation curve");
		check(closeTo(AutoIgniteRotation.interpolateAngle(10.0F, 90.0F, 2.0), 90.0),
			"rotation progress must clamp at the target");
		AutoIgniteVisualState.publish(90.0F, -30.0F, true);
		check(AutoIgniteVisualState.snapshot().active(), "third-person preview must become active during rotation");
		check(AutoIgniteVisualState.snapshot().suppressMouseTurn(),
			"camera-follow mode must suppress manual mouse rotation");
		check(closeTo(AutoIgniteVisualState.snapshot().pitch(), -30.0),
			"third-person preview must retain vertical head pitch");
		AutoIgniteVisualState.clear();
		check(!AutoIgniteVisualState.snapshot().active(), "preview state must clear after restoration");
	}

	private static void autoLibrarianTargetsNormalizeAndPersist() {
		Path temporaryDirectory = null;
		try {
			temporaryDirectory = Files.createTempDirectory("edge-client-librarian-target-test");
			Path configPath = temporaryDirectory.resolve("edge-config.json");

			ModuleManager sourceManager = new ModuleManager();
			TrackingModule sourceModule = sourceManager.register(new TrackingModule("librarian_targets"));
			EnchantmentTargetsSetting sourceTargets = sourceModule.exposeRegister(
				new EnchantmentTargetsSetting(
					"targets",
					"client.setting.auto_librarian.targets"
				)
			);
			JsonConfigService sourceConfig = new JsonConfigService(sourceManager, configPath);
			check(sourceConfig.load(), "missing target config must initialize a baseline");

			sourceTargets.set(List.of(
				new EnchantmentTarget("MENDING", 999, false, -4, 70),
				new EnchantmentTarget("minecraft:mending", 2, true, 20, 10),
				new EnchantmentTarget("minecraft:efficiency", 5, true, 12, 24)
			));

			check(sourceTargets.value().size() == 2, "duplicate enchantment IDs must collapse");
			EnchantmentTarget mending = sourceTargets.value().getFirst();
			check(mending.enchantmentId().equals("minecraft:mending"), "IDs must normalize");
			check(mending.level() == 255, "levels must clamp to the persisted domain");
			check(mending.minEmeraldPrice() == 1, "minimum price must clamp");
			check(mending.maxEmeraldPrice() == 64, "maximum price must clamp");
			check(
				Files.readString(configPath).contains("\"enchantmentId\": \"minecraft:efficiency\""),
				"target arrays must save as structured JSON"
			);

			ModuleManager restoredManager = new ModuleManager();
			TrackingModule restoredModule =
				restoredManager.register(new TrackingModule("librarian_targets"));
			EnchantmentTargetsSetting restoredTargets = restoredModule.exposeRegister(
				new EnchantmentTargetsSetting(
					"targets",
					"client.setting.auto_librarian.targets"
				)
			);
			JsonConfigService restoredConfig = new JsonConfigService(restoredManager, configPath);
			check(restoredConfig.load(), "structured target config must load");
			check(
				restoredTargets.value().equals(sourceTargets.value()),
				"target rules must survive config round-trip"
			);
		} catch (IOException error) {
			throw new AssertionError("target persistence test failed", error);
		} finally {
			if (temporaryDirectory != null) {
				try {
					Files.deleteIfExists(temporaryDirectory.resolve("edge-config.json"));
					Files.deleteIfExists(temporaryDirectory);
				} catch (IOException ignored) {
				}
			}
		}
	}

	private static void editorSettingsReceiveReusableGuiControls() {
		SettingControlFactory factory = new SettingControlFactory();
		EnchantmentTargetsSetting targets = new EnchantmentTargetsSetting(
			"targets",
			"client.setting.auto_librarian.targets"
		);

		EditorSettingControl unsupported =
			(EditorSettingControl) factory.create(targets);
		check(
			unsupported instanceof EditorSettingControl,
			"editor settings must automatically receive the reusable editor control"
		);
		check(
			!unsupported.isEnabled(),
			"an unregistered complex editor must be explicit and non-interactive"
		);

		AtomicLong editRequests = new AtomicLong();
		SettingControlFactory registeredFactory = new SettingControlFactory(
			setting -> {},
			setting -> setting.editorId().equals(EnchantmentTargetsSetting.EDITOR_ID),
			setting -> editRequests.incrementAndGet()
		);
		EditorSettingControl registered =
			(EditorSettingControl) registeredFactory.create(targets);
		registered.layout(new Rect(0, 0, 80, 24));
		check(registered.isEnabled(), "registered complex editors must be interactive");
		check(
			registered.handleInput(
				new UiInputEvent.PointerPressed(4, 4, MouseButton.LEFT, 0)
			) == InputResult.CONSUMED,
			"registered editor controls must consume their open action"
		);
		check(editRequests.get() == 1L, "editor open action must delegate once");
	}

	private static void hudPositionsNormalizeLayoutAndPersist() {
		HudPosition normalized = new HudPosition(1.4, -0.2);
		check(closeTo(normalized.x(), 1.0), "HUD x must clamp to the viewport");
		check(closeTo(normalized.y(), 0.0), "HUD y must clamp to the viewport");
		Rect bounds = HudPositionLayout.resolve(normalized, 100, 50, 20, 10);
		check(closeTo(bounds.x(), 80.0), "right anchored HUD must remain fully visible");
		check(closeTo(bounds.y(), 0.0), "top anchored HUD must remain fully visible");

		Path temporaryDirectory = null;
		try {
			temporaryDirectory = Files.createTempDirectory("edge-client-hud-position-test");
			Path configPath = temporaryDirectory.resolve("edge-config.json");

			ModuleManager sourceManager = new ModuleManager();
			TrackingModule source = sourceManager.register(new TrackingModule("hud_position"));
			HudPositionSetting sourcePosition = source.exposeRegister(new HudPositionSetting(
				"position",
				"client.setting.position",
				new HudPosition(0.4, 0.8)
			));
			JsonConfigService sourceConfig = new JsonConfigService(sourceManager, configPath);
			check(sourceConfig.load(), "missing HUD position config must initialize cleanly");
			sourcePosition.set(new HudPosition(0.25, 0.75));

			String json = Files.readString(configPath);
			check(json.contains("\"x\": 0.25"), "HUD x must persist as structured JSON");
			check(json.contains("\"y\": 0.75"), "HUD y must persist as structured JSON");

			ModuleManager restoredManager = new ModuleManager();
			TrackingModule restored = restoredManager.register(new TrackingModule("hud_position"));
			HudPositionSetting restoredPosition = restored.exposeRegister(new HudPositionSetting(
				"position",
				"client.setting.position",
				new HudPosition(0.4, 0.8)
			));
			JsonConfigService restoredConfig = new JsonConfigService(restoredManager, configPath);
			check(restoredConfig.load(), "HUD position config must load");
			check(
				restoredPosition.value().equals(new HudPosition(0.25, 0.75)),
				"HUD position must survive restart"
			);
		} catch (IOException error) {
			throw new AssertionError("HUD position persistence test failed", error);
		} finally {
			if (temporaryDirectory != null) {
				try {
					Files.deleteIfExists(temporaryDirectory.resolve("edge-config.json"));
					Files.deleteIfExists(temporaryDirectory);
				} catch (IOException ignored) {
					// 临时测试目录由操作系统后续清理，不影响功能判断。
				}
			}
		}
	}

	private static void clickGuiAppearanceModuleDeclaresCustomizableDefaults() {
		ClickGuiModule module = new ClickGuiModule();

		check(
			module.category().id().equals("gui"),
			"ClickGUI appearance derives from its gui package"
		);
		check(!module.metadata().toggleable(), "appearance-only module must not expose a fake toggle");
		check(module.settings().size() == 18, "all requested Click GUI values must be GUI settings");
		check(module.language().value().equals("auto"), "language defaults to auto");
		check(module.customFont().value().equals("default"), "customFont defaults to default");
		check(module.armorHudEnabled().value(), "armor HUD enabled defaults to true");
		check(module.potionHudEnabled().value(), "potion HUD enabled defaults to true");
		check(module.arrayListEnabled().value(), "arrayList HUD enabled defaults to true");
		check(
			module.openShortcut().value().keyCode() == 344,
			"Click GUI shortcut must default to right Shift"
		);
		check(module.guiTextScale().value() == 1.0, "Click GUI text scale defaults to 100%");
		check(module.messageBoxScale().value() == 1.0, "message box scale defaults to 100%");
		check(module.messageTextScale().value() == 1.0, "message text scale defaults to 100%");
		check(module.messageOpacity().value() == 0.85, "message box opacity defaults to 85%");
		check(
			module.messageFont().value().equals("minecraft:default"),
			"message font defaults to Minecraft default"
		);
		check(module.messageTextColor() == 0xFFE4E8ED, "message text color defaults to theme text");

		ModuleManager manager = new ModuleManager();
		manager.register(module);
		check(
			manager.setEnabled(module.id(), true) instanceof ModuleChangeResult.Failed,
			"appearance-only module must reject enabled-state changes"
		);
	}

	private static void customFontManagerDirectoryCreationAndScan() {
		io.qzz.iie.font.ClientFontManager.ensureDirectoriesExist();

		List<Path> dirs = io.qzz.iie.font.ClientFontManager.getFontDirectories();
		check(!dirs.isEmpty(), "must have at least one font directory");
		for (Path dir : dirs) {
			check(Files.exists(dir) && Files.isDirectory(dir), "font directory must be automatically created");
		}

		Path primaryDir = dirs.get(0);
		Path testTtf = primaryDir.resolve("HarmonyOS-Bold.TTF");
		Path testOtf = primaryDir.resolve("宋体_Regular.otf");
		Path testTxt = primaryDir.resolve("ignore.txt");

		try {
			Files.writeString(testTtf, "dummy ttf");
			Files.writeString(testOtf, "dummy otf");
			Files.writeString(testTxt, "dummy txt");

			List<String> scanned = io.qzz.iie.font.ClientFontManager.scanAvailableFontFiles();
			check(scanned.contains("HarmonyOS-Bold.TTF"), "scanned must contain HarmonyOS-Bold.TTF");
			check(scanned.contains("宋体_Regular.otf"), "scanned must contain 宋体_Regular.otf");
			check(!scanned.contains("ignore.txt"), "scanned must ignore non-font files");

			List<ChoiceOption<String>> options = io.qzz.iie.font.ClientFontManager.getAvailableFontOptions();
			check(options.stream().anyMatch(o -> o.id().equals("default")), "options must contain default");
			check(options.stream().anyMatch(o -> o.value().equals("HarmonyOS-Bold.TTF")), "options must contain HarmonyOS-Bold.TTF");
			check(options.stream().anyMatch(o -> o.value().equals("宋体_Regular.otf")), "options must contain 宋体_Regular.otf");

			// Verify ChoiceSetting updateOptions accepts these options cleanly without throwing
			ChoiceSetting<String> setting = new ChoiceSetting<>(
				"custom_font",
				"test.font",
				"default",
				options
			);
			setting.updateOptions(options);
			check(setting.options().size() >= 3, "setting must have at least 3 options");

			io.qzz.iie.font.ClientFontManager.applyFont("default");
			check(io.qzz.iie.font.ClientFontManager.getActiveFontDescription() == null, "default font description must be null");
		} catch (IOException e) {
			throw new AssertionError("font scan test failed", e);
		} finally {
			try {
				Files.deleteIfExists(testTtf);
				Files.deleteIfExists(testOtf);
				Files.deleteIfExists(testTxt);
			} catch (IOException ignored) {
			}
		}
	}

	private static void hudPositionDragCommitsOnlyOnRelease() {
		HudPositionSetting setting = new HudPositionSetting(
			"position",
			"client.setting.position",
			new HudPosition(0.5, 0.5)
		);
		AtomicLong changes = new AtomicLong();
		setting.addChangeListener(changes::incrementAndGet);
		HudPositionDrag drag = new HudPositionDrag(setting);
		drag.layout(100, 50, 20, 10);

		check(drag.begin(45, 25), "pressing the HUD preview must start dragging");
		drag.move(90, 40);
		check(changes.get() == 0L, "dragging must not save every pointer movement");
		check(
			drag.draft().x() > 0.5 && drag.draft().y() > 0.5,
			"dragging must update only the preview draft"
		);
		check(drag.end(), "releasing an active drag must commit");
		check(changes.get() == 1L, "drag release must save exactly once");
		check(setting.value().equals(drag.draft()), "committed position must match the preview");

		AtomicLong editRequests = new AtomicLong();
		SettingControlFactory factory = new SettingControlFactory(
			requested -> {
				check(requested == setting, "position editor must receive the same setting");
				editRequests.incrementAndGet();
			}
		);
		HudPositionControl control = (HudPositionControl) factory.create(setting);
		control.layout(new Rect(0, 0, 80, 24));
		check(
			control.handleInput(
				new UiInputEvent.PointerPressed(4, 4, MouseButton.LEFT, 0)
			) == InputResult.CONSUMED,
			"position editor control must consume left click"
		);
		check(editRequests.get() == 1L, "position editor action must be delegated once");
	}

	private static void messageBoxAppearancePreservesAspectRatio() {
		MessageBoxAppearance base = new MessageBoxAppearance(
			1.0,
			1.0,
			0.85,
			0xFFE4E8ED,
			"minecraft:default"
		);
		MessageBoxAppearance enlarged = new MessageBoxAppearance(
			1.5,
			1.25,
			0.6,
			0xFFFFAA33,
			"minecraft:uniform"
		);

		check(
			base.boxWidth() * enlarged.boxHeight()
				== enlarged.boxWidth() * base.boxHeight(),
			"message box scaling must preserve the base aspect ratio"
		);
		check(enlarged.boxWidth() == 330, "150% scale must enlarge width uniformly");
		check(enlarged.boxHeight() == 78, "150% scale must enlarge height uniformly");
	}

	private static void messageBoxApiQueuesAndExpiresMessages() {
		AtomicLong clock = new AtomicLong();
		MessageBoxManager messages = new MessageBoxManager(clock::get, 2);

		messages.show(Component.literal("first"), Duration.ofSeconds(1));
		messages.show(Component.literal("second"), Duration.ofSeconds(1));
		messages.show(Component.literal("third"), Duration.ofSeconds(1));
		check(messages.snapshots().size() == 2, "message queue must discard the oldest overflow");
		check(
			messages.snapshots().getFirst().message().getString().equals("second"),
			"queue must retain the newest messages in order"
		);

		clock.set(Duration.ofMillis(250).toNanos());
		check(messages.snapshots().getFirst().visibility() == 1.0, "message must finish fading in");
		clock.set(Duration.ofSeconds(1).toNanos());
		check(messages.snapshots().isEmpty(), "expired messages must be removed");
		expectThrows(
			IllegalArgumentException.class,
			() -> messages.show(Component.literal("bad"), Duration.ZERO),
			"message duration must be positive"
		);
		expectThrows(
			IllegalArgumentException.class,
			() -> messages.show(
				Component.literal("too long"),
				Duration.ofSeconds(Long.MAX_VALUE)
			),
			"message duration overflow must be rejected consistently"
		);

		MessageBoxApi noop = MessageBoxApi.noop();
		noop.show(Component.literal("safe no-op"));
	}

	private static void moduleStateChangesUseSharedMessageBoxApi() {
		ModuleManager manager = new ModuleManager();
		TrackingModule module = manager.register(new TrackingModule("notified"));
		List<Component> notifications = new java.util.ArrayList<>();
		Runnable unsubscribe = ModuleStateMessageNotifier.attach(
			manager,
			(message, duration) -> notifications.add(message)
		);

		manager.setEnabled(module.id(), true);
		check(notifications.size() == 1, "enabling a module must show one message");
		check(
			notifications.getFirst().getString().contains("client.message.module.enabled"),
			"enable message must use the shared translation key"
		);

		manager.setEnabled(module.id(), true);
		check(notifications.size() == 1, "an unchanged state must not show another message");

		module.failDisable = true;
		manager.setEnabled(module.id(), false);
		check(notifications.size() == 1, "a failed state change must not show a message");

		module.failDisable = false;
		manager.setEnabled(module.id(), false);
		check(notifications.size() == 2, "disabling a module must show one message");
		check(
			notifications.getLast().getString().contains("client.message.module.disabled"),
			"disable message must use the shared translation key"
		);

			unsubscribe.run();
		manager.setEnabled(module.id(), true);
		check(notifications.size() == 2, "unsubscribed notifier must remain silent");
	}

	private static void moduleIdsAreNamespacedAndValidated() {
		ModuleId id = ModuleId.of("client", "aim_assist");

		check(id.toString().equals("client:aim_assist"), "module ID must be namespaced");
		expectThrows(
			IllegalArgumentException.class,
			() -> ModuleId.of("Client", "aim assist"),
			"invalid module ID must fail"
		);
	}

	private static void moduleRegistrationRejectsDuplicateIds() {
		ModuleManager manager = new ModuleManager();
		manager.register(new TrackingModule("speed"));

		expectThrows(
			IllegalArgumentException.class,
			() -> manager.register(new TrackingModule("speed")),
			"duplicate module ID must fail"
		);
	}

	private static void moduleLifecycleIsIdempotentAndRollsBackFailures() {
		ModuleManager manager = new ModuleManager();
		TrackingModule module = new TrackingModule("lifecycle");
		manager.register(module);

		check(manager.setEnabled(module.id(), true) instanceof ModuleChangeResult.Changed, "enable must succeed");
		check(manager.setEnabled(module.id(), true) instanceof ModuleChangeResult.Unchanged, "enable must be idempotent");
		check(module.enableCalls == 1, "onEnable must run once");

		module.failDisable = true;
		check(manager.setEnabled(module.id(), false) instanceof ModuleChangeResult.Failed, "failure must be reported");
		check(module.isEnabled(), "failed disable must roll state back");
	}

	private static void builtInModulesRegisterFullbrightAndAutoWeb() {
		ModuleManager manager = new ModuleManager();

		BuiltInModules.register(manager);

		check(manager.modules().size() == 14, "only production built-ins must be registered in the GUI");
		Module fullbright = manager.find(ModuleId.of("client", "fullbright")).orElseThrow();
		check(
			fullbright.category().id().equals("render"),
			"fullbright derives from its render package"
		);
		Module autoWeb = manager.find(ModuleId.of("client", "auto_web")).orElseThrow();
		check(
			autoWeb.category().id().equals("combat"),
			"auto web derives from its combat package"
		);
		Module autoWalk = manager.find(ModuleId.of("client", "auto_walk")).orElseThrow();
		check(
			autoWalk.category().id().equals("movement"),
			"auto walk derives from its movement package"
		);
		Module safeWalkPlus = manager.find(ModuleId.of("client", "safe_walk_plus")).orElseThrow();
		check(
			safeWalkPlus.category().id().equals("movement"),
			"safe walk plus derives from its movement package"
		);
		check(
			manager.find(ModuleId.of("client", "click_gui")).isPresent(),
			"ClickGUI appearance module must be registered"
		);
		check(
			manager.find(ModuleId.of("client", "better_health_bar")).isPresent(),
			"better health bar must be registered in the GUI"
		);
		check(
			manager.find(ModuleId.of("client", "auto_librarian")).isPresent(),
			"auto librarian must be registered in the GUI"
		);
		check(
			manager.find(ModuleId.of("client", "invert_mouse")).isPresent(),
			"invert mouse must be registered in the GUI"
		);
		check(
			manager.find(ModuleId.of("client", "invert_mouse_pitch")).isPresent(),
			"invert mouse pitch must be registered in the GUI"
		);
		check(
			manager.find(ModuleId.of("client", "special_flip")).isPresent(),
			"special flip must be registered in the GUI"
		);
		check(
			manager.find(ModuleId.of("client", "item_render_mode")).isPresent(),
			"item render mode must be registered in the GUI"
		);
		check(
			manager.find(ModuleId.of("client", "drop_point")).isPresent(),
			"drop point must be registered in the GUI"
		);
		check(
			manager.find(ModuleId.of("client", "explosion_warning")).isPresent(),
			"explosion warning must be registered in the GUI"
		);
		check(
			manager.find(ModuleId.of("client", "auto_ignite")).isPresent(),
			"auto ignite must be registered in the GUI"
		);
	}

	private static void autoWalkModuleMetadataAndLifecycle() {
		AutoWalkModule module = new AutoWalkModule();
		check(
			module.category().id().equals("movement"),
			"auto walk derives from its movement package"
		);
		check(module.metadata().toggleable(), "auto walk must expose a real toggle");
		check(module.settings().size() == 1, "auto walk must declare only its shortcut");
		check(
			!module.keybind().orElseThrow().value().isBound(),
			"auto walk shortcut must default unbound"
		);
	}

	private static void invertMouseDeclaresDefaultsAndHooksFollowLifecycle() {
		InvertMouseModule horizontal = new InvertMouseModule();
		check(
			horizontal.category().id().equals("input"),
			"invert mouse derives from its input package"
		);
		check(horizontal.metadata().toggleable(), "invert mouse must expose a real toggle");
		check(horizontal.settings().size() == 1, "invert mouse must declare only its shortcut");
		check(
			!horizontal.keybind().orElseThrow().value().isBound(),
			"invert mouse shortcut must default unbound"
		);

		InvertMousePitchModule vertical = new InvertMousePitchModule();
		check(
			vertical.category().id().equals("input"),
			"invert mouse pitch derives from its input package"
		);
		check(
			vertical.metadata().toggleable(),
			"invert mouse pitch must expose a real toggle"
		);
		check(
			vertical.settings().size() == 1,
			"invert mouse pitch must declare only its shortcut"
		);
		check(
			!vertical.keybind().orElseThrow().value().isBound(),
			"invert mouse pitch shortcut must default unbound"
		);

		check(
			!InvertMouseHooks.shouldInvertHorizontal(),
			"uninstalled horizontal hooks must stay inert"
		);
		check(
			!InvertMouseHooks.shouldInvertVertical(),
			"uninstalled vertical hooks must stay inert"
		);
		InvertMouseHooks.install(horizontal);
		InvertMouseHooks.installPitch(vertical);
		check(
			!InvertMouseHooks.shouldInvertHorizontal(),
			"a disabled module must not invert horizontal mouse look"
		);
		check(
			!InvertMouseHooks.shouldInvertVertical(),
			"a disabled module must not invert vertical mouse look"
		);

		ModuleManager manager = new ModuleManager();
		manager.register(horizontal);
		manager.register(vertical);
		manager.setEnabled(horizontal.id(), true);
		check(
			InvertMouseHooks.shouldInvertHorizontal(),
			"enabling the module must invert horizontal mouse look"
		);
		check(
			!InvertMouseHooks.shouldInvertVertical(),
			"horizontal enablement must not invert vertical mouse look"
		);
		manager.setEnabled(horizontal.id(), false);
		check(
			!InvertMouseHooks.shouldInvertHorizontal(),
			"disabling the module must stop inverting horizontal mouse look"
		);

		manager.setEnabled(vertical.id(), true);
		check(
			InvertMouseHooks.shouldInvertVertical(),
			"enabling the pitch module must invert vertical mouse look"
		);
		check(
			!InvertMouseHooks.shouldInvertHorizontal(),
			"vertical enablement must not invert horizontal mouse look"
		);
		manager.setEnabled(vertical.id(), false);
		check(
			!InvertMouseHooks.shouldInvertVertical(),
			"disabling the pitch module must stop inverting vertical mouse look"
		);
	}

	private static void specialFlipDeclaresDefaultsAndHooksFollowLifecycle() {
		SpecialFlipModule module = new SpecialFlipModule();
		check(
			module.category().id().equals("input"),
			"special flip derives from its input package"
		);
		check(
			module.metadata().toggleable(),
			"special flip must expose a real toggle"
		);
		check(
			module.settings().size() == 1,
			"special flip must declare only its shortcut"
		);
		check(
			!module.keybind().orElseThrow().value().isBound(),
			"special flip shortcut must default unbound"
		);

		check(
			!SpecialFlipHooks.shouldApply(),
			"uninstalled special flip hooks must stay inert"
		);
		SpecialFlipHooks.install(module);
		check(
			!SpecialFlipHooks.shouldApply(),
			"a disabled special flip module must not apply"
		);

		ModuleManager manager = new ModuleManager();
		manager.register(module);
		manager.setEnabled(module.id(), true);
		check(
			SpecialFlipHooks.shouldApply(),
			"enabling special flip must apply the rotated mapping"
		);
		manager.setEnabled(module.id(), false);
		check(
			!SpecialFlipHooks.shouldApply(),
			"disabling special flip must stop applying the rotated mapping"
		);
	}

	private static void autoLibrarianDeclaresOriginalSettingsAndDefaults() {
		AutoLibrarianModule module = new AutoLibrarianModule(MessageBoxApi.noop());

		check(
			module.category().id().equals("player"),
			"auto librarian derives from its player package"
		);
		check(module.targets().value().size() == 1, "one historical target must be declared");
		check(
			module.targets().value().getFirst().equals(
				new EnchantmentTarget("minecraft:unbreaking", 3, false, 1, 64)
			),
			"historical unbreaking III target must be the default"
		);
		check(module.searchRadius().value() == 3.0, "search radius must default to 3");
		check(module.placementRadius().value() == 2.0, "placement radius must default to 2");
		check(module.allowHandMining().value(), "hand mining must default on");
		check(module.reportTrades().value(), "trade reporting must default on");
		check(!module.autoRecycle().value(), "automatic lectern recovery must default off");
		check(module.rotationTicks().value() == 6.0, "rotation must default to 6 ticks");
		check(!module.keybind().orElseThrow().value().isBound(), "shortcut must be unbound");
	}

	private static void betterHealthBarDeclaresDefaultsAndCapsExtraRows() {
		BetterHealthBarModule module = new BetterHealthBarModule();

		check(
			module.category().id().equals("render"),
			"better health bar derives from its render package"
		);
		check(module.thresholdRows().value() == 2, "health threshold must default to two rows");
		check(module.numberScale().value() == 1.0, "health number size must default to 100%");
		check(module.settings().size() == 3, "module must expose threshold, position and size");

		check(
			!BetterHealthBarPolicy.shouldShowNumber(true, 2, 40.0F),
			"number must stay hidden at the configured threshold"
		);
		check(
			BetterHealthBarPolicy.shouldShowNumber(true, 2, 42.0F),
			"number must appear above the configured threshold"
		);
		check(
			BetterHealthBarPolicy.shouldShowNumber(true, -1, 20.0F),
			"-1 must always show the number"
		);
		check(
			closeTo(BetterHealthBarPolicy.visibleMaximumHealth(true, 2, 60.0F), 40.0),
			"extra maximum-health rows must be hidden"
		);
		check(
			closeTo(BetterHealthBarPolicy.visibleMaximumHealth(true, -1, 60.0F), 60.0),
			"-1 must not hide vanilla hearts"
		);
		check(
			BetterHealthBarPolicy.formatHealth(19.0F).equals("19")
				&& BetterHealthBarPolicy.formatHealth(19.5F).equals("19.5"),
			"health number must avoid an unnecessary decimal"
		);

		ModuleManager manager = new ModuleManager();
		manager.register(module);
		BetterHealthBarHooks.install(module);
		check(
			closeTo(BetterHealthBarHooks.clampMaximumHealth(60.0F), 60.0),
			"disabled module hook must preserve vanilla maximum health"
		);
		manager.setEnabled(module.id(), true);
		check(
			closeTo(BetterHealthBarHooks.clampMaximumHealth(60.0F), 40.0),
			"enabled module hook must apply the configured row cap"
		);
	}

	private static void itemRenderModeDeclaresDefaultsAndHooksFollowLifecycle() {
		ItemRenderModeModule module = new ItemRenderModeModule();

		check(
			module.category().id().equals("render"),
			"item render mode derives from its render package"
		);
		check(module.metadata().toggleable(), "item render mode must expose a real toggle");
		check(
			module.renderModeSetting().value() == ItemRenderMode.BILLBOARD,
			"2D billboard must be the default mode"
		);
		check(module.settings().size() == 2, "module must declare mode and shortcut");
		check(
			!module.keybind().orElseThrow().value().isBound(),
			"item render mode shortcut must default unbound"
		);

		check(
			ItemRenderModeHooks.renderMode() == ItemRenderMode.VANILLA,
			"uninstalled hooks must stay inert"
		);
		ItemRenderModeHooks.install(module);
		check(
			ItemRenderModeHooks.renderMode() == ItemRenderMode.VANILLA,
			"a disabled module must not modify item rendering"
		);

		ModuleManager manager = new ModuleManager();
		manager.register(module);
		manager.setEnabled(module.id(), true);
		check(
			ItemRenderModeHooks.renderMode() == ItemRenderMode.BILLBOARD,
			"enabling must apply the 2D billboard mode"
		);

		module.renderModeSetting().set(ItemRenderMode.FREEZE_ROTATION);
		check(
			ItemRenderModeHooks.renderMode() == ItemRenderMode.FREEZE_ROTATION,
			"switching the mode must apply freeze rotation"
		);

		UUID kept = UUID.randomUUID();
		UUID dropped = UUID.randomUUID();
		ItemRenderModeRenderState first = new ItemRenderModeRenderState();
		ItemRenderModeHooks.applyFrozenSpin(kept, 2.5F, first);
		check(
			first.frozenSpin != null && closeTo(first.frozenSpin, 2.5),
			"first sight must freeze the current angle"
		);
		ItemRenderModeRenderState second = new ItemRenderModeRenderState();
		ItemRenderModeHooks.applyFrozenSpin(kept, 9.0F, second);
		check(
			closeTo(second.frozenSpin, 2.5),
			"later frames must reuse the first frozen angle"
		);

		ItemRenderModeRenderState stale = new ItemRenderModeRenderState();
		ItemRenderModeHooks.applyFrozenSpin(dropped, 4.0F, stale);
		ItemRenderModeHooks.pruneFrozenAngles(List.of(kept));
		ItemRenderModeRenderState surviving = new ItemRenderModeRenderState();
		ItemRenderModeHooks.applyFrozenSpin(kept, 7.0F, surviving);
		check(
			closeTo(surviving.frozenSpin, 2.5),
			"a live entity must keep its frozen angle after pruning"
		);
		ItemRenderModeRenderState recaptured = new ItemRenderModeRenderState();
		ItemRenderModeHooks.applyFrozenSpin(dropped, 3.0F, recaptured);
		check(
			closeTo(recaptured.frozenSpin, 3.0),
			"a pruned entity must re-freeze at its current angle"
		);

		module.renderModeSetting().set(ItemRenderMode.BILLBOARD);
		ItemRenderModeRenderState nonFrozen = new ItemRenderModeRenderState();
		ItemRenderModeHooks.applyFrozenSpin(UUID.randomUUID(), 5.0F, nonFrozen);
		check(
			nonFrozen.frozenSpin == null,
			"non-freeze modes must not carry frozen angles into rendering"
		);

		manager.setEnabled(module.id(), false);
		check(
			ItemRenderModeHooks.renderMode() == ItemRenderMode.VANILLA,
			"disabling must restore vanilla item rendering"
		);
	}

	private static void autoWebDeclaresConfirmedDefaults() {
		AutoWebModule module = new AutoWebModule();

		check(module.settings().size() == 11, "auto web must expose every confirmed setting");
		check(module.targetPriority().value() == TargetPriority.NEAREST, "nearest target must be default");
		check(module.targetType().value() == TargetType.PLAYER, "players must be the default target type");
		check(module.placementPattern().value() == PlacementPattern.FEET, "feet-only must be default");
		check(module.range().value() == 3.0, "range must default to 3 blocks");
		check(module.rotationTicks().value() == 2.0, "rotation must default to 2 ticks");
		check(module.hotbarMode().value() == HotbarMode.SILENT, "silent hotbar switching must be default");
		check(!module.checkInventory().value(), "inventory search must be opt-in");
		check(
			module.inventoryMode().value() == InventoryMode.SILENT_SELECTED_RESTORE,
			"silent selected-slot inventory swap must be default"
		);
		check(
			module.placementCadence().value() == PlacementCadence.ONE_PER_ROTATION,
			"natural one-per-rotation placement must be default"
		);
		check(module.placementInterval().value() == 1.0, "placement interval must default to 1 tick");
		check(module.keybind().orElseThrow().value().isBound() == false, "shortcut must default unbound");
	}

	private static void autoWebPlacementPatternsAreDeterministic() {
		BlockCell feet = new BlockCell(10, 64, -4);

		check(
			AutoWebPlanner.placementCells(feet, PlacementPattern.FEET).equals(List.of(feet)),
			"feet pattern must contain only the target feet"
		);
		check(
			AutoWebPlanner.placementCells(feet, PlacementPattern.FEET_AND_HEAD).equals(
				List.of(feet, new BlockCell(10, 65, -4))
			),
			"feet-and-head pattern must preserve placement order"
		);
		check(
			AutoWebPlanner.placementCells(feet, PlacementPattern.SURROUND).equals(
				List.of(
					feet,
					new BlockCell(10, 65, -4),
					new BlockCell(10, 64, -5),
					new BlockCell(10, 64, -3),
					new BlockCell(11, 64, -4),
					new BlockCell(9, 64, -4)
				)
			),
			"surround pattern must include feet, head and four horizontal neighbors"
		);
	}

	private static void autoWebTargetPrioritiesSelectExpectedCandidate() {
		TargetSnapshot nearest = new TargetSnapshot(1, 1.0, 18.0, 0.4);
		TargetSnapshot weakest = new TargetSnapshot(2, 9.0, 2.0, 0.3);
		TargetSnapshot aimed = new TargetSnapshot(3, 4.0, 10.0, 0.01);
		List<TargetSnapshot> candidates = List.of(weakest, aimed, nearest);

		check(
			AutoWebPlanner.selectTarget(candidates, TargetPriority.NEAREST).orElseThrow() == nearest,
			"nearest priority must select the shortest distance"
		);
		check(
			AutoWebPlanner.selectTarget(candidates, TargetPriority.LOWEST_HEALTH).orElseThrow() == weakest,
			"health priority must select the lowest health"
		);
		check(
			AutoWebPlanner.selectTarget(candidates, TargetPriority.CROSSHAIR).orElseThrow() == aimed,
			"crosshair priority must select the smallest aim error"
		);
	}

	private static void autoWebRotationUsesShortestWrappedPath() {
		check(
			closeTo(AutoWebRotation.interpolateAngle(170.0F, -170.0F, 0.5), 180.0),
			"rotation interpolation must cross the wrapped boundary by the shortest path"
		);
		check(
			closeTo(AutoWebRotation.interpolateAngle(-30.0F, 30.0F, 0.25), -15.0),
			"ordinary rotation interpolation must remain linear"
		);
	}

	private static void onlyEnabledModulesReceiveClientTicks() {
		ModuleManager manager = new ModuleManager();
		TrackingModule module = manager.register(new TrackingModule("tick"));

		manager.tickEnabledModules();
		check(module.tickCalls == 0, "disabled modules must not receive client ticks");

		manager.setEnabled(module.id(), true);
		manager.tickEnabledModules();
		check(module.tickCalls == 1, "enabled modules must receive client ticks");

		manager.setEnabled(module.id(), false);
		manager.tickEnabledModules();
		check(module.tickCalls == 1, "disabled modules must stop receiving client ticks");
	}

	private static void modulesCanRequestSafeDisableAfterTheirTick() {
		ModuleManager manager = new ModuleManager();
		SelfStoppingModule module =
			manager.register(new SelfStoppingModule("self_stopping"));
		manager.setEnabled(module.id(), true);

		manager.tickEnabledModules();

		check(!module.isEnabled(), "a module disable request must run after its tick");
		check(module.disableCalls == 1, "a requested disable must use the normal lifecycle");
	}

	private static void settingsNormalizeValuesAndRejectDuplicateIds() {
		TrackingModule module = new TrackingModule("settings");
		DoubleSetting speed = module.exposedSpeed();
		BooleanSetting visible = module.exposedVisible();

		speed.set(0.63);
		check(Math.abs(speed.value() - 0.65) < 0.000_001, "double setting must align to step");
		speed.set(4.0);
		check(speed.value() == 1.0, "double setting must clamp to maximum");
		check(visible.value(), "boolean default must be retained");

		expectThrows(
			IllegalArgumentException.class,
			() -> module.exposeRegister(new BooleanSetting("visible", "duplicate", false)),
			"duplicate setting ID must fail"
		);
	}

	private static void choiceSettingsValidateOptionsAndSelection() {
		ChoiceSetting<TestTarget> target = new ChoiceSetting<>(
			"target",
			"client.setting.target",
			TestTarget.PLAYER,
			List.of(
				new ChoiceOption<>("player", "client.option.player", TestTarget.PLAYER),
				new ChoiceOption<>("hostile", "client.option.hostile", TestTarget.HOSTILE)
			)
		);

		check(target.value() == TestTarget.PLAYER, "choice setting must retain its default");
		target.set(TestTarget.HOSTILE);
		check(target.selectedOption().id().equals("hostile"), "selected option must follow the value");
		expectThrows(
			IllegalArgumentException.class,
			() -> target.set(TestTarget.FRIENDLY),
			"choice setting must reject values outside its options"
		);
		expectThrows(
			IllegalArgumentException.class,
			() -> new ChoiceSetting<>(
				"duplicate",
				"duplicate",
				TestTarget.PLAYER,
				List.of(
					new ChoiceOption<>("same", "first", TestTarget.PLAYER),
					new ChoiceOption<>("same", "second", TestTarget.HOSTILE)
				)
			),
			"choice option IDs must be unique"
		);
	}

	private static void choiceControlExpandsSelectsAndCancels() {
		ChoiceSetting<TestTarget> target = new ChoiceSetting<>(
			"target",
			"client.setting.target",
			TestTarget.PLAYER,
			List.of(
				new ChoiceOption<>("player", "client.option.player", TestTarget.PLAYER),
				new ChoiceOption<>("hostile", "client.option.hostile", TestTarget.HOSTILE)
			)
		);
		ChoiceControl control = new ChoiceControl(target);
		control.layout(new Rect(0, 0, 100, 20), 20);

		control.handleInput(new UiInputEvent.PointerPressed(10, 10, MouseButton.LEFT, 0));
		check(control.isExpanded(), "choice control must expand on left click");
		check(control.inputBounds().height() == 60.0, "expanded input bounds must include options");
		double drawerProgress = control.advanceAnimation(0.08);
		check(
			drawerProgress > 0.0 && drawerProgress < 1.0,
			"choice drawer must animate instead of appearing instantly"
		);

		control.handleInput(new UiInputEvent.PointerPressed(10, 50, MouseButton.LEFT, 0));
		check(target.value() == TestTarget.HOSTILE, "clicking an option must update the setting");
		check(!control.isExpanded(), "choice control must collapse after selection");

		control.handleInput(new UiInputEvent.PointerPressed(10, 10, MouseButton.LEFT, 0));
		control.handleInput(new UiInputEvent.KeyPressed(256, 0, 0));
		check(!control.isExpanded(), "Escape must collapse the choice drawer");
		check(target.value() == TestTarget.HOSTILE, "Escape must preserve the selected value");
	}

	private static void choiceControlCollapsesWhenFocusMoves() {
		ChoiceSetting<TestTarget> target = testTargetSetting();
		ChoiceControl choice = new ChoiceControl(target);
		choice.layout(new Rect(0, 0, 100, 20), 20);
		RecordingTarget other = new RecordingTarget(new Rect(150, 0, 50, 20));
		UiInputRouter router = new UiInputRouter();

		router.route(
			new UiInputEvent.PointerPressed(10, 10, MouseButton.LEFT, 0),
			List.of(choice, other)
		);
		check(choice.isExpanded(), "clicking the choice must focus and expand it");

		router.route(
			new UiInputEvent.PointerPressed(160, 10, MouseButton.LEFT, 0),
			List.of(choice, other)
		);
		check(!choice.isExpanded(), "moving focus to another control must close the drawer");
	}

	private static void choiceHoverHighlightsOnlyDrawerOptions() {
		ChoiceControl choice = new ChoiceControl(testTargetSetting());
		choice.layout(new Rect(0, 0, 100, 20), 20);
		UiInputRouter router = new UiInputRouter();

		router.route(
			new UiInputEvent.PointerPressed(10, 10, MouseButton.LEFT, 0),
			List.of(choice)
		);
		router.route(new UiInputEvent.PointerMoved(10, 50), List.of(choice));
		check(
			choice.hoveredOptionIndex() == 1,
			"hovering a drawer option must expose that option as highlighted"
		);

		router.route(new UiInputEvent.PointerMoved(10, 10), List.of(choice));
		check(
			choice.hoveredOptionIndex() == -1,
			"the collapsed header must not be treated as an option highlight"
		);

		router.route(new UiInputEvent.PointerMoved(10, 30), List.of(choice));
		check(choice.hoveredOptionIndex() == 0, "hover must be restorable before leaving");
		check(choice.coversDrawer(10, 30), "expanded drawer must report overlay coverage");
		router.route(new UiInputEvent.PointerMoved(140, 50), List.of(choice));
		check(
			choice.hoveredOptionIndex() == -1,
			"moving outside the choice bounds must clear option highlighting"
		);
		check(!choice.coversDrawer(140, 50), "outside points must not cover the drawer");
	}

	private static void keybindControlConfirmsCancelsAndClears() {
		KeybindSetting keybind = new KeybindSetting("keybind", "client.setting.keybind");
		KeybindControl control = new KeybindControl(keybind);
		control.layout(new Rect(0, 0, 100, 24));

		check(!keybind.value().isBound(), "module keybinds must default to unbound");
		control.handleInput(new UiInputEvent.PointerPressed(10, 10, MouseButton.LEFT, 0));
		check(control.isListening(), "left click must begin key listening");
		control.handleInput(new UiInputEvent.KeyPressed(82, 19, 0));
		check(keybind.value().equals(new KeybindValue(82)), "ordinary keys must confirm the binding");
		check(!control.isListening(), "binding a key must finish listening");

		control.handleInput(new UiInputEvent.PointerPressed(10, 10, MouseButton.LEFT, 0));
		control.handleInput(new UiInputEvent.KeyPressed(256, 0, 0));
		check(keybind.value().equals(new KeybindValue(82)), "Escape must preserve the previous binding");

		control.handleInput(new UiInputEvent.PointerPressed(10, 10, MouseButton.LEFT, 0));
		control.handleInput(new UiInputEvent.KeyPressed(259, 0, 0));
		check(!keybind.value().isBound(), "Backspace must clear the binding");
	}

	private static void clickGuiShortcutPersistsFromControl() {
		Path temporaryDirectory = null;
		try {
			temporaryDirectory = Files.createTempDirectory("edge-client-gui-key-test");
			Path configPath = temporaryDirectory.resolve("edge-config.json");

			ModuleManager sourceManager = new ModuleManager();
			ClickGuiModule source = sourceManager.register(new ClickGuiModule());
			JsonConfigService sourceConfig = new JsonConfigService(sourceManager, configPath);
			check(sourceConfig.load(), "missing Click GUI config must initialize a clean baseline");

			KeybindControl control = new KeybindControl(source.openShortcut());
			control.layout(new Rect(0, 0, 100, 24));
			control.handleInput(new UiInputEvent.PointerPressed(10, 10, MouseButton.LEFT, 0));
			control.handleInput(new UiInputEvent.KeyPressed(82, 19, 0));

			check(Files.isRegularFile(configPath), "binding the GUI shortcut must save immediately");
			check(
				Files.readString(configPath).contains("\"open_shortcut\": 82"),
				"the GUI shortcut must be encoded in Edge config"
			);

			ModuleManager restoredManager = new ModuleManager();
			ClickGuiModule restored = restoredManager.register(new ClickGuiModule());
			JsonConfigService restoredConfig = new JsonConfigService(restoredManager, configPath);
			check(restoredConfig.load(), "saved Click GUI shortcut must load successfully");
			check(
				restored.openShortcut().value().keyCode() == 82,
				"the Click GUI shortcut must survive restart"
			);
		} catch (IOException error) {
			throw new AssertionError("Click GUI shortcut persistence test failed", error);
		} finally {
			if (temporaryDirectory != null) {
				try {
					Files.deleteIfExists(temporaryDirectory.resolve("edge-config.json"));
					Files.deleteIfExists(temporaryDirectory);
				} catch (IOException ignored) {
					// 临时测试目录由操作系统后续清理，不影响功能判断。
				}
			}
		}
	}

	private static void moduleShortcutsUsePressEdgesAndIgnoreOpenScreens() {
		ModuleManager manager = new ModuleManager();
		KeyboundModule module = manager.register(new KeyboundModule("shortcut", 82));
		ModuleShortcutDispatcher dispatcher = new ModuleShortcutDispatcher(manager);

		dispatcher.update(false, key -> key == 82);
		dispatcher.update(true, key -> key == 82);
		check(!module.isEnabled(), "a key held while a screen closes must not toggle a module");

		dispatcher.update(true, key -> false);
		dispatcher.update(true, key -> key == 82);
		check(module.isEnabled(), "a fresh gameplay key press must toggle the module");

		dispatcher.update(true, key -> key == 82);
		check(module.isEnabled(), "holding a shortcut must not toggle it repeatedly");

		dispatcher.update(true, key -> false);
		dispatcher.update(true, key -> key == 82);
		check(!module.isEnabled(), "a second press edge must toggle the module off");
	}

	private static void keybindActionsUsePressEdgesAndIgnoreOpenScreens() {
		KeybindSetting setting = new KeybindSetting(
			"action",
			"client.setting.action",
			new KeybindValue(82)
		);
		KeybindActionDispatcher dispatcher = new KeybindActionDispatcher(setting);

		check(!dispatcher.update(false, key -> key == 82), "open screens must suppress actions");
		check(
			!dispatcher.update(true, key -> key == 82),
			"a held key must not trigger when returning to gameplay"
		);
		check(!dispatcher.update(true, key -> false), "key release must not trigger an action");
		check(dispatcher.update(true, key -> key == 82), "a fresh press edge must trigger once");
		check(!dispatcher.update(true, key -> key == 82), "holding a key must not repeat actions");
	}

	private static void extensionsRegisterLogicOnlyModulesThroughContext() {
		ModuleManager manager = new ModuleManager();
		MessageBoxManager messages = new MessageBoxManager();
		EdgeClientExtension extension = context -> {
			context.registerModule(new TrackingModule("extension"));
			context.messages().show(Component.literal("extension ready"));
		};
		EdgeClientExtensionContext context = new EdgeClientExtensionContext() {
			@Override
			public void registerModule(Module module) {
				manager.register(module);
			}

			@Override
			public MessageBoxApi messages() {
				return messages;
			}
		};

		extension.initialize(context);

		check(manager.modules().size() == 1, "extension must register through the provided context");
		check(
			messages.snapshots().size() == 1,
			"extension context must expose the shared message box API"
		);
	}

	private static void inputRouterDistinguishesRightClickAndCapturesDrag() {
		DoubleSetting value = new DoubleSetting("drag", "drag", 0.0, 0.0, 1.0, 0.1);
		SliderControl slider = new SliderControl(value);
		slider.layout(new Rect(10, 10, 100, 20));
		RecordingTarget rightClickTarget = new RecordingTarget(new Rect(0, 0, 8, 8));
		UiInputRouter router = new UiInputRouter();

		check(
			router.route(
				new UiInputEvent.PointerPressed(4, 4, MouseButton.RIGHT, 0),
				List.of(rightClickTarget, slider)
			),
			"right click must be consumed"
		);
		check(rightClickTarget.lastButton == MouseButton.RIGHT, "right button identity must be preserved");

		router.route(
			new UiInputEvent.PointerPressed(35, 15, MouseButton.LEFT, 0),
			List.of(rightClickTarget, slider)
		);
		check(Math.abs(value.value() - 0.3) < 0.000_001, "press must update slider value");

		router.route(
			new UiInputEvent.PointerDragged(150, 15, 115, 0, MouseButton.LEFT, 0),
			List.of()
		);
		check(value.value() == 1.0, "captured drag must continue outside slider bounds");

		router.route(
			new UiInputEvent.PointerReleased(150, 15, MouseButton.RIGHT, 0),
			List.of()
		);
		check(router.hasPointerCapture(), "releasing another button must preserve pointer capture");

		router.route(
			new UiInputEvent.PointerReleased(150, 15, MouseButton.LEFT, 0),
			List.of()
		);
		check(!router.hasPointerCapture(), "release must clear pointer capture");
	}

	private static void slidersCanWriteCanonicalNestedValues() {
		double[] canonicalValue = {1.0};
		SliderControl slider = new SliderControl(new RangedDoubleBinding() {
			@Override
			public Double get() {
				return canonicalValue[0];
			}

			@Override
			public BindingUpdateResult set(Double value) {
				canonicalValue[0] = value;
				return new BindingUpdateResult.Accepted();
			}

			@Override
			public double minimum() {
				return 1.0;
			}

			@Override
			public double maximum() {
				return 64.0;
			}

			@Override
			public double step() {
				return 1.0;
			}
		});
		slider.layout(new Rect(0, 0, 63, 12));
		slider.handleInput(new UiInputEvent.PointerPressed(31.5, 6, MouseButton.LEFT, 0));

		check(canonicalValue[0] == 33.0, "generic sliders must update their canonical bound value");
		check(closeTo(slider.fraction(), 32.0 / 63.0), "generic sliders must expose their live fraction");
	}

	private static void autoLibrarianSearchUsesNativeImeInput() {
		try {
			check(
				AutoLibrarianTargetEditorScreen.class
					.getDeclaredField("searchBox")
					.getType() == EditBox.class,
				"Auto Librarian search must use Minecraft's native IME-aware EditBox"
			);
		} catch (NoSuchFieldException error) {
			throw new AssertionError("Auto Librarian search must declare a native searchBox", error);
		}
	}

	private static void moduleBindingPreservesManagerLifecycle() {
		ModuleManager manager = new ModuleManager();
		TrackingModule module = manager.register(new TrackingModule("binding"));
		ModuleEnabledBinding binding = new ModuleEnabledBinding(manager, module.id());

		check(binding.set(true).accepted(), "binding must enable through manager");
		check(module.isEnabled(), "module must be enabled");
		check(module.enableCalls == 1, "binding must preserve lifecycle callback");
	}

	private static void moduleControlsShareLiveManagerState() {
		ModuleManager manager = new ModuleManager();
		TrackingModule module = manager.register(new TrackingModule("shared_controls"));
		SettingControlFactory factory = new SettingControlFactory();
		ToggleControl outerToggle = factory.createModuleEnabled(manager, module.id());
		ToggleControl settingsToggle = factory.createModuleEnabled(manager, module.id());

		outerToggle.handleInput(new UiInputEvent.PointerPressed(0, 0, MouseButton.LEFT, 0));
		check(settingsToggle.value(), "settings toggle must observe the module-list toggle");

		settingsToggle.handleInput(new UiInputEvent.PointerPressed(0, 0, MouseButton.LEFT, 0));
		check(!outerToggle.value(), "module-list toggle must observe the settings toggle");

		manager.setEnabled(module.id(), true);
		check(outerToggle.value() && settingsToggle.value(), "both controls must observe external state changes");
		double progress = settingsToggle.advanceAnimation(0.09);
		check(progress > 0.0 && progress < 1.0, "externally changed module toggles must animate");
	}

	private static void unsupportedSettingsDegradeWithoutCrashingTheGui() {
		SettingControlFactory factory = new SettingControlFactory();

		check(
			factory.create(testTargetSetting()) instanceof ChoiceControl,
			"choice settings must receive a reusable choice control"
		);
		check(
			factory.create(new KeybindSetting("keybind", "keybind")) instanceof KeybindControl,
			"keybind settings must receive a reusable keybind control"
		);
		check(
			factory.create(new HudPositionSetting(
				"position",
				"client.setting.position",
				new HudPosition(0.5, 0.5)
			)) instanceof HudPositionControl,
			"HUD positions must receive the reusable editor control"
		);
		check(
			factory.create(new TextSetting()) instanceof UnsupportedControl,
			"unknown setting types must render an explicit unsupported control"
		);
	}

	private static void animationProgressesAndRetargetsWithoutJumping() {
		AnimatedDouble animation = new AnimatedDouble(
			0.0,
			new AnimationSpec(1.0, Easing.LINEAR)
		);

		animation.animateTo(1.0);
		check(closeTo(animation.advance(0.25), 0.25), "animation must advance by elapsed time");

		animation.animateTo(0.0);
		check(closeTo(animation.value(), 0.25), "retargeting must not jump");
		check(closeTo(animation.advance(0.25), 0.1875), "retargeting must continue from current value");

		animation.advance(10.0);
		check(closeTo(animation.value(), 0.0), "animation must finish exactly at its target");
		check(!animation.isRunning(), "finished animation must stop running");
	}

	private static void smoothScrollAccumulatesAndReversesWithoutJumping() {
		AnimatedScroll scroll = new AnimatedScroll(
			new AnimationSpec(1.0, Easing.LINEAR)
		);
		scroll.setMaximum(100.0);

		scroll.scrollBy(60.0);
		check(closeTo(scroll.value(), 0.0), "wheel input must not jump the visible offset");
		check(closeTo(scroll.target(), 60.0), "wheel input must accumulate in the target");
		check(closeTo(scroll.advance(0.25), 15.0), "scrolling down must interpolate");

		scroll.scrollBy(-30.0);
		check(closeTo(scroll.value(), 15.0), "reversing direction must not jump");
		check(closeTo(scroll.target(), 30.0), "reverse input must retarget from the destination");
		check(closeTo(scroll.advance(0.5), 22.5), "reverse scrolling must stay smooth");

		scroll.scrollBy(1_000.0);
		check(closeTo(scroll.target(), 100.0), "downward scrolling must clamp to the end");
		scroll.advance(1.0);
		scroll.scrollBy(-1_000.0);
		check(closeTo(scroll.target(), 0.0), "upward scrolling must clamp to the start");
	}

	private static void animatedGeometryAndColorInterpolateDeterministically() {
		AnimatedRect rect = new AnimatedRect(
			new Rect(0, 10, 100, 20),
			new AnimationSpec(1.0, Easing.LINEAR)
		);
		rect.animateTo(new Rect(20, 30, 120, 40));
		Rect halfway = rect.advance(0.5);

		check(closeTo(halfway.x(), 10.0), "animated rect x must interpolate");
		check(closeTo(halfway.y(), 20.0), "animated rect y must interpolate");
		check(closeTo(halfway.width(), 110.0), "animated rect width must interpolate");
		check(closeTo(halfway.height(), 30.0), "animated rect height must interpolate");
		check(
			ArgbColor.interpolate(0xFF000000, 0xFFFFFFFF, 0.5) == 0xFF808080,
			"ARGB interpolation must blend every channel"
		);
	}

	private static void toggleAnimationFollowsItsBinding() {
		BooleanSetting enabled = new BooleanSetting("animated", "animated", false);
		io.qzz.iie.ui.component.control.ToggleControl toggle =
			new io.qzz.iie.ui.component.control.ToggleControl(enabled);
		toggle.layout(new Rect(0, 0, 42, 22));

		toggle.handleInput(new UiInputEvent.PointerPressed(10, 10, MouseButton.LEFT, 0));
		double halfway = toggle.advanceAnimation(0.09);

		check(halfway > 0.0 && halfway < 1.0, "toggle must animate instead of jumping");
		check(closeTo(toggle.advanceAnimation(1.0), 1.0), "toggle animation must reach enabled state");
	}

	private static void animationClockUsesElapsedTimeAndClampsLongFrames() {
		AtomicLong now = new AtomicLong(1_000_000_000L);
		AnimationFrameClock clock = new AnimationFrameClock(now::get);

		check(closeTo(clock.nextDeltaSeconds(), 0.0), "first frame must not jump");
		now.addAndGet(10_000_000L);
		check(closeTo(clock.nextDeltaSeconds(), 0.01), "clock must report elapsed seconds");
		now.addAndGet(1_000_000_000L);
		check(closeTo(clock.nextDeltaSeconds(), 0.05), "long frames must be clamped");
	}

	private static void animationRejectsNonFiniteInputsAndEasingOutputs() {
		AnimatedDouble invalidEasing = new AnimatedDouble(
			0.0,
			new AnimationSpec(1.0, progress -> Double.NaN)
		);
		invalidEasing.animateTo(1.0);

		expectThrows(
			IllegalStateException.class,
			() -> invalidEasing.advance(0.5),
			"non-finite easing output must fail explicitly"
		);
		expectThrows(
			IllegalArgumentException.class,
			() -> ArgbColor.interpolate(0, 1, Double.NaN),
			"non-finite color progress must fail explicitly"
		);
	}

	private static boolean closeTo(double actual, double expected) {
		return Math.abs(actual - expected) < 0.000_001;
	}

	private static ChoiceSetting<TestTarget> testTargetSetting() {
		return new ChoiceSetting<>(
			"target",
			"client.setting.target",
			TestTarget.PLAYER,
			List.of(
				new ChoiceOption<>("player", "client.option.player", TestTarget.PLAYER),
				new ChoiceOption<>("hostile", "client.option.hostile", TestTarget.HOSTILE)
			)
		);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void expectThrows(
		Class<? extends Throwable> expected,
		Runnable action,
		String message
	) {
		try {
			action.run();
		} catch (Throwable throwable) {
			if (expected.isInstance(throwable)) {
				return;
			}
			throw new AssertionError(message + ": wrong exception " + throwable, throwable);
		}
		throw new AssertionError(message + ": no exception");
	}

	private static final class TrackingModule extends Module {
		private final DoubleSetting speed = setting(
			new DoubleSetting("speed", "client.setting.speed", 0.5, 0.0, 1.0, 0.05)
		);
		private final BooleanSetting visible = setting(
			new BooleanSetting("visible", "client.setting.visible", true)
		);
		private int enableCalls;
		private int tickCalls;
		private boolean failDisable;

		private TrackingModule(String path) {
			super(new ModuleMetadata(
				ModuleId.of("test", path),
				"client.module.test.name",
				"client.module.test.description",
				100
			));
		}

		private DoubleSetting exposedSpeed() {
			return speed;
		}

		private BooleanSetting exposedVisible() {
			return visible;
		}

		private <S extends io.qzz.iie.setting.Setting<?>> S exposeRegister(S setting) {
			return setting(setting);
		}

		@Override
		protected void onEnable() {
			enableCalls++;
		}

		@Override
		protected void onDisable() {
			if (failDisable) {
				throw new IllegalStateException("expected test failure");
			}
		}

		@Override
		protected void onClientTick() {
			tickCalls++;
		}
	}

	private static final class RecordingTarget implements UiInputTarget {
		private final Rect bounds;
		private MouseButton lastButton;

		private RecordingTarget(Rect bounds) {
			this.bounds = bounds;
		}

		@Override
		public Rect inputBounds() {
			return bounds;
		}

		@Override
		public InputResult handleInput(UiInputEvent event) {
			if (event instanceof UiInputEvent.PointerPressed pressed) {
				lastButton = pressed.button();
				return InputResult.CONSUMED;
			}
			return InputResult.IGNORED;
		}
	}

	private static final class KeyboundModule extends Module {
		private KeyboundModule(String path, int keyCode) {
			super(new ModuleMetadata(
				ModuleId.of("test", path),
				"client.module.test.name",
				"client.module.test.description",
				100
			));
			keybind(new KeybindSetting(
				"keybind",
				"client.setting.module_keybind",
				new KeybindValue(keyCode)
			));
		}
	}

	private static final class SelfStoppingModule extends Module {
		private int disableCalls;

		private SelfStoppingModule(String path) {
			super(new ModuleMetadata(
				ModuleId.of("test", path),
				"client.module.test.name",
				"client.module.test.description",
				100
			));
		}

		@Override
		protected void onClientTick() {
			requestDisable();
		}

		@Override
		protected void onDisable() {
			disableCalls++;
		}
	}

	private static final class TextSetting extends io.qzz.iie.setting.Setting<String> {
		private TextSetting() {
			super("text", "client.setting.text", "");
		}

		@Override
		protected String normalize(String requestedValue) {
			return requestedValue;
		}
	}

	private enum TestTarget {
		PLAYER,
		FRIENDLY,
		HOSTILE
	}
}
