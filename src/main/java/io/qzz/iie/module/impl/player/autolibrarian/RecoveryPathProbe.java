package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 对前方一个移动步长做原版碰撞、净高、高差和危险检查。
 */
final class RecoveryPathProbe {
	private static final double STEP_LENGTH = 0.45;

	DirectRecoveryPath.Action probe(
		ClientLevel level,
		LocalPlayer player,
		Vec3 destination,
		BlockPos scanCenter,
		int radius
	) {
		Vec3 current = player.position();
		Vec3 horizontal =
			new Vec3(destination.x - current.x, 0.0, destination.z - current.z);
		double distance = horizontal.length();
		if (distance < 0.01) {
			return DirectRecoveryPath.Action.WALK;
		}
		Vec3 next =
			current.add(horizontal.scale(Math.min(STEP_LENGTH, distance) / distance));
		if (next.distanceToSqr(Vec3.atCenterOf(scanCenter)) > (double) radius * radius) {
			return DirectRecoveryPath.Action.BLOCKED;
		}

		int currentFeetY = Mth.floor(current.y + 0.01);
		int blockX = Mth.floor(next.x);
		int blockZ = Mth.floor(next.z);
		for (int floorDelta = 1; floorDelta >= -1; floorDelta--) {
			int feetY = currentFeetY + floorDelta;
			BlockPos floorPosition = new BlockPos(blockX, feetY - 1, blockZ);
			var floorState = level.getBlockState(floorPosition);
			if (!floorState.isFaceSturdy(level, floorPosition, Direction.UP)) {
				continue;
			}
			BlockPos feetPosition = floorPosition.above();
			boolean magma = floorState.is(Blocks.MAGMA_BLOCK);
			boolean hazard = isDangerous(floorState.getBlock())
				|| isDangerous(level.getBlockState(feetPosition).getBlock())
				|| isDangerous(level.getBlockState(feetPosition.above()).getBlock());
			Vec3 candidate = new Vec3(next.x, feetY, next.z);
			boolean standing = level.noCollision(
				player,
				player.getDimensions(Pose.STANDING).makeBoundingBox(candidate)
			);
			boolean crouching = level.noCollision(
				player,
				player.getDimensions(Pose.CROUCHING).makeBoundingBox(candidate)
			);
			return DirectRecoveryPath.decide(new DirectRecoveryPath.Cell(
				floorDelta,
				true,
				standing,
				crouching,
				magma,
				hazard
			));
		}
		return DirectRecoveryPath.Action.BLOCKED;
	}

	private static boolean isDangerous(Block block) {
		return block == Blocks.LAVA
			|| block == Blocks.FIRE
			|| block == Blocks.SOUL_FIRE
			|| block == Blocks.CACTUS
			|| block == Blocks.POWDER_SNOW
			|| block == Blocks.CAMPFIRE
			|| block == Blocks.SOUL_CAMPFIRE;
	}
}
