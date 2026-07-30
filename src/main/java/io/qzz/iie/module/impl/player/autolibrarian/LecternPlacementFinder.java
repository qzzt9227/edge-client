package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * 查找离玩家最近且满足原版放置条件的讲台位置。
 */
final class LecternPlacementFinder {
	Placement find(
		ClientLevel level,
		LocalPlayer player,
		Villager villager,
		ItemStack lecternStack,
		int radius
	) {
		BlockPos center = villager.blockPosition();
		Placement best = null;
		double bestDistance = Double.MAX_VALUE;
		for (int deltaY = -radius; deltaY <= radius; deltaY++) {
			for (int deltaX = -radius; deltaX <= radius; deltaX++) {
				for (int deltaZ = -radius; deltaZ <= radius; deltaZ++) {
					if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ
						> radius * radius) {
						continue;
					}
					Placement placement = validate(
						level,
						player,
						lecternStack,
						center.offset(deltaX, deltaY, deltaZ)
					);
					if (placement == null) {
						continue;
					}
					double distance = player.getEyePosition().distanceToSqr(
						placement.hitResult().getLocation()
					);
					if (distance < bestDistance) {
						best = placement;
						bestDistance = distance;
					}
				}
			}
		}
		return best;
	}

	Placement findAt(
		ClientLevel level,
		LocalPlayer player,
		Villager villager,
		ItemStack lecternStack,
		int radius,
		BlockPos candidate
	) {
		BlockPos center = villager.blockPosition();
		long deltaX = candidate.getX() - center.getX();
		long deltaY = candidate.getY() - center.getY();
		long deltaZ = candidate.getZ() - center.getZ();
		if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ
			> (long) radius * radius) {
			return null;
		}
		return validate(level, player, lecternStack, candidate);
	}

	private Placement validate(
		ClientLevel level,
		LocalPlayer player,
		ItemStack stack,
		BlockPos candidate
	) {
		BlockState targetState = level.getBlockState(candidate);
		BlockPos supportPosition = candidate.below();
		BlockState supportState = level.getBlockState(supportPosition);
		if (!targetState.canBeReplaced()
			|| !supportState.isFaceSturdy(level, supportPosition, Direction.UP)
			|| !player.isWithinBlockInteractionRange(supportPosition, 0.0)) {
			return null;
		}

		Vec3 hitLocation = Vec3.atBottomCenterOf(candidate);
		BlockHitResult hit = new BlockHitResult(
			hitLocation,
			Direction.UP,
			supportPosition,
			false
		);
		BlockPlaceContext context =
			new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit);
		BlockState lecternState = Blocks.LECTERN.defaultBlockState();
		if (!context.getClickedPos().equals(candidate)
			|| !lecternState.canSurvive(level, candidate)
			|| !level.isUnobstructed(
				lecternState,
				candidate,
				CollisionContext.of(player)
			)) {
			return null;
		}

		BlockHitResult visibility = level.clip(new ClipContext(
			player.getEyePosition(),
			hitLocation,
			ClipContext.Block.OUTLINE,
			ClipContext.Fluid.NONE,
			player
		));
		if (visibility.getType() != HitResult.Type.MISS
			&& !visibility.getBlockPos().equals(supportPosition)
			&& !visibility.getBlockPos().equals(candidate)) {
			return null;
		}
		return new Placement(candidate.immutable(), hit);
	}

	record Placement(BlockPos position, BlockHitResult hitResult) {
	}
}
