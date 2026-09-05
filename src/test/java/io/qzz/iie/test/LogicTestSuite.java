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
import io.qzz.iie.module.impl.movement.flight.FlightHooks;
import io.qzz.iie.module.impl.movement.flight.FlightModule;
import io.qzz.iie.module.impl.movement.flight.FlightPolicy;
import io.qzz.iie.module.impl.movement.safewalkplus.SafeWalkPlusModule;
import io.qzz.iie.module.impl.movement.safewalkplus.SafeWalkPlusPolicy;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebModule;
import io.qzz.iie.module.impl.combat.bedaura.BedAuraModule;
import io.qzz.iie.module.impl.combat.autototem.AutoTotemModule;
import io.qzz.iie.module.impl.combat.autototem.AutoTotemPolicy;
import io.qzz.iie.module.impl.combat.autototem.AutoTotemTypes.OffhandMode;
import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import io.qzz.iie.module.impl.render.norender.NoRenderModule;
import io.qzz.iie.module.impl.gui.clickgui.ClickGuiModule;
import io.qzz.iie.module.impl.player.autolibrarian.EnchantmentTarget;
import io.qzz.iie.module.impl.player.autolibrarian.EnchantmentTargetsSetting;
import io.qzz.iie.module.impl.player.autolibrarian.AutoLibrarianLogicContract;
import io.qzz.iie.module.impl.player.antiquit.AntiQuitHooks;
import io.qzz.iie.module.impl.player.antiquit.AntiQuitModule;
import io.qzz.iie.module.impl.player.copynbt.BlockNbtCategory;
import io.qzz.iie.module.impl.player.copynbt.CopyNbtHooks;
import io.qzz.iie.module.impl.player.copynbt.CopyNbtModule;
import io.qzz.iie.module.impl.player.copynbt.CopyNbtPolicy;
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
import io.qzz.iie.module.impl.render.crystalanimation.CrystalAnimationHooks;
import io.qzz.iie.module.impl.render.crystalanimation.CrystalAnimationMode;
import io.qzz.iie.module.impl.render.crystalanimation.CrystalAnimationModule;
import io.qzz.iie.module.impl.render.crystalanimation.CrystalAnimationPolicy;
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
import io.qzz.iie.module.impl.render.zoom.ZoomHooks;
import io.qzz.iie.module.impl.render.zoom.ZoomModule;
import io.qzz.iie.module.impl.render.freelook.FreeLookHooks;
import io.qzz.iie.module.impl.render.freelook.FreeLookModule;
import io.qzz.iie.module.impl.render.freelook.FreeLookPolicy;
import io.qzz.iie.module.impl.player.packetmine.PacketMineBox;
import io.qzz.iie.module.impl.player.packetmine.PacketMineModule;
import io.qzz.iie.module.impl.player.packetmine.PacketMinePolicy;
import io.qzz.iie.module.impl.player.packetmine.PacketMineRenderStyle;
import io.qzz.iie.module.impl.player.packetmine.PacketMineVisualState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
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
import io.qzz.iie.setting.ColorSetting;
import io.qzz.iie.setting.DoubleRange;
import io.qzz.iie.setting.DoubleRangeSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;
import io.qzz.iie.setting.KeybindActionDispatcher;
import io.qzz.iie.setting.KeybindValue;
import io.qzz.iie.ui.component.control.ColorPickerControl;
import io.qzz.iie.ui.component.control.RangeSliderControl;
import io.qzz.iie.ui.panel.DoubleRangeSettingItem;
import io.qzz.iie.module.impl.player.airplace.AirPlaceDirection;
import io.qzz.iie.module.impl.player.airplace.AirPlaceModule;
import io.qzz.iie.module.impl.player.airplace.AirPlacePolicy;
import io.qzz.iie.module.impl.combat.autoclicker.AutoClickerController;
import io.qzz.iie.module.impl.combat.autoclicker.AutoClickerModule;
import io.qzz.iie.ui.hud.CpsTracker;
import io.qzz.iie.ui.hud.CpsHudRenderer;
import io.qzz.iie.ui.hud.VersionHudRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
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
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.world.entity.player.Abilities;

import io.qzz.iie.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
		crystalAnimationDeclaresDefaultsAndHooksFollowLifecycle();
		zoomModuleMetadataAndSettings();
		zoomHooksCalculatesMultipliersAndFollowsLifecycle();
		moduleNotificationControlsStateChangeMessages();
		packetMineModuleMetadataAndSettings();
		packetMinePolicyGeometryAndRotationCalculations();
		settingVisibilityConditionsControlVisibility();
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
		bedAuraModuleMetadataAndSettings();
		autoTotemPolicyFallDamageAndEquipDecisions();
		autoTotemModuleMetadataAndSettings();
		noRenderModuleMetadataAndHooksFollowLifecycle();
		antiQuitModuleMetadataSettingsAndHooksFollowLifecycle();
		flightModuleMetadataSettingsAndPolicyCalculations();
		doubleRangeSettingNormalizationAndFractionMath();
		rangeSliderControlPointerHandling();
		doubleRangeConfigPersistence();
		airPlaceModuleMetadataSettingsAndPolicy();
		autoClickerModuleMetadataSettingsAndDualScheduling();
		cpsTrackerAndHudRendererCalculations();
		colorSettingHexParsingAndConversion();
		colorPickerControlHsvMathAndPresets();
		versionHudRendererMetadataAndMeasurements();
		freeLookPolicyRotationAndInterpolation();
		copyNbtPolicyDecisionsAndSizeLimits();
		copyNbtModuleMetadataAndDefaults();
		allRegisteredModulesAndSettingsHaveTranslations();
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
		check(module.coverageThreshold().value() == 45.0, "coverage threshold must default to 45%");
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
		check(module.settings().size() == 12, "drop point must expose 5 color settings + 5 opacity sliders + hint count + notification");
		check(module.scaffoldHintCount().value() == 2.0, "scaffold hint count must default to two");
		check(module.scaffoldHintCount().minimum() == 1.0, "scaffold hint count minimum must be one");
		check(module.scaffoldHintCount().maximum() == 8.0, "scaffold hint count maximum must be eight");
		check(module.safeColor().opacity() == 0.4, "safe color opacity must be independently adjustable");
		check(module.safeColor().argb() != 0, "safe color ARGB must be valid");
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
		check(module.radius().value() == 6.0, "explosion warning radius must default to six blocks");
		check(module.radius().minimum() == 1.0 && module.radius().maximum() == 10.0,
			"explosion warning radius must be constrained to one through ten blocks");
		check(module.impendingMessage().value(), "impending explosion messages must default to enabled");
		check(!module.creeperRangeMessage().value(), "creeper range messages must default to disabled");
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
		check(module.itemPriority().value() == ItemPriority.FIRE_CHARGE_FIRST, "fire charge must be preferred by default");
		check(!module.restoreAfterFlint().value(), "flint ignition restore must default to disabled");
		check(!module.cameraFollows().value(), "client camera following must default to disabled");
		check(module.rotationTicks().value() == 3.0, "rotation must default to 3 ticks");
		check(module.rotationTicks().minimum() == 1.0 && module.rotationTicks().maximum() == 10.0,
			"rotation time must be constrained to one through ten ticks");
		check(module.targetHandling().value() == TargetHandling.LATEST_ONLY,
			"new TNT placements must replace unfinished targets by default");
		check(module.strictInteraction().value(), "strict reach and visibility checks must default to enabled");
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
		check(module.settings().size() == 21, "all requested Click GUI values must be GUI settings");
		check(module.language().value().equals("zh_cn"), "language defaults to zh_cn");
		check(module.customFont().value().equals("default"), "customFont defaults to default");
		check(module.armorHudEnabled().value(), "armor HUD enabled defaults to true");
		check(module.potionHudEnabled().value(), "potion HUD enabled defaults to true");
		check(module.arrayListEnabled().value(), "arrayList HUD enabled defaults to true");
		check(module.cpsHudEnabled().value(), "cps HUD enabled defaults to true");
		check(module.versionHudEnabled().value(), "version HUD enabled defaults to true");
		check(module.versionHudPosition().value() != null, "version HUD position must not be null");
		check(
			module.openShortcut().value().keyCode() == 344,
			"Click GUI shortcut must default to right Shift"
		);
		check(module.guiTextScale().value() == 1.3, "Click GUI text scale defaults to 130%");
		check(module.messageBoxScale().value() == 1.0, "message box scale defaults to 100%");
		check(module.messageTextScale().value() == 1.0, "message text scale defaults to 100%");
		check(module.messageOpacity().value() == 0.8, "message box opacity defaults to 80%");
		check(
			module.messageFont().value().equals("minecraft:uniform"),
			"message font defaults to uniform"
		);
		check(module.messageTextColor() == 0xFFDEE8ED, "message text color defaults to theme text");
		check(module.messageTextColorSetting().rgb() == 0xDEE8ED, "message text color setting defaults to 0xDEE8ED");

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

		check(manager.modules().size() == 26, "only production built-ins must be registered in the GUI");
		Module copyNbt = manager.find(ModuleId.of("client", "copy_nbt")).orElseThrow();
		check(
			copyNbt.category().id().equals("player"),
			"copy nbt derives from its player package"
		);
		Module fullbright = manager.find(ModuleId.of("client", "fullbright")).orElseThrow();
		check(
			fullbright.category().id().equals("render"),
			"fullbright derives from its render package"
		);
		Module freeLook = manager.find(ModuleId.of("client", "free_look")).orElseThrow();
		check(
			freeLook.category().id().equals("render"),
			"free look derives from its render package"
		);
		Module noRender = manager.find(ModuleId.of("client", "no_render")).orElseThrow();
		check(
			noRender.category().id().equals("render"),
			"no render derives from its render package"
		);
		Module zoom = manager.find(ModuleId.of("client", "zoom")).orElseThrow();
		check(
			zoom.category().id().equals("render"),
			"zoom derives from its render package"
		);
		Module crystalAnimation = manager.find(ModuleId.of("client", "crystal_animation")).orElseThrow();
		check(
			crystalAnimation.category().id().equals("render"),
			"crystal animation derives from its render package"
		);
		Module packetMine = manager.find(ModuleId.of("client", "packet_mine")).orElseThrow();
		check(
			packetMine.category().id().equals("player"),
			"packet mine derives from its player package"
		);
		Module autoWeb = manager.find(ModuleId.of("client", "auto_web")).orElseThrow();
		check(
			autoWeb.category().id().equals("combat"),
			"auto web derives from its combat package"
		);
		Module autoClicker = manager.find(ModuleId.of("client", "auto_clicker")).orElseThrow();
		check(
			autoClicker.category().id().equals("combat"),
			"auto clicker derives from its combat package"
		);
		Module airPlace = manager.find(ModuleId.of("client", "air_place")).orElseThrow();
		check(
			airPlace.category().id().equals("player"),
			"air place derives from its player package"
		);
		Module bedAura = manager.find(ModuleId.of("client", "bed_aura")).orElseThrow();
		check(
			bedAura.category().id().equals("combat"),
			"bed aura derives from its combat package"
		);
		Module autoTotem = manager.find(ModuleId.of("client", "auto_totem")).orElseThrow();
		check(
			autoTotem.category().id().equals("combat"),
			"auto totem derives from its combat package"
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
		check(
			manager.find(ModuleId.of("client", "anti_quit")).isPresent(),
			"anti quit must be registered in the GUI"
		);
		Module flight = manager.find(ModuleId.of("client", "flight")).orElseThrow();
		check(
			flight.category().id().equals("movement"),
			"flight derives from its movement package"
		);
	}

	private static void autoWalkModuleMetadataAndLifecycle() {
		AutoWalkModule module = new AutoWalkModule();
		check(
			module.category().id().equals("movement"),
			"auto walk derives from its movement package"
		);
		check(module.metadata().toggleable(), "auto walk must expose a real toggle");
		check(module.settings().size() == 2, "auto walk must declare its shortcut and notification");
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
		check(horizontal.settings().size() == 2, "invert mouse must declare its shortcut and notification");
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
			vertical.settings().size() == 2,
			"invert mouse pitch must declare its shortcut and notification"
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
			module.settings().size() == 2,
			"special flip must declare its shortcut and notification"
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
		check(module.targets().value().size() == 5, "five targets must be declared by default");
		check(
			module.targets().value().getFirst().equals(
				new EnchantmentTarget("minecraft:unbreaking", 3, false, 4, 64)
			),
			"unbreaking III target must be the first default"
		);
		check(module.searchRadius().value() == 3.0, "search radius must default to 3");
		check(module.placementRadius().value() == 2.0, "placement radius must default to 2");
		check(module.allowHandMining().value(), "hand mining must default on");
		check(module.reportTrades().value(), "trade reporting must default on");
		check(module.autoRecycle().value(), "automatic lectern recovery must default on");
		check(module.rotationTicks().value() == 6.0, "rotation must default to 6 ticks");
		check(!module.keybind().orElseThrow().value().isBound(), "shortcut must be unbound");
	}

	private static void betterHealthBarDeclaresDefaultsAndCapsExtraRows() {
		BetterHealthBarModule module = new BetterHealthBarModule();

		check(
			module.category().id().equals("render"),
			"better health bar derives from its render package"
		);
		check(module.thresholdRows().value() == 1, "health threshold must default to one row");
		check(module.numberScale().value() == 1.2, "health number size must default to 120%");
		check(module.settings().size() == 4, "module must expose threshold, position, size and notification");

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
			closeTo(BetterHealthBarHooks.clampMaximumHealth(60.0F), 20.0),
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
		check(module.settings().size() == 3, "module must declare mode, shortcut and notification");
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

	private static void crystalAnimationDeclaresDefaultsAndHooksFollowLifecycle() {
		CrystalAnimationModule module = new CrystalAnimationModule();

		check(
			module.category().id().equals("render"),
			"crystal animation derives from its render package"
		);
		check(module.metadata().toggleable(), "crystal animation must expose a toggle");
		check(
			module.modeSetting().value() == CrystalAnimationMode.SPIN,
			"spin mode must be the default"
		);
		check(
			module.speedSetting().defaultValue() == 5.2,
			"speed setting must default to 5.2x"
		);
		check(
			module.speedSetting().minimum() == 0.0 && module.speedSetting().maximum() == 10.0,
			"speed setting range must be 0.0 to 10.0"
		);
		check(
			module.speedSetting().step() == 0.1,
			"speed setting step must be 0.1"
		);
		check(module.settings().size() == 8, "module must declare mode, speed, offsets, scale, shortcut, and notification");
		check(
			module.offsetXSetting().defaultValue() == 0.0
				&& module.offsetXSetting().minimum() == -5.0
				&& module.offsetXSetting().maximum() == 5.0
				&& module.offsetXSetting().step() == 0.1,
			"offsetX range must be -5.0 to 5.0 with step 0.1"
		);
		check(
			module.offsetYSetting().defaultValue() == 0.0
				&& module.offsetYSetting().minimum() == -5.0
				&& module.offsetYSetting().maximum() == 5.0
				&& module.offsetYSetting().step() == 0.1,
			"offsetY range must be -5.0 to 5.0 with step 0.1"
		);
		check(
			module.offsetZSetting().defaultValue() == 0.0
				&& module.offsetZSetting().minimum() == -5.0
				&& module.offsetZSetting().maximum() == 5.0
				&& module.offsetZSetting().step() == 0.1,
			"offsetZ range must be -5.0 to 5.0 with step 0.1"
		);
		check(
			module.scaleSetting().defaultValue() == 1.1
				&& module.scaleSetting().minimum() == 0.1
				&& module.scaleSetting().maximum() == 5.0
				&& module.scaleSetting().step() == 0.1,
			"scale range must be 0.1 to 5.0 with step 0.1"
		);
		check(
			!module.keybind().orElseThrow().value().isBound(),
			"crystal animation shortcut must default unbound"
		);

		// Visibility condition check
		check(
			module.speedSetting().isVisible(),
			"speed setting must be visible when mode is spin"
		);
		module.modeSetting().set(CrystalAnimationMode.STATIC);
		check(
			!module.speedSetting().isVisible(),
			"speed setting must be hidden when mode is static"
		);
		module.modeSetting().set(CrystalAnimationMode.SPIN);
		check(
			module.speedSetting().isVisible(),
			"speed setting must be visible when mode is spin"
		);
		module.modeSetting().set(CrystalAnimationMode.STATIC);
		check(
			!module.speedSetting().isVisible(),
			"speed setting must collapse again when switching back to static"
		);

		// Hook & Policy lifecycle checks
		check(
			!CrystalAnimationHooks.isEnabled(),
			"uninstalled hooks must stay disabled"
		);
		check(
			CrystalAnimationHooks.mode() == null,
			"uninstalled hooks must return null mode"
		);

		CrystalAnimationHooks.install(module);
		check(
			!CrystalAnimationHooks.isEnabled(),
			"disabled module must keep hooks disabled"
		);

		ModuleManager manager = new ModuleManager();
		manager.register(module);
		manager.setEnabled(module.id(), true);

		check(
			CrystalAnimationHooks.isEnabled(),
			"enabling module must activate hooks"
		);
		check(
			CrystalAnimationHooks.mode() == CrystalAnimationMode.STATIC,
			"mode must reflect static mode"
		);

		module.modeSetting().set(CrystalAnimationMode.SPIN);
		module.speedSetting().set(2.5);
		module.offsetXSetting().set(1.2);
		module.offsetYSetting().set(0.5);
		module.offsetZSetting().set(-0.8);
		module.scaleSetting().set(1.5);

		check(
			CrystalAnimationHooks.mode() == CrystalAnimationMode.SPIN,
			"mode must reflect spin mode"
		);
		check(
			closeTo(CrystalAnimationHooks.speed(), 2.5),
			"speed must reflect custom speed"
		);
		check(
			closeTo(CrystalAnimationHooks.offsetX(), 1.2),
			"offsetX must reflect custom offset"
		);
		check(
			closeTo(CrystalAnimationHooks.offsetY(), 0.5),
			"offsetY must reflect custom offset"
		);
		check(
			closeTo(CrystalAnimationHooks.offsetZ(), -0.8),
			"offsetZ must reflect custom offset"
		);
		check(
			closeTo(CrystalAnimationHooks.scale(), 1.5),
			"scale must reflect custom scale"
		);

		// Policy tests
		check(
			closeTo(CrystalAnimationPolicy.calculateSpinAngle(10.0F, 1.0), 30.0),
			"policy spin angle at 1.0x must be 3.0 * ageInTicks"
		);
		check(
			closeTo(CrystalAnimationPolicy.calculateSpinAngle(10.0F, 2.5), 75.0),
			"policy spin angle at 2.5x must scale with speed multiplier"
		);
		check(
			CrystalAnimationPolicy.isStatic(true, CrystalAnimationMode.STATIC),
			"policy isStatic must be true when enabled and static"
		);
		check(
			!CrystalAnimationPolicy.isStatic(false, CrystalAnimationMode.STATIC),
			"policy isStatic must be false when disabled"
		);
		check(
			CrystalAnimationPolicy.isSpin(true, CrystalAnimationMode.SPIN),
			"policy isSpin must be true when enabled and spin"
		);
		check(
			!CrystalAnimationPolicy.isSpin(true, CrystalAnimationMode.STATIC),
			"policy isSpin must be false when mode is static"
		);
		check(
			closeTo(CrystalAnimationPolicy.fixedYOffset(), -8.8),
			"fixed Y offset must compensate for model space baseline"
		);
		check(
			closeTo(CrystalAnimationPolicy.toModelUnits(1.0), 8.0),
			"1 block offset must convert to 8 model units"
		);
		check(
			closeTo(CrystalAnimationPolicy.calculateY(0.0), -8.8),
			"calculateY with 0 offset must equal fixedYOffset"
		);
		check(
			closeTo(CrystalAnimationPolicy.calculateY(1.0), -16.8),
			"calculateY with +1.0 offset must raise crystal (decrease model Y)"
		);
		check(
			closeTo(CrystalAnimationPolicy.calculateY(-1.0), -0.8),
			"calculateY with -1.0 offset must lower crystal (increase model Y)"
		);

		manager.setEnabled(module.id(), false);
		check(
			!CrystalAnimationHooks.isEnabled(),
			"disabling module must deactivate hooks"
		);
	}

	private static void zoomModuleMetadataAndSettings() {
		ZoomModule module = new ZoomModule();
		check(
			module.category().id().equals("render"),
			"zoom derives from its render package"
		);
		check(module.metadata().toggleable(), "zoom must expose a toggle");
		check(
			closeTo(module.zoomFactor().value(), 7.5),
			"default zoom factor must be 7.5"
		);
		check(
			closeTo(module.zoomFactor().minimum(), 1.5),
			"minimum zoom factor must be 1.5"
		);
		check(
			closeTo(module.zoomFactor().maximum(), 20.0),
			"maximum zoom factor must be 20.0"
		);
		check(
			closeTo(module.zoomFactor().step(), 0.5),
			"zoom factor step must be 0.5"
		);
		check(module.smoothZoom().value(), "smooth zoom must default to enabled");
		check(
			module.reduceSensitivity().value(),
			"reduce sensitivity must default to enabled"
		);
		check(
			!module.keybind().orElseThrow().value().isBound(),
			"zoom keybind must default unbound"
		);
		check(module.settings().size() == 5, "zoom must declare 5 settings");
	}

	private static void zoomHooksCalculatesMultipliersAndFollowsLifecycle() {
		ZoomModule module = new ZoomModule();
		ModuleManager manager = new ModuleManager();
		manager.register(module);

		ZoomHooks.resetState();
		check(
			closeTo(ZoomHooks.getZoomMultiplier(1.0f), 1.0),
			"uninstalled hook must return 1.0 multiplier"
		);
		check(
			closeTo(ZoomHooks.getSensitivityMultiplier(), 1.0),
			"uninstalled hook must return 1.0 sensitivity"
		);

		ZoomHooks.install(module);
		check(
			closeTo(ZoomHooks.getZoomMultiplier(1.0f), 1.0),
			"disabled module must return 1.0 multiplier"
		);
		check(
			closeTo(ZoomHooks.getSensitivityMultiplier(), 1.0),
			"disabled module must return 1.0 sensitivity"
		);

		// Test without smooth zoom (instant)
		module.smoothZoom().set(false);
		module.zoomFactor().set(6.0);
		manager.setEnabled(module.id(), true);
		check(
			closeTo(ZoomHooks.getZoomMultiplier(1.0f), 6.0),
			"enabled module without smooth zoom must immediately return configured factor"
		);
		check(
			closeTo(ZoomHooks.getSensitivityMultiplier(), 1.0 / 6.0),
			"sensitivity must be reduced by zoom factor"
		);

		module.reduceSensitivity().set(false);
		check(
			closeTo(ZoomHooks.getSensitivityMultiplier(), 1.0),
			"disabling reduceSensitivity must keep sensitivity at 1.0"
		);
		module.reduceSensitivity().set(true);

		manager.setEnabled(module.id(), false);
		check(
			closeTo(ZoomHooks.getZoomMultiplier(1.0f), 1.0),
			"disabling module without smooth zoom must immediately return 1.0"
		);
		check(
			closeTo(ZoomHooks.getSensitivityMultiplier(), 1.0),
			"disabling module must restore sensitivity to 1.0"
		);

		// Test with smooth zoom animation
		module.smoothZoom().set(true);
		AtomicLong currentTime = new AtomicLong(1000L);
		ZoomHooks.setTimeSupplierForTesting(currentTime::get);

		ZoomHooks.resetState();
		manager.setEnabled(module.id(), true);

		// At start of transition (elapsed = 0)
		check(
			closeTo(ZoomHooks.getZoomMultiplier(1.0f), 1.0),
			"at transition start zoom multiplier must be initial value"
		);

		// Halfway through 150ms transition (75ms elapsed)
		currentTime.set(1075L);
		float midMultiplier = ZoomHooks.getZoomMultiplier(1.0f);
		check(
			midMultiplier > 1.0f && midMultiplier < 6.0f,
			"mid-transition multiplier must be between start and target"
		);

		// After transition finishes (150ms elapsed)
		currentTime.set(1150L);
		check(
			closeTo(ZoomHooks.getZoomMultiplier(1.0f), 6.0),
			"after transition completes multiplier must equal target factor"
		);
		check(
			closeTo(ZoomHooks.getSensitivityMultiplier(), 1.0 / 6.0),
			"after transition completes sensitivity must match target factor"
		);

		// Transition back to 1.0 on disable
		manager.setEnabled(module.id(), false);
		check(
			closeTo(ZoomHooks.getZoomMultiplier(1.0f), 6.0),
			"at disable transition start multiplier must be at previous value"
		);

		currentTime.set(1225L);
		float midDisableMultiplier = ZoomHooks.getZoomMultiplier(1.0f);
		check(
			midDisableMultiplier > 1.0f && midDisableMultiplier < 6.0f,
			"mid-disable multiplier must be between previous and 1.0"
		);

		currentTime.set(1300L);
		check(
			closeTo(ZoomHooks.getZoomMultiplier(1.0f), 1.0),
			"after disable transition completes multiplier must be 1.0"
		);
		check(
			closeTo(ZoomHooks.getSensitivityMultiplier(), 1.0),
			"after disable transition completes sensitivity must be 1.0"
		);

		ZoomHooks.resetTimeSupplierForTesting();
	}

	private static void moduleNotificationControlsStateChangeMessages() {
		ModuleManager manager = new ModuleManager();
		ZoomModule module = new ZoomModule();
		manager.register(module);

		check(module.notification().value(), "module notification setting must default to true");

		List<String> messages = new java.util.ArrayList<>();
		MessageBoxApi messageApi = (message, duration) -> messages.add(message.getString());

		ModuleStateMessageNotifier.attach(manager, messageApi);

		// With notification == true, enable and disable trigger messages
		manager.setEnabled(module.id(), true);
		check(messages.size() == 1, "enabling with notification=true must emit message");
		messages.clear();

		manager.setEnabled(module.id(), false);
		check(messages.size() == 1, "disabling with notification=true must emit message");
		messages.clear();

		// With notification == false, enable and disable do NOT trigger messages
		module.notification().set(false);

		manager.setEnabled(module.id(), true);
		check(messages.isEmpty(), "enabling with notification=false must NOT emit message");

		manager.setEnabled(module.id(), false);
		check(messages.isEmpty(), "disabling with notification=false must NOT emit message");
	}

	private static void packetMineModuleMetadataAndSettings() {
		PacketMineModule module = new PacketMineModule();
		check(
			module.category().id().equals("player"),
			"packet mine derives from its player package"
		);
		check(module.metadata().toggleable(), "packet mine must expose a toggle");
		check(module.settings().size() == 10, "packet mine must declare 10 settings");
		check(module.delayTicks().value() == 1.0, "delay ticks must default to 1.0");
		check(module.delayTicks().minimum() == 0.0, "delay ticks min must be 0");
		check(module.delayTicks().maximum() == 10.0, "delay ticks max must be 10");
		check(module.range().value() == 3.5, "range must default to 3.5");
		check(module.range().minimum() == 1.0, "range min must be 1.0");
		check(module.range().maximum() == 8.0, "range max must be 8.0");
		check(module.rotationTicks().value() == 2.0, "rotation ticks must default to 2.0");
		check(module.resetRotationTicks().value() == 4.0, "reset rotation ticks must default to 4.0");
		check(!module.ghostHand().value(), "ghost hand must default to false");
		check(module.renderStyle().value() == PacketMineRenderStyle.EXPAND, "render style defaults to EXPAND");
		check(module.notification().value(), "notification must default to true");
		check(!module.keybind().orElseThrow().value().isBound(), "shortcut defaults unbound");
	}

	private static void packetMinePolicyGeometryAndRotationCalculations() {
		BlockPos pos = new BlockPos(10, 64, 20);

		// Expand box at 0.5 progress -> center (10.5, 64.5, 20.5) +- 0.25
		PacketMineBox expand = PacketMinePolicy.calculateExpandBox(pos, 0.5f);
		check(closeTo(expand.minX(), 10.25) && closeTo(expand.maxX(), 10.75), "expand box X bounds");
		check(closeTo(expand.minY(), 64.25) && closeTo(expand.maxY(), 64.75), "expand box Y bounds");
		check(closeTo(expand.minZ(), 20.25) && closeTo(expand.maxZ(), 20.75), "expand box Z bounds");

		// Rise box at 0.5 progress -> Y from 64.0 to 64.5
		PacketMineBox rise = PacketMinePolicy.calculateRiseBox(pos, 0.5f);
		check(closeTo(rise.minX(), 10.0) && closeTo(rise.maxX(), 11.0), "rise box X bounds");
		check(closeTo(rise.minY(), 64.0) && closeTo(rise.maxY(), 64.5), "rise box Y bounds");
		check(closeTo(rise.minZ(), 20.0) && closeTo(rise.maxZ(), 21.0), "rise box Z bounds");

		// Full box -> 10 to 11, 64 to 65, 20 to 21
		PacketMineBox full = PacketMinePolicy.calculateFullBox(pos);
		check(closeTo(full.minX(), 10.0) && closeTo(full.maxX(), 11.0), "full box X bounds");
		check(closeTo(full.minY(), 64.0) && closeTo(full.maxY(), 65.0), "full box Y bounds");
		check(closeTo(full.minZ(), 20.0) && closeTo(full.maxZ(), 21.0), "full box Z bounds");

		// Reach check
		Vec3 closeEye = new Vec3(10.5, 65.0, 20.5);
		check(PacketMinePolicy.isWithinReach(closeEye, pos, 3.0), "close eye position must be within reach");
		Vec3 farEye = new Vec3(100.0, 64.0, 20.0);
		check(!PacketMinePolicy.isWithinReach(farEye, pos, 3.0), "far eye position must be outside reach");

		// Aim angle calculation
		Vec3 eye = new Vec3(0, 0, 0);
		Vec3 target = new Vec3(0, 0, 5);
		PacketMinePolicy.AimAngles aim = PacketMinePolicy.calculateAim(eye, target);
		check(closeTo(aim.pitch(), 0.0), "straight horizontal aim pitch must be 0");
		check(closeTo(aim.yaw(), 0.0), "straight south aim yaw must be 0");

		// Angle interpolation with wrap-around
		float interp = PacketMinePolicy.interpolateAngle(350.0f, 10.0f, 0.5f);
		check(closeTo(interp, 360.0f) || closeTo(interp, 0.0f), "wrap-around interpolation must choose shortest path");
	}

	private static void settingVisibilityConditionsControlVisibility() {
		BooleanSetting master = new BooleanSetting("master", "test.master", false);
		DoubleSetting child = new DoubleSetting("child", "test.child", 5.0, 0.0, 10.0, 1.0)
			.visibleWhen(master::value);

		check(!child.isVisible(), "child setting must be hidden when master condition is false");
		master.set(true);
		check(child.isVisible(), "child setting must become visible when master condition is true");
		master.set(false);
		check(!child.isVisible(), "child setting must hide again when master condition is false");
	}

	private static void autoWebDeclaresConfirmedDefaults() {
		AutoWebModule module = new AutoWebModule();

		check(module.settings().size() == 12, "auto web must expose every confirmed setting");
		check(module.targetPriority().value() == TargetPriority.CROSSHAIR, "crosshair target must be default");
		check(module.targetType().value() == TargetType.ALL, "all must be the default target type");
		check(module.placementPattern().value() == PlacementPattern.FEET, "feet-only must be default");
		check(module.range().value() == 4.0, "range must default to 4 blocks");
		check(module.rotationTicks().value() == 4.7, "rotation must default to 4.7 ticks");
		check(module.hotbarMode().value() == HotbarMode.SILENT, "silent hotbar switching must be default");
		check(module.checkInventory().value(), "inventory search must be enabled by default");
		check(
			module.inventoryMode().value() == InventoryMode.SILENT_SELECTED_RESTORE,
			"silent selected-slot inventory swap must be default"
		);
		check(
			module.placementCadence().value() == PlacementCadence.ONE_PER_ROTATION,
			"natural one-per-rotation placement must be default"
		);
		check(module.placementInterval().value() == 1.7, "placement interval must default to 1.7 ticks");
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

	private static void bedAuraModuleMetadataAndSettings() {
		BedAuraModule module = new BedAuraModule();
		check(module.category().id().equals("combat"), "bed aura must reside in combat category");
		check(module.range().value() == 4.5, "range must default to 4.5");
		check(module.placeInterval().value() == 2.0, "place interval must default to 2.0");
		check(module.breakInterval().value() == 2.0, "break interval must default to 2.0");
		check(module.onlyNether().value(), "only nether must default to true");
		check(module.keybind().isPresent(), "bed aura must declare shortcut");
	}

	private static void autoTotemPolicyFallDamageAndEquipDecisions() {
		check(AutoTotemPolicy.calculateFallDamage(10.0, 0.0F, false) == 7.0, "fall damage from 10 blocks without jump boost must be 7.0");
		check(AutoTotemPolicy.calculateFallDamage(10.0, 2.0F, false) == 5.0, "fall damage with jump boost 2 must be 5.0");
		check(AutoTotemPolicy.calculateFallDamage(10.0, 0.0F, true) == 0.0, "slow falling must completely negate fall damage");
		check(AutoTotemPolicy.calculateFallDamage(3.0, 0.0F, false) == 0.0, "fall distance <= 3 blocks must have 0 damage");

		check(AutoTotemPolicy.isCompletelyInvulnerable(5), "resistance level 5 must be invulnerable");
		check(!AutoTotemPolicy.isCompletelyInvulnerable(4), "resistance level 4 must not be completely invulnerable");

		check(!AutoTotemPolicy.shouldEquipTotem(5.0, 0.0, 0.0, 10.0, 10.0, true, true), "invulnerable state must not equip totem");
		check(AutoTotemPolicy.shouldEquipTotem(20.0, 0.0, 0.0, 10.0, 10.0, false, false), "onlyOnLowHealth = false must always equip totem");
		check(AutoTotemPolicy.shouldEquipTotem(8.0, 0.0, 0.0, 10.0, 10.0, true, false), "health <= threshold must equip totem");
		check(AutoTotemPolicy.shouldEquipTotem(20.0, 0.0, 12.0, 10.0, 10.0, true, false), "fall damage >= threshold must equip totem");
		check(AutoTotemPolicy.shouldEquipTotem(15.0, 0.0, 8.0, 10.0, 10.0, true, false), "health after fall damage <= threshold must equip totem");
		check(!AutoTotemPolicy.shouldEquipTotem(20.0, 0.0, 0.0, 10.0, 10.0, true, false), "safe state must not equip totem");
	}

	private static void autoTotemModuleMetadataAndSettings() {
		AutoTotemModule module = new AutoTotemModule(MessageBoxApi.noop());
		check(module.category().id().equals("combat"), "auto totem must reside in combat category");
		check(module.healthThreshold().value() == 10.0, "health threshold must default to 10.0");
		check(module.fallDamageThreshold().value() == 10.0, "fall damage threshold must default to 10.0");
		check(module.delay().value() == 0.0, "delay must default to 0.0");
		check(module.randomDelay().value() == 0.0, "random delay must default to 0.0");
		check(module.checkEffects().value(), "check effects must default to true");
		check(!module.onlyOnLowHealth().value(), "only on low health must default to false");
		check(module.offhandMode().value() == OffhandMode.SWAP, "offhand mode must default to swap");
		check(!module.alerts().value(), "alerts must default to false");
		check(!module.fallbackShield().value(), "fallback shield must default to false");
	}

	private static void noRenderModuleMetadataAndHooksFollowLifecycle() {
		ModuleManager manager = new ModuleManager();
		NoRenderModule module = new NoRenderModule();
		manager.register(module);
		NoRenderHooks.install(module);

		check(module.category().id().equals("render"), "no render must reside in render category");
		check(module.keybind().isPresent(), "no render must declare shortcut");

		// 默认折叠与默认项状态
		check(!module.particlesGroup().value(), "particles group must default to collapsed");
		check(!module.staticUiGroup().value(), "static UI group must default to collapsed");
		check(!module.effectsGroup().value(), "effects group must default to collapsed");
		check(!module.animationsGroup().value(), "animations group must default to collapsed");

		// 初始级联可见性校验 (父折叠项为 false 时子项必须不可见)
		check(!module.particleExplosion().isVisible(), "child setting must be invisible when parent group is collapsed");
		check(!module.itemFrames().isVisible(), "child setting must be invisible when parent group is collapsed");

		// 展开粒子组并测试可见性级联
		module.particlesGroup().set(true);
		check(module.particleExplosion().isVisible(), "child setting must become visible when parent group is expanded");
		module.particlesGroup().set(false);
		check(!module.particleExplosion().isVisible(), "child setting must hide again when parent group is collapsed");

		// 三级折叠级联测试
		module.staticUiGroup().set(true);
		check(!module.itemFrames().isVisible(), "itemFrames must be hidden when subgroupStaticEntities is collapsed");
		module.subgroupStaticEntities().set(true);
		check(module.itemFrames().isVisible(), "itemFrames must be visible when both groups are expanded");

		// 禁用状态下所有 Hook 必须返回 false / 默认值
		check(!NoRenderHooks.isEnabled(), "hooks must report disabled when module is disabled");
		check(!NoRenderHooks.shouldNoRenderParticle("minecraft:explosion"), "particle hook must be false when disabled");
		check(!NoRenderHooks.shouldNoRenderItemFrames(), "item frames hook must be false when disabled");
		check(!NoRenderHooks.shouldNoRenderPlayerNameTags(), "player name tags hook must be false when disabled");
		check(!NoRenderHooks.shouldNoRenderWeather(), "weather hook must be false when disabled");
		check(!NoRenderHooks.shouldFreezeSpriteAnimation("minecraft:block/water_flow"), "water freeze hook must be false when disabled");
		check(NoRenderHooks.getGlobalFogDistance() == 1.0, "default fog distance must be 1.0");

		// 启用模块
		manager.setEnabled(module.id(), true);
		check(NoRenderHooks.isEnabled(), "hooks must report enabled when module is enabled");

		// 1. 粒子效果测试
		check(!NoRenderHooks.shouldNoRenderParticle("minecraft:explosion"), "default particles must not be blocked");
		module.particleExplosion().set(true);
		check(NoRenderHooks.shouldNoRenderParticle("minecraft:explosion"), "explosion particle must be blocked");
		check(NoRenderHooks.shouldNoRenderParticle("minecraft:sonic_boom"), "sonic boom particle must be blocked");
		check(!NoRenderHooks.shouldNoRenderParticle("minecraft:heart"), "heart particle must not be blocked");

		// 自定义粒子黑名单测试
		module.particleCustomBlacklist().toggle("custom_mod:magic_spark");
		check(NoRenderHooks.shouldNoRenderParticle("custom_mod:magic_spark"), "custom blacklisted particle must be blocked");
		module.particleCustomBlacklist().toggle("custom_mod:magic_spark");
		check(!NoRenderHooks.shouldNoRenderParticle("custom_mod:magic_spark"), "untoggled particle must not be blocked");

		// 2. 静态实体与界面测试
		module.itemFrames().set(true);
		module.armorStands().set(true);
		module.paintings().set(true);
		module.playerNameTags().set(true);
		module.itemFrameNameTags().set(true);
		module.beaconBeams().set(true);
		module.enchantingTableBooks().set(true);
		module.movingPistons().set(true);
		module.underwaterLavaOverlay().set(true);

		check(NoRenderHooks.shouldNoRenderItemFrames(), "item frames must be blocked");
		check(NoRenderHooks.shouldNoRenderArmorStands(), "armor stands must be blocked");
		check(NoRenderHooks.shouldNoRenderPaintings(), "paintings must be blocked");
		check(NoRenderHooks.shouldNoRenderPlayerNameTags(), "player name tags must be blocked");
		check(NoRenderHooks.shouldNoRenderItemFrameNameTags(), "item frame name tags must be blocked");
		check(NoRenderHooks.shouldNoRenderBeaconBeams(), "beacon beams must be blocked");
		check(NoRenderHooks.shouldNoRenderEnchantingTableBooks(), "enchanting books must be blocked");
		check(NoRenderHooks.shouldNoRenderMovingPistons(), "moving pistons must be blocked");
		check(NoRenderHooks.shouldNoRenderUnderwaterLavaOverlay(), "underwater/lava overlay must be blocked");

		// 3. 物效测试
		module.globalFogDistance().set(4.0);
		module.fogOverworld().set(true);
		module.fogNether().set(true);
		module.fogEnd().set(true);
		module.weather().set(true);
		module.sky().set(true);
		module.biomeColors().set(true);

		check(NoRenderHooks.getGlobalFogDistance() == 4.0, "global fog distance must update");
		check(NoRenderHooks.shouldNoRenderOverworldFog(), "overworld fog must be blocked");
		check(NoRenderHooks.shouldNoRenderNetherFog(), "nether fog must be blocked");
		check(NoRenderHooks.shouldNoRenderEndFog(), "end fog must be blocked");
		check(NoRenderHooks.shouldNoRenderWeather(), "weather must be blocked");
		check(NoRenderHooks.shouldNoRenderSky(), "sky must be blocked");
		check(NoRenderHooks.shouldNoRenderBiomeColors(), "biome colors must be blocked");

		// 4. 动画测试
		module.animationWater().set(true);
		module.animationLava().set(true);
		module.animationFire().set(true);
		module.animationPortals().set(true);
		module.animationSculkSensors().set(true);

		check(NoRenderHooks.shouldFreezeSpriteAnimation("minecraft:block/water_still"), "water still must freeze");
		check(NoRenderHooks.shouldFreezeSpriteAnimation("minecraft:block/water_flow"), "water flow must freeze");
		check(NoRenderHooks.shouldFreezeSpriteAnimation("minecraft:block/lava_flow"), "lava flow must freeze");
		check(NoRenderHooks.shouldFreezeSpriteAnimation("minecraft:block/fire_0"), "fire must freeze");
		check(NoRenderHooks.shouldFreezeSpriteAnimation("minecraft:block/nether_portal"), "portal must freeze");
		check(NoRenderHooks.shouldFreezeSpriteAnimation("minecraft:block/sculk_sensor_tendril_active"), "sculk sensor must freeze");
		check(!NoRenderHooks.shouldFreezeSpriteAnimation("minecraft:block/stone"), "stone must not freeze");

		// 再次禁用模块
		manager.setEnabled(module.id(), false);
		check(!NoRenderHooks.isEnabled(), "hooks must report disabled when module is disabled");
		check(!NoRenderHooks.shouldNoRenderItemFrames(), "hooks must be false when module disabled");
		check(!NoRenderHooks.shouldFreezeSpriteAnimation("minecraft:block/water_still"), "freeze hook must be false when module disabled");
	}

	private static void antiQuitModuleMetadataSettingsAndHooksFollowLifecycle() {
		ModuleManager manager = new ModuleManager();
		AntiQuitModule module = new AntiQuitModule();
		manager.register(module);
		AntiQuitHooks.install(module);

		// Metadata & defaults
		check(module.category().id().equals("player"), "anti quit must reside in player category");
		check(module.id().path().equals("anti_quit"), "module id path must be anti_quit");
		check(module.id().namespace().equals("client"), "module id namespace must be client");
		check(module.notification().value(), "notification must default to true");
		check(module.confirmDisconnect().value(), "confirm disconnect must default to true");
		check(module.confirmWindowClose().value(), "confirm window close must default to true");
		check(module.keybind().isPresent(), "anti quit must declare shortcut");

		// Disabled state
		check(!AntiQuitHooks.isEnabled(), "hooks must report disabled when module is disabled");
		check(!AntiQuitHooks.shouldConfirmDisconnect(), "confirm disconnect hook must be false when disabled");
		check(!AntiQuitHooks.shouldConfirmWindowClose(), "confirm window close hook must be false when disabled");
		check(AntiQuitHooks.handleWindowShouldClose(null, null), "window close must proceed when disabled");

		// Enable module
		manager.setEnabled(module.id(), true);
		check(AntiQuitHooks.isEnabled(), "hooks must report enabled when module is enabled");
		check(AntiQuitHooks.shouldConfirmDisconnect(), "confirm disconnect hook must be true when enabled");
		check(AntiQuitHooks.shouldConfirmWindowClose(), "confirm window close hook must be true when enabled");
		check(!AntiQuitHooks.handleWindowShouldClose(null, null), "window close must be intercepted when enabled");

		// Toggle individual settings
		module.confirmDisconnect().set(false);
		check(!AntiQuitHooks.shouldConfirmDisconnect(), "confirm disconnect hook must be false when setting is false");
		check(AntiQuitHooks.shouldConfirmWindowClose(), "confirm window close hook must remain true");

		module.confirmWindowClose().set(false);
		check(!AntiQuitHooks.shouldConfirmWindowClose(), "confirm window close hook must be false when setting is false");
		check(AntiQuitHooks.handleWindowShouldClose(null, null), "window close must proceed when confirmWindowClose is false");

		// Force quit behavior
		module.confirmWindowClose().set(true);
		AntiQuitHooks.setForceQuitting(true);
		check(AntiQuitHooks.isForceQuitting(), "force quitting state must report true");
		check(AntiQuitHooks.handleWindowShouldClose(null, null), "window close must proceed when force quitting");
		AntiQuitHooks.setForceQuitting(false);

		// Disable module
		manager.setEnabled(module.id(), false);
		check(!AntiQuitHooks.isEnabled(), "hooks must report disabled after disabling module");
		check(!AntiQuitHooks.shouldConfirmDisconnect(), "confirm disconnect must be false after disabling");
		check(!AntiQuitHooks.shouldConfirmWindowClose(), "confirm window close must be false after disabling");
	}

	private static void flightModuleMetadataSettingsAndPolicyCalculations() {
		ModuleManager manager = new ModuleManager();
		FlightModule module = new FlightModule();
		manager.register(module);
		FlightHooks.install(module);

		// Metadata & settings
		check(module.category().id().equals("movement"), "flight must reside in movement category");
		check(module.id().path().equals("flight"), "module id path must be flight");
		check(module.id().namespace().equals("client"), "module id namespace must be client");
		check(module.horizontalSpeed().value() == 1.0, "horizontal speed must default to 1.0");
		check(module.horizontalSpeed().minimum() == 0.1 && module.horizontalSpeed().maximum() == 10.0,
			"horizontal speed must range from 0.1 to 10.0");
		check(module.verticalSpeed().value() == 0.8, "vertical speed must default to 0.8");
		check(module.verticalSpeed().minimum() == 0.1 && module.verticalSpeed().maximum() == 10.0,
			"vertical speed must range from 0.1 to 10.0");
		check(module.spoofGround().value(), "spoof ground must default to true");
		check(module.notification().value(), "notification must default to true");
		check(module.keybind().isPresent(), "flight must declare shortcut");

		// FlightPolicy calculations
		FlightPolicy.Velocity2d zero = FlightPolicy.calculateHorizontalVelocity(0.0f, 0.0f, 0.0f, 1.0);
		check(zero.x() == 0.0 && zero.z() == 0.0, "zero input must yield zero horizontal velocity");

		FlightPolicy.Velocity2d fwd = FlightPolicy.calculateHorizontalVelocity(0.0f, 1.0f, 0.0f, 1.5);
		check(closeTo(fwd.x(), 0.0) && closeTo(fwd.z(), 1.5), "forward at yaw 0 must move in +Z");

		FlightPolicy.Velocity2d bwd = FlightPolicy.calculateHorizontalVelocity(0.0f, -1.0f, 0.0f, 1.5);
		check(closeTo(bwd.x(), 0.0) && closeTo(bwd.z(), -1.5), "backward at yaw 0 must move in -Z");

		FlightPolicy.Velocity2d strafeLeft = FlightPolicy.calculateHorizontalVelocity(0.0f, 0.0f, 1.0f, 1.5);
		check(closeTo(strafeLeft.x(), 1.5) && closeTo(strafeLeft.z(), 0.0), "strafe left at yaw 0 must move in +X (East)");

		FlightPolicy.Velocity2d strafeRight = FlightPolicy.calculateHorizontalVelocity(0.0f, 0.0f, -1.0f, 1.5);
		check(closeTo(strafeRight.x(), -1.5) && closeTo(strafeRight.z(), 0.0), "strafe right at yaw 0 must move in -X (West)");

		FlightPolicy.Velocity2d yaw90 = FlightPolicy.calculateHorizontalVelocity(90.0f, 1.0f, 0.0f, 2.0);
		check(closeTo(yaw90.x(), -2.0) && closeTo(yaw90.z(), 0.0), "forward at yaw 90 must move in -X");

		FlightPolicy.Velocity2d yaw90StrafeLeft = FlightPolicy.calculateHorizontalVelocity(90.0f, 0.0f, 1.0f, 2.0);
		check(closeTo(yaw90StrafeLeft.x(), 0.0) && closeTo(yaw90StrafeLeft.z(), 2.0), "strafe left at yaw 90 must move in +Z (South)");

		FlightPolicy.Velocity2d diag = FlightPolicy.calculateHorizontalVelocity(0.0f, 1.0f, 1.0f, 2.0);
		check(closeTo(Math.hypot(diag.x(), diag.z()), 2.0), "diagonal speed must normalize to target speed");

		check(FlightPolicy.calculateVerticalVelocity(true, false, 0.8) == 0.8, "jump must ascend at vertical speed");
		check(FlightPolicy.calculateVerticalVelocity(false, true, 0.8) == -0.8, "sneak must descend at vertical speed");
		check(FlightPolicy.calculateVerticalVelocity(true, true, 0.8) == 0.0, "jump + sneak must hover in place");
		check(FlightPolicy.calculateVerticalVelocity(false, false, 0.8) == 0.0, "no vertical input must hover in place");

		// FlightHooks & Packet Spoofing
		check(!FlightHooks.isEnabled(), "hooks must report disabled when module is disabled");
		check(!FlightHooks.shouldSpoofGround(), "spoofGround must report false when module is disabled");

		ServerboundMovePlayerPacket.Pos movePacket = new ServerboundMovePlayerPacket.Pos(10.0, 64.0, 20.0, false, false);
		check(FlightHooks.processOutgoingPacket(movePacket) == movePacket, "packets must remain unmodified when disabled");

		// Enable module
		manager.setEnabled(module.id(), true);
		check(FlightHooks.isEnabled(), "hooks must report enabled when module is enabled");
		check(FlightHooks.shouldSpoofGround(), "spoofGround must report true when module is enabled");

		// Abilities packet suppression
		Abilities abilities = new Abilities();
		abilities.flying = true;
		ServerboundPlayerAbilitiesPacket abilitiesPacket = new ServerboundPlayerAbilitiesPacket(abilities);
		check(FlightHooks.processOutgoingPacket(abilitiesPacket) == null, "flying abilities packet must be suppressed");

		// Ground spoofing
		var processed = FlightHooks.processOutgoingPacket(movePacket);
		check(processed instanceof ServerboundMovePlayerPacket, "processed packet must be a MovePlayerPacket");
		check(((ServerboundMovePlayerPacket) processed).isOnGround(), "move packet must be spoofed to onGround = true");

		// Disable spoof ground setting
		module.spoofGround().set(false);
		check(!FlightHooks.shouldSpoofGround(), "spoofGround hook must report false when setting is disabled");
		check(FlightHooks.processOutgoingPacket(movePacket) == movePacket, "packets must not be spoofed when spoofGround is false");

		// Disable module
		manager.setEnabled(module.id(), false);
		check(!FlightHooks.isEnabled(), "hooks must report disabled after module disabled");
	}

	private static void doubleRangeSettingNormalizationAndFractionMath() {
		// DoubleRange direct checks
		DoubleRange swapped = new DoubleRange(20.0, 5.0);
		check(swapped.min() == 5.0 && swapped.max() == 20.0, "DoubleRange must auto-order min and max");

		expectThrows(
			IllegalArgumentException.class,
			() -> new DoubleRange(Double.NaN, 10.0),
			"non-finite values must be rejected"
		);

		// DoubleRangeSetting checks
		DoubleRangeSetting setting = new DoubleRangeSetting(
			"test_range",
			"client.setting.test_range",
			5.0,
			20.0,
			1.0,
			100.0,
			1.0
		);
		check(setting.minimum() == 5.0, "setting min must initialize to 5.0");
		check(setting.maximum() == 20.0, "setting max must initialize to 20.0");
		check(setting.rangeMinimum() == 1.0, "range min must be 1.0");
		check(setting.rangeMaximum() == 100.0, "range max must be 100.0");
		check(setting.step() == 1.0, "step must be 1.0");

		check(closeTo(setting.minFraction(), (5.0 - 1.0) / 99.0), "minFraction calculation");
		check(closeTo(setting.maxFraction(), (20.0 - 1.0) / 99.0), "maxFraction calculation");

		// Change listeners
		AtomicLong changeCount = new AtomicLong();
		setting.addChangeListener(changeCount::incrementAndGet);

		setting.setRange(10.0, 30.0);
		check(setting.minimum() == 10.0 && setting.maximum() == 30.0, "setRange must update bounds");
		check(changeCount.get() == 1L, "change listener must fire once");

		// Step alignment & clamp
		setting.setRange(-5.0, 150.0);
		check(setting.minimum() == 1.0 && setting.maximum() == 100.0, "out-of-bounds range must clamp to range limits");

		// Setting fractions
		setting.setMinFraction(0.5);
		check(setting.minimum() == 51.0, "setMinFraction 0.5 must align to step on [1, 100]");

		// Random sampling
		setting.setRange(10.0, 15.0);
		for (int i = 0; i < 50; i++) {
			double val = setting.randomValue();
			check(val >= 10.0 && val <= 15.0, "randomValue must be within [10.0, 15.0]");
		}
	}

	private static void rangeSliderControlPointerHandling() {
		DoubleRangeSetting setting = new DoubleRangeSetting(
			"cps_range",
			"client.setting.cps_range",
			10.0,
			20.0,
			0.0,
			100.0,
			1.0
		);
		RangeSliderControl control = new RangeSliderControl(setting);
		control.layout(new Rect(0, 0, 100, 20));

		check(closeTo(control.minFraction(), 0.1), "initial minFraction must be 0.1");
		check(closeTo(control.maxFraction(), 0.2), "initial maxFraction must be 0.2");

		// Click closer to MIN (x = 5 => fraction 0.05)
		InputResult res1 = control.handleInput(new UiInputEvent.PointerPressed(5, 10, MouseButton.LEFT, 0));
		check(res1 == InputResult.CAPTURE_POINTER, "pointer press must capture pointer");
		check(control.activeTarget() == RangeSliderControl.DragTarget.MIN, "clicking near min must grab MIN handle");
		check(setting.minimum() == 5.0, "min value must update to 5.0");

		// Drag MIN to x = 8
		control.handleInput(new UiInputEvent.PointerDragged(8, 10, 3, 0, MouseButton.LEFT, 0));
		check(setting.minimum() == 8.0, "dragging must update min value to 8.0");

		// Release pointer
		control.handleInput(new UiInputEvent.PointerReleased(8, 10, MouseButton.LEFT, 0));
		check(control.activeTarget() == RangeSliderControl.DragTarget.NONE, "releasing pointer must clear active target");

		// Click closer to MAX (x = 80 => fraction 0.8)
		control.handleInput(new UiInputEvent.PointerPressed(80, 10, MouseButton.LEFT, 0));
		check(control.activeTarget() == RangeSliderControl.DragTarget.MAX, "clicking near max must grab MAX handle");
		check(setting.maximum() == 80.0, "max value must update to 80.0");
	}

	private static void doubleRangeConfigPersistence() {
		Path temporaryDirectory = null;
		try {
			temporaryDirectory = Files.createTempDirectory("edge-client-range-config-test");
			Path configPath = temporaryDirectory.resolve("edge-config.json");

			ModuleManager sourceManager = new ModuleManager();
			TrackingModule sourceModule = sourceManager.register(new TrackingModule("range_test"));
			DoubleRangeSetting sourceSetting = sourceModule.exposeRegister(
				new DoubleRangeSetting("cps_range", "client.setting.cps", 5.0, 20.0, 1.0, 100.0, 1.0)
			);
			JsonConfigService sourceConfig = new JsonConfigService(sourceManager, configPath);
			check(sourceConfig.load(), "missing config must initialize cleanly");

			sourceSetting.setRange(12.0, 35.0);
			check(Files.isRegularFile(configPath), "modifying range setting must write to disk");
			String json = Files.readString(configPath);
			check(json.contains("\"min\": 12.0") && json.contains("\"max\": 35.0"),
				"double range must serialize as JSON object with min and max");

			ModuleManager restoredManager = new ModuleManager();
			TrackingModule restoredModule = restoredManager.register(new TrackingModule("range_test"));
			DoubleRangeSetting restoredSetting = restoredModule.exposeRegister(
				new DoubleRangeSetting("cps_range", "client.setting.cps", 5.0, 20.0, 1.0, 100.0, 1.0)
			);
			JsonConfigService restoredConfig = new JsonConfigService(restoredManager, configPath);
			check(restoredConfig.load(), "config must load successfully");
			check(restoredSetting.minimum() == 12.0 && restoredSetting.maximum() == 35.0,
				"range setting min and max must be restored from disk");
		} catch (IOException error) {
			throw new AssertionError("double range config persistence failed", error);
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

	private static void airPlaceModuleMetadataSettingsAndPolicy() {
		AirPlaceModule module = new AirPlaceModule();
		check(module.category().id().equals("player"), "air place module must reside in player category");
		check(module.range().value() == 4.5, "range must default to 4.5");
		check(module.swing().value(), "swing must default to true");
		check(module.direction().value() == AirPlaceDirection.AUTO, "direction must default to AUTO");

		// Policy tests
		Vec3 eyePos = new Vec3(0.0, 64.0, 0.0);
		Vec3 lookVec = new Vec3(0.0, 0.0, 1.0);
		AirPlacePolicy.TargetPlacement target = AirPlacePolicy.calculatePlacement(
			eyePos,
			lookVec,
			4.0,
			AirPlaceDirection.AUTO,
			Direction.SOUTH
		);
		check(target.blockPos().equals(new BlockPos(0, 64, 4)), "target block pos must be (0, 64, 4)");
		check(target.direction() == Direction.NORTH, "auto direction looking straight south must face NORTH");

		check(AirPlacePolicy.resolveDirection(AirPlaceDirection.UP, lookVec, Direction.SOUTH) == Direction.UP,
			"UP direction mode must resolve to UP");
		check(AirPlacePolicy.resolveDirection(AirPlaceDirection.DOWN, lookVec, Direction.SOUTH) == Direction.DOWN,
			"DOWN direction mode must resolve to DOWN");
		check(AirPlacePolicy.resolveDirection(AirPlaceDirection.FACING, lookVec, Direction.SOUTH) == Direction.NORTH,
			"FACING direction mode must resolve opposite to player facing");

		AABB playerBox = new AABB(0.2, 64.0, 0.2, 0.8, 66.0, 0.8);
		check(!AirPlacePolicy.isPlacementAllowed(playerBox, new BlockPos(0, 64, 0)),
			"placement inside player bounding box must be disallowed");
		check(AirPlacePolicy.isPlacementAllowed(playerBox, new BlockPos(0, 64, 4)),
			"placement outside player bounding box must be allowed");
	}

	private static void autoClickerModuleMetadataSettingsAndDualScheduling() {
		AutoClickerModule module = new AutoClickerModule();
		check(module.category().id().equals("combat"), "auto clicker must reside in combat category");
		check(module.leftClick().value(), "left click must default to enabled");
		check(module.leftCps().minimum() == 8.0 && module.leftCps().maximum() == 14.0, "left CPS defaults to 8-14");
		check(!module.rightClick().value(), "right click must default to disabled");
		check(module.rightCps().minimum() == 10.0 && module.rightCps().maximum() == 20.0, "right CPS defaults to 10-20");
		check(module.holdOnly().value(), "hold only must default to true");

		AutoClickerController controller = new AutoClickerController();
		check(AutoClickerController.calculateIntervalMs(10.0) == 100L, "10 CPS must equate to 100ms interval");
		check(AutoClickerController.calculateIntervalMs(20.0) == 50L, "20 CPS must equate to 50ms interval");

		// Disabled check
		check(!controller.checkAndScheduleLeft(1000L, false, true, true, new DoubleRange(10.0, 10.0)),
			"disabled left click must not schedule");

		// Hold-only check
		check(!controller.checkAndScheduleLeft(1000L, true, false, true, new DoubleRange(10.0, 10.0)),
			"holdOnly=true with holding=false must not schedule");

		// Active scheduling
		check(controller.checkAndScheduleLeft(1000L, true, true, true, new DoubleRange(10.0, 10.0)),
			"first left click must trigger immediately");
		check(controller.nextLeftClickTimeMs() == 1100L, "next left click scheduled for 1100ms");
		check(!controller.checkAndScheduleLeft(1050L, true, true, true, new DoubleRange(10.0, 10.0)),
			"subsequent left click before cooldown must not trigger");
		check(controller.checkAndScheduleLeft(1100L, true, true, true, new DoubleRange(10.0, 10.0)),
			"left click at cooldown must trigger");

		// Concurrent dual scheduling at the same timestamp
		check(controller.checkAndScheduleRight(1100L, true, true, true, new DoubleRange(20.0, 20.0)),
			"right click must trigger independently at the same timestamp");
		check(controller.nextRightClickTimeMs() == 1150L, "next right click scheduled for 1150ms");
	}

	private static void cpsTrackerAndHudRendererCalculations() {
		CpsTracker.reset();
		long now = 100_000L;
		java.util.Deque<Long> clicks = new java.util.ArrayDeque<>();
		clicks.addLast(now - 1200L); // expired
		clicks.addLast(now - 900L);
		clicks.addLast(now - 500L);
		clicks.addLast(now - 100L);
		check(CpsTracker.calculateCps(clicks, now) == 3, "expired clicks (> 1000ms) must be pruned");

		CpsTracker.recordLeftClick();
		CpsTracker.recordLeftClick();
		check(CpsTracker.getLeftCps() >= 2, "recorded left clicks must increment left CPS");

		CpsTracker.recordRightClick();
		check(CpsTracker.getRightCps() >= 1, "recorded right clicks must increment right CPS");

		CpsTracker.reset();
		check(CpsTracker.getLeftCps() == 0 && CpsTracker.getRightCps() == 0, "reset must clear CPS queues");

		ClickGuiModule clickGui = new ClickGuiModule();
		check(clickGui.cpsHudEnabled().value(), "CPS HUD must default to enabled");
		check(clickGui.cpsHudPosition().value().equals(new HudPosition(0.0, 0.35)), "CPS HUD default position");

		CpsHudRenderer renderer = new CpsHudRenderer(
			clickGui.cpsHudEnabled(),
			clickGui.cpsHudPosition(),
			() -> false
		);
		check(renderer.measure().width() > 0 && renderer.measure().height() == 16, "CPS HUD measure must provide valid size");
	}

	private static void colorSettingHexParsingAndConversion() {
		ColorSetting setting = new ColorSetting(
			"test_color",
			"test.setting.color",
			0xFFFF0000,
			false
		);
		check(setting.rgb() == 0xFF0000, "initial RGB must be 0xFF0000");
		check(setting.hex().equals("FF0000"), "hex representation must default without leading hash");
		check(setting.red() == 255 && setting.green() == 0 && setting.blue() == 0, "channels must be 255, 0, 0");

		// Test setting hex string with '#' - silent strip
		setting.setFromHex("#00FF00");
		check(setting.rgb() == 0x00FF00, "setFromHex with '#' must strip hash and set 0x00FF00");
		check(setting.hex().equals("00FF00"), "hex must be 00FF00");
		check(setting.green() == 255 && setting.red() == 0, "green channel must be 255");

		// Test setting 3-character hex shorthand
		setting.setFromHex("00F");
		check(setting.rgb() == 0x0000FF, "3-digit hex '00F' must expand to 0x0000FF");

		// Test setting 8-character hex on alpha-enabled setting
		ColorSetting alphaSetting = new ColorSetting(
			"test_alpha_color",
			"test.setting.color",
			0x80FF0000,
			true
		);
		alphaSetting.setFromHex("80FF0000");
		check(alphaSetting.rgb() == 0xFF0000, "8-digit hex RGB must be 0xFF0000");
		check(alphaSetting.alpha() == 128, "8-digit hex alpha must be 128");

		// Test presets
		check(ColorSetting.PRESET_COLORS.size() == 5, "must have 5 preset colors");
		setting.set(ColorSetting.PRESET_COLORS.get(3));
		check(setting.rgb() == 0xFFFF00, "preset 3 must be yellow (0xFFFF00)");
	}

	private static void colorPickerControlHsvMathAndPresets() {
		ColorSetting setting = new ColorSetting(
			"picker_test",
			"test.setting.picker",
			0xFFFF0000,
			false
		);
		ColorPickerControl control = new ColorPickerControl(setting);
		control.layout(new Rect(0, 0, 180, 200));

		check(control.hexText().equals("FF0000"), "hex text must be FF0000");
		check(control.presetColors().size() == 5, "control must expose 5 presets");

		// Test Hue update
		control.setHue(120.0f); // Green
		control.setSaturation(1.0f);
		control.setValue(1.0f);
		check(setting.rgb() == 0x00FF00, "hue 120 with S=1, V=1 must give green");

		// Test setting from control hex edit
		control.onHexInputChanged("0000FF");
		check(setting.rgb() == 0x0000FF, "typing 0000FF must update setting to blue");

		// Test setting with leading '#' silent strip
		control.onHexInputChanged("#FFFF00");
		check(setting.rgb() == 0xFFFF00, "typing #FFFF00 must silently strip '#' and update setting");
	}

	private static void versionHudRendererMetadataAndMeasurements() {
		BooleanSetting enabled = new BooleanSetting("test_enabled", "test.enabled", true);
		HudPositionSetting position = new HudPositionSetting("test_pos", "test.pos", new HudPosition(0.0, 0.0));
		VersionHudRenderer renderer = new VersionHudRenderer(
			enabled,
			position,
			() -> false,
			"Edge Client v1.0.0"
		);
		check(renderer.displayText().equals("Edge Client v1.0.0"), "displayText must match configured text");
		check(renderer.measure().height() == 16, "measured height must be 16");
		check(renderer.measure().width() > 0, "measured width must be positive");
	}

	private static void allRegisteredModulesAndSettingsHaveTranslations() {
		ModuleManager manager = new ModuleManager();
		BuiltInModules.register(manager);
		Map<String, String> zhMap = io.qzz.iie.i18n.ClientI18n.getMap("zh_cn");
		Map<String, String> enMap = io.qzz.iie.i18n.ClientI18n.getMap("en_us");

		List<String> missingZh = new ArrayList<>();
		List<String> missingEn = new ArrayList<>();

		for (Module module : manager.modules()) {
			checkKey(module.metadata().nameTranslationKey(), zhMap, enMap, missingZh, missingEn);
			checkKey(module.metadata().descriptionTranslationKey(), zhMap, enMap, missingZh, missingEn);
			checkKey("client.category." + module.category().id(), zhMap, enMap, missingZh, missingEn);

			for (Setting<?> setting : module.settings()) {
				checkKey(setting.translationKey(), zhMap, enMap, missingZh, missingEn);
				if (setting instanceof ChoiceSetting<?> choice) {
					for (ChoiceOption<?> option : choice.options()) {
						checkKey(option.translationKey(), zhMap, enMap, missingZh, missingEn);
					}
				}
			}
		}

		if (!missingZh.isEmpty() || !missingEn.isEmpty()) {
			throw new AssertionError("Missing translation keys!\nZH: " + missingZh + "\nEN: " + missingEn);
		}
	}

	private static void checkKey(
		String key,
		Map<String, String> zh,
		Map<String, String> en,
		List<String> missingZh,
		List<String> missingEn
	) {
		if (key == null || key.isBlank()) return;
		if (!zh.containsKey(key)) {
			missingZh.add(key);
		}
		if (!en.containsKey(key)) {
			missingEn.add(key);
		}
	}

	private static void freeLookPolicyRotationAndInterpolation() {
		FreeLookPolicy policy = new FreeLookPolicy();
		check(!policy.isActive(), "policy starts inactive");

		policy.activate(180.0f, 10.0f);
		check(policy.isActive(), "policy becomes active");
		check(closeTo(policy.cameraYaw(), 180.0), "initial yaw is 180");
		check(closeTo(policy.cameraPitch(), 10.0), "initial pitch is 10");

		policy.tick();
		policy.turn(10.0, -20.0, false, false);
		check(closeTo(policy.cameraYaw(), 181.5), "accumulated yaw without invert");
		check(closeTo(policy.cameraPitch(), 7.0), "accumulated pitch without invert");

		// Test invert yaw and pitch
		policy.turn(10.0, 10.0, true, true);
		check(closeTo(policy.cameraYaw(), 180.0), "accumulated yaw with invert");
		check(closeTo(policy.cameraPitch(), 5.5), "accumulated pitch with invert");

		// Test pitch clamp [-90, 90]
		policy.turn(0.0, 1000.0, false, false);
		check(closeTo(policy.cameraPitch(), 90.0), "pitch clamps to 90");
		policy.turn(0.0, -2000.0, false, false);
		check(closeTo(policy.cameraPitch(), -90.0), "pitch clamps to -90");

		// Test partial tick interpolation
		policy.activate(0.0f, 0.0f);
		policy.tick();
		policy.turn(100.0, 0.0, false, false); // cameraYaw becomes 15.0f, prev was 0.0f
		check(closeTo(policy.renderYaw(0.5f), 7.5), "renderYaw at 0.5 partialTicks interpolates halfway");

		// Module metadata and defaults
		FreeLookModule module = new FreeLookModule();
		check(module.category().id().equals("render"), "free look derives category render from package");
		check(module.autoThirdPerson().value(), "auto third person defaults to true");
		check(!module.holdMode().value(), "hold mode defaults to false");
		check(module.smoothTransition().value(), "smooth transition defaults to true");
		check(!module.invertPitch().value(), "invert pitch defaults to false");
		check(!module.invertYaw().value(), "invert yaw defaults to false");
		check(module.keybind().isPresent() && !module.keybind().get().value().isBound(), "free look keybind defaults to unbound");
	}

	private static void copyNbtPolicyDecisionsAndSizeLimits() {
		// 模块未启用：保持原始输入
		check(
			!CopyNbtPolicy.shouldIncludeData(false, true, false, false, 2048, 0, false, null, null),
			"disabled module must preserve false includeData"
		);
		check(
			CopyNbtPolicy.shouldIncludeData(false, true, true, false, 2048, 0, false, null, null),
			"disabled module must preserve true includeData"
		);

		// 非创造模式：保持原始输入
		check(
			!CopyNbtPolicy.shouldIncludeData(true, false, false, false, 2048, 0, false, null, null),
			"non-creative player must preserve false includeData"
		);
		check(
			CopyNbtPolicy.shouldIncludeData(true, false, true, false, 2048, 0, false, null, null),
			"non-creative player must preserve true includeData"
		);

		// 创造模式且模块启用：默认允许所有 NBT（limitSize = false, filterBlocks = false）
		check(
			CopyNbtPolicy.shouldIncludeData(true, true, false, false, 2048, 999999, false, BlockNbtCategory.CONTAINERS, java.util.Set.of()),
			"creative mode without limit must allow NBT copying"
		);

		// 创造模式且开启大小限制：未超出限制时允许
		check(
			CopyNbtPolicy.shouldIncludeData(true, true, false, true, 2048, 1024, false, null, null),
			"creative mode under size limit must allow NBT copying"
		);

		// 创造模式且开启大小限制：超出限制时拒绝
		check(
			!CopyNbtPolicy.shouldIncludeData(true, true, false, true, 2048, 4096, false, null, null),
			"creative mode exceeding size limit must reject NBT copying"
		);

		// 创造模式且开启大小限制：未知大小（<=0）允许
		check(
			CopyNbtPolicy.shouldIncludeData(true, true, false, true, 2048, 0, false, null, null),
			"creative mode with unknown size must allow NBT copying"
		);

		// 开启方块分类过滤：允许的分类复制，未勾选的分类拦截
		java.util.Set<BlockNbtCategory> allowed = java.util.Set.of(BlockNbtCategory.CONTAINERS, BlockNbtCategory.SPECIAL);
		check(
			CopyNbtPolicy.shouldIncludeData(true, true, false, false, 2048, 0, true, BlockNbtCategory.CONTAINERS, allowed),
			"allowed category must permit NBT copying"
		);
		check(
			!CopyNbtPolicy.shouldIncludeData(true, true, false, false, 2048, 0, true, BlockNbtCategory.REDSTONE, allowed),
			"disallowed category must reject NBT copying"
		);
	}

	private static void copyNbtModuleMetadataAndDefaults() {
		CopyNbtModule module = new CopyNbtModule();
		check(module.category().id().equals("player"), "copy nbt must reside in player category");
		check(!module.limitSize().value(), "limitSize must default to false");
		check(module.maxSizeKb().value() == 2048.0, "maxSizeKb must default to 2048 KB");
		check(module.maxSizeKb().minimum() == 1.0, "maxSizeKb minimum must be 1 KB");
		check(module.maxSizeKb().maximum() == 2048.0, "maxSizeKb maximum must be 2048 KB");
		check(!module.maxSizeKb().isVisible(), "maxSizeKb must be hidden when limitSize is false");

		// 方块分类过滤与子选项默认值
		check(!module.filterBlocks().value(), "filterBlocks must default to false");
		check(!module.allowSpecial().isVisible(), "sub-settings must be hidden when filterBlocks is false");
		check(!module.allowDecorative().isVisible(), "sub-settings must be hidden when filterBlocks is false");
		check(!module.allowContainers().isVisible(), "sub-settings must be hidden when filterBlocks is false");
		check(!module.allowProcessing().isVisible(), "sub-settings must be hidden when filterBlocks is false");
		check(!module.allowRedstone().isVisible(), "sub-settings must be hidden when filterBlocks is false");
		check(!module.allowAdvanced().isVisible(), "sub-settings must be hidden when filterBlocks is false");
		check(!module.allowOther().isVisible(), "sub-settings must be hidden when filterBlocks is false");

		// 开启 filterBlocks 后子选项变为可见
		module.filterBlocks().set(true);
		check(module.allowSpecial().isVisible(), "sub-settings must be visible when filterBlocks is true");
		check(module.allowContainers().isVisible(), "sub-settings must be visible when filterBlocks is true");
		check(module.allowedCategories().size() == 7, "all categories must be allowed by default");

		// 禁用容器类后 allowedCategories 发生变化
		module.allowContainers().set(false);
		check(!module.allowedCategories().contains(BlockNbtCategory.CONTAINERS), "disabled category must not be in allowed set");

		check(module.copyEntityNbt().value(), "copyEntityNbt must default to true");
		check(module.keybind().isPresent() && !module.keybind().get().value().isBound(), "keybind must default to unbound");
		check(module.notification().value(), "notification must default to true");

		// 测试生命周期
		ModuleManager manager = new ModuleManager();
		manager.register(module);
		check(!CopyNbtHooks.isEnabled(), "hooks must not be active before module enable");
		manager.setEnabled(module.id(), true);
		check(CopyNbtHooks.isEnabled(), "hooks must be active after module enable");
		manager.setEnabled(module.id(), false);
		check(!CopyNbtHooks.isEnabled(), "hooks must not be active after module disable");
	}
}
