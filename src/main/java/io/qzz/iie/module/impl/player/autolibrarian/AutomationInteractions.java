package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 封装原版放置、交易交互和持续挖掘调用。
 */
final class AutomationInteractions {
	private final Minecraft client;

	AutomationInteractions(Minecraft client) {
		this.client = client;
	}

	PlaceResult placeLectern(BlockHitResult hit) {
		LocalPlayer player = client.player;
		if (player == null || !player.getInventory().getSelectedItem().is(Items.LECTERN)) {
			return PlaceResult.NO_LECTERN;
		}
		if (!player.isWithinBlockInteractionRange(hit.getBlockPos(), 0.0)) {
			return PlaceResult.OUT_OF_RANGE;
		}
		InteractionResult result =
			client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
		if (!result.consumesAction()) {
			return PlaceResult.REJECTED;
		}
		player.swing(InteractionHand.MAIN_HAND);
		return PlaceResult.STARTED;
	}

	TradeOpenResult openTrade(Villager villager) {
		LocalPlayer player = client.player;
		if (!player.isWithinEntityInteractionRange(villager, 0.0)
			|| !player.hasLineOfSight(villager)) {
			return TradeOpenResult.OUT_OF_RANGE;
		}
		Vec3 location =
			villager.position().add(0.0, villager.getBbHeight() * 0.55, 0.0);
		InteractionResult result = client.gameMode.interact(
			player,
			villager,
			new EntityHitResult(villager, location),
			InteractionHand.MAIN_HAND
		);
		if (!result.consumesAction()) {
			return TradeOpenResult.REJECTED;
		}
		player.swing(InteractionHand.MAIN_HAND);
		return TradeOpenResult.STARTED;
	}

	BreakResult continueBreaking(BlockPos position, boolean firstTick) {
		LocalPlayer player = client.player;
		if (!isLecternPresent(position)) {
			client.gameMode.stopDestroyBlock();
			return BreakResult.COMPLETE;
		}
		if (!player.isWithinBlockInteractionRange(position, 0.0)) {
			client.gameMode.stopDestroyBlock();
			return BreakResult.OUT_OF_RANGE;
		}
		Direction face = breakFace(player, position);
		boolean progressing = firstTick
			? client.gameMode.startDestroyBlock(position, face)
			: client.gameMode.continueDestroyBlock(position, face);
		if (!progressing) {
			client.gameMode.stopDestroyBlock();
			return BreakResult.REJECTED;
		}
		player.swing(InteractionHand.MAIN_HAND);
		return BreakResult.PROGRESS;
	}

	boolean isLecternPresent(BlockPos position) {
		return position != null
			&& client.level.getBlockState(position).is(Blocks.LECTERN);
	}

	void stopBreaking() {
		if (client.gameMode != null) {
			client.gameMode.stopDestroyBlock();
		}
	}

	private Direction breakFace(LocalPlayer player, BlockPos position) {
		BlockHitResult hit = client.level.clip(new ClipContext(
			player.getEyePosition(),
			Vec3.atCenterOf(position),
			ClipContext.Block.OUTLINE,
			ClipContext.Fluid.NONE,
			player
		));
		return hit.getType() == HitResult.Type.BLOCK
			&& hit.getBlockPos().equals(position)
			? hit.getDirection()
			: Direction.UP;
	}

	enum PlaceResult {
		STARTED,
		NO_LECTERN,
		OUT_OF_RANGE,
		REJECTED
	}

	enum TradeOpenResult {
		STARTED,
		OUT_OF_RANGE,
		REJECTED
	}

	enum BreakResult {
		PROGRESS,
		COMPLETE,
		OUT_OF_RANGE,
		REJECTED
	}
}
