package io.qzz.iie.module.impl.player.airplace;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class AirPlaceModule extends Module {
	private final DoubleSetting range = setting(new DoubleSetting(
		"range",
		"client.setting.air_place.range",
		4.5,
		1.0,
		6.0,
		0.1
	));
	private final BooleanSetting swing = setting(new BooleanSetting(
		"swing",
		"client.setting.air_place.swing",
		true
	));
	private final ChoiceSetting<AirPlaceDirection> direction = setting(new ChoiceSetting<>(
		"direction",
		"client.setting.air_place.direction",
		AirPlaceDirection.AUTO,
		List.of(
			option("auto", AirPlaceDirection.AUTO),
			option("up", AirPlaceDirection.UP),
			option("down", AirPlaceDirection.DOWN),
			option("facing", AirPlaceDirection.FACING)
		)
	));
	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public AirPlaceModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "air_place"),
			"client.module.air_place.name",
			"client.module.air_place.description",
			220
		));
	}

	public DoubleSetting range() {
		return range;
	}

	public BooleanSetting swing() {
		return swing;
	}

	public ChoiceSetting<AirPlaceDirection> direction() {
		return direction;
	}

	@Override
	protected void onClientTick() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null || client.gameMode == null) {
			return;
		}
		if (!client.options.keyUse.isDown()) {
			return;
		}
		if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.BLOCK) {
			return;
		}

		ItemStack mainHand = client.player.getMainHandItem();
		ItemStack offHand = client.player.getOffhandItem();
		InteractionHand hand = null;
		if (mainHand.getItem() instanceof BlockItem) {
			hand = InteractionHand.MAIN_HAND;
		} else if (offHand.getItem() instanceof BlockItem) {
			hand = InteractionHand.OFF_HAND;
		}
		if (hand == null) {
			return;
		}

		Vec3 eyePos = client.player.getEyePosition(1.0F);
		Vec3 lookVec = client.player.getViewVector(1.0F);
		AirPlacePolicy.TargetPlacement target = AirPlacePolicy.calculatePlacement(
			eyePos,
			lookVec,
			range.value(),
			direction.value(),
			client.player.getDirection()
		);

		if (!client.level.getBlockState(target.blockPos()).canBeReplaced()) {
			return;
		}
		if (!AirPlacePolicy.isPlacementAllowed(client.player.getBoundingBox(), target.blockPos())) {
			return;
		}

		BlockHitResult hitResult = new BlockHitResult(
			target.hitVec(),
			target.direction(),
			target.blockPos(),
			false
		);
		InteractionResult result = client.gameMode.useItemOn(client.player, hand, hitResult);
		if (result.consumesAction() && swing.value()) {
			client.player.swing(hand);
		}
	}

	private static ChoiceOption<AirPlaceDirection> option(String id, AirPlaceDirection value) {
		return new ChoiceOption<>(
			id,
			"client.option.air_place." + id,
			value
		);
	}
}
