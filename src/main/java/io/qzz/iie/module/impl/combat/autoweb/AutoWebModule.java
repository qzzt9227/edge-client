package io.qzz.iie.module.impl.combat.autoweb;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.HotbarMode;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.InventoryMode;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.PlacementCadence;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.PlacementPattern;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.TargetPriority;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.TargetType;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;

import java.util.List;

public final class AutoWebModule extends Module {
	private final AutoWebMinecraftController controller = new AutoWebMinecraftController();
	private final ChoiceSetting<TargetPriority> targetPriority = setting(new ChoiceSetting<>(
		"target_priority",
		"client.setting.auto_web.target_priority",
		TargetPriority.NEAREST,
		List.of(
			option("nearest", TargetPriority.NEAREST),
			option("lowest_health", TargetPriority.LOWEST_HEALTH),
			option("crosshair", TargetPriority.CROSSHAIR)
		)
	));
	private final ChoiceSetting<TargetType> targetType = setting(new ChoiceSetting<>(
		"target_type",
		"client.setting.auto_web.target_type",
		TargetType.PLAYER,
		List.of(
			option("player", TargetType.PLAYER),
			option("friendly", TargetType.FRIENDLY),
			option("hostile", TargetType.HOSTILE),
			option("all", TargetType.ALL),
			option("neutral", TargetType.NEUTRAL)
		)
	));
	private final ChoiceSetting<PlacementPattern> placementPattern = setting(new ChoiceSetting<>(
		"placement_pattern",
		"client.setting.auto_web.placement_pattern",
		PlacementPattern.FEET,
		List.of(
			option("feet", PlacementPattern.FEET),
			option("feet_head", PlacementPattern.FEET_AND_HEAD),
			option("surround", PlacementPattern.SURROUND)
		)
	));
	private final DoubleSetting range = setting(new DoubleSetting(
		"range",
		"client.setting.auto_web.range",
		3.0,
		1.0,
		6.0,
		1.0
	));
	private final DoubleSetting rotationTicks = setting(new DoubleSetting(
		"rotation_ticks",
		"client.setting.auto_web.rotation_ticks",
		2.0,
		1.0,
		20.0,
		0.1
	));
	private final ChoiceSetting<HotbarMode> hotbarMode = setting(new ChoiceSetting<>(
		"hotbar_mode",
		"client.setting.auto_web.hotbar_mode",
		HotbarMode.SILENT,
		List.of(
			option("silent", HotbarMode.SILENT),
			option("visible", HotbarMode.VISIBLE),
			option("held_only", HotbarMode.HELD_ONLY)
		)
	));
	private final BooleanSetting checkInventory = setting(new BooleanSetting(
		"check_inventory",
		"client.setting.auto_web.check_inventory",
		false
	));
	private final ChoiceSetting<InventoryMode> inventoryMode = setting(new ChoiceSetting<>(
		"inventory_mode",
		"client.setting.auto_web.inventory_mode",
		InventoryMode.SILENT_SELECTED_RESTORE,
		List.of(
			option("temporary_restore", InventoryMode.TEMPORARY_RESTORE),
			option("move_to_empty", InventoryMode.MOVE_TO_EMPTY),
			option("silent_selected_restore", InventoryMode.SILENT_SELECTED_RESTORE)
		)
	));
	private final ChoiceSetting<PlacementCadence> placementCadence = setting(new ChoiceSetting<>(
		"placement_cadence",
		"client.setting.auto_web.placement_cadence",
		PlacementCadence.ONE_PER_ROTATION,
		List.of(
			option("one_per_rotation", PlacementCadence.ONE_PER_ROTATION),
			option("all_after_rotation", PlacementCadence.ALL_AFTER_ROTATION),
			option("interval", PlacementCadence.INTERVAL)
		)
	));
	private final DoubleSetting placementInterval = setting(new DoubleSetting(
		"placement_interval",
		"client.setting.auto_web.placement_interval",
		1.0,
		0.1,
		20.0,
		0.1
	));
	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public AutoWebModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "auto_web"),
			"client.module.auto_web.name",
			"client.module.auto_web.description",
			100
		));
	}

	public ChoiceSetting<TargetPriority> targetPriority() {
		return targetPriority;
	}

	public ChoiceSetting<TargetType> targetType() {
		return targetType;
	}

	public ChoiceSetting<PlacementPattern> placementPattern() {
		return placementPattern;
	}

	public DoubleSetting range() {
		return range;
	}

	public DoubleSetting rotationTicks() {
		return rotationTicks;
	}

	public ChoiceSetting<HotbarMode> hotbarMode() {
		return hotbarMode;
	}

	public BooleanSetting checkInventory() {
		return checkInventory;
	}

	public ChoiceSetting<InventoryMode> inventoryMode() {
		return inventoryMode;
	}

	public ChoiceSetting<PlacementCadence> placementCadence() {
		return placementCadence;
	}

	public DoubleSetting placementInterval() {
		return placementInterval;
	}

	@Override
	protected void onClientTick() {
		controller.tick(this);
	}

	@Override
	protected void onDisable() {
		controller.reset(net.minecraft.client.Minecraft.getInstance());
	}

	private static <T> ChoiceOption<T> option(String id, T value) {
		return new ChoiceOption<>(
			id,
			"client.option.auto_web." + id,
			value
		);
	}
}
