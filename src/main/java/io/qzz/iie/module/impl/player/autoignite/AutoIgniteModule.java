package io.qzz.iie.module.impl.player.autoignite;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.ItemPriority;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.ItemSource;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.TargetHandling;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;

import java.util.List;

public final class AutoIgniteModule extends Module {
	private final AutoIgniteController controller = new AutoIgniteController();
	private final ChoiceSetting<ItemSource> itemSource = setting(new ChoiceSetting<>(
		"item_source",
		"client.setting.auto_ignite.item_source",
		ItemSource.HOTBAR,
		List.of(
			option("hotbar", "source.hotbar", ItemSource.HOTBAR),
			option("silent_inventory", "source.silent_inventory", ItemSource.SILENT_INVENTORY)
		)
	));
	private final ChoiceSetting<ItemPriority> itemPriority = setting(new ChoiceSetting<>(
		"item_priority",
		"client.setting.auto_ignite.item_priority",
		ItemPriority.FLINT_FIRST,
		List.of(
			option("flint_first", "priority.flint_first", ItemPriority.FLINT_FIRST),
			option("fire_charge_first", "priority.fire_charge_first", ItemPriority.FIRE_CHARGE_FIRST),
			option("flint_only", "priority.flint_only", ItemPriority.FLINT_ONLY),
			option("fire_charge_only", "priority.fire_charge_only", ItemPriority.FIRE_CHARGE_ONLY)
		)
	));
	private final BooleanSetting restoreAfterFlint = setting(new BooleanSetting(
		"restore_after_flint",
		"client.setting.auto_ignite.restore_after_flint",
		true
	));
	private final BooleanSetting cameraFollows = setting(new BooleanSetting(
		"camera_follows",
		"client.setting.auto_ignite.camera_follows",
		false
	));
	private final DoubleSetting rotationTicks = setting(new DoubleSetting(
		"rotation_ticks",
		"client.setting.auto_ignite.rotation_ticks",
		1.0,
		1.0,
		10.0,
		1.0
	));
	private final ChoiceSetting<TargetHandling> targetHandling = setting(new ChoiceSetting<>(
		"target_handling",
		"client.setting.auto_ignite.target_handling",
		TargetHandling.LATEST_ONLY,
		List.of(
			option("latest_only", "target.latest_only", TargetHandling.LATEST_ONLY),
			option("queue", "target.queue", TargetHandling.QUEUE)
		)
	));
	private final BooleanSetting strictInteraction = setting(new BooleanSetting(
		"strict_interaction",
		"client.setting.auto_ignite.strict_interaction",
		false
	));
	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public AutoIgniteModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "auto_ignite"),
			"client.module.auto_ignite.name",
			"client.module.auto_ignite.description",
			210
		));
	}

	public ChoiceSetting<ItemSource> itemSource() {
		return itemSource;
	}

	public ChoiceSetting<ItemPriority> itemPriority() {
		return itemPriority;
	}

	public BooleanSetting restoreAfterFlint() {
		return restoreAfterFlint;
	}

	public BooleanSetting cameraFollows() {
		return cameraFollows;
	}

	public DoubleSetting rotationTicks() {
		return rotationTicks;
	}

	public ChoiceSetting<TargetHandling> targetHandling() {
		return targetHandling;
	}

	public BooleanSetting strictInteraction() {
		return strictInteraction;
	}

	void recordPlacementCandidate(long packedBlockPos) {
		controller.recordPlacementCandidate(packedBlockPos);
	}

	@Override
	protected void onClientTick() {
		controller.tick(this);
	}

	@Override
	protected void onDisable() {
		controller.reset(net.minecraft.client.Minecraft.getInstance());
	}

	private static <T> ChoiceOption<T> option(String id, String translationSuffix, T value) {
		return new ChoiceOption<>(
			id,
			"client.option.auto_ignite." + translationSuffix,
			value
		);
	}
}
