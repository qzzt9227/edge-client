package io.qzz.iie.module.impl.combat.bedaura;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class BedAuraPlanner {

	public record PlacementCandidate(BlockPos headPos, BlockPos footPos, Direction facing, BlockHitResult hitResult) {}

	public Optional<LivingEntity> findTarget(ClientLevel level, LocalPlayer player, double range) {
		double rangeSq = range * range;
		LivingEntity bestTarget = null;
		double bestDistSq = Double.MAX_VALUE;

		for (var entity : level.entitiesForRendering()) {
			if (!(entity instanceof LivingEntity living) || living == player || !living.isAlive()) {
				continue;
			}
			if (living instanceof Player targetPlayer && (targetPlayer.isCreative() || targetPlayer.isSpectator())) {
				continue;
			}
			double distSq = player.distanceToSqr(living);
			if (distSq <= rangeSq && distSq < bestDistSq) {
				bestDistSq = distSq;
				bestTarget = living;
			}
		}
		return Optional.ofNullable(bestTarget);
	}

	public Optional<BlockHitResult> findPlacedBed(ClientLevel level, LocalPlayer player, LivingEntity target, double range) {
		double rangeSq = range * range;
		BlockPos center = target != null ? target.blockPosition() : player.blockPosition();
		int radius = (int) Math.ceil(range);

		BlockPos bestBed = null;
		double bestDistSq = Double.MAX_VALUE;

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -2; dy <= 2; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					BlockPos pos = center.offset(dx, dy, dz);
					if (player.distanceToSqr(Vec3.atCenterOf(pos)) > rangeSq) {
						continue;
					}
					BlockState state = level.getBlockState(pos);
					if (state.getBlock() instanceof BedBlock) {
						double distSq = target != null ? target.distanceToSqr(Vec3.atCenterOf(pos)) : player.distanceToSqr(Vec3.atCenterOf(pos));
						if (distSq < bestDistSq) {
							bestDistSq = distSq;
							bestBed = pos;
						}
					}
				}
			}
		}

		if (bestBed != null) {
			return Optional.of(new BlockHitResult(
				Vec3.atCenterOf(bestBed),
				Direction.UP,
				bestBed,
				false
			));
		}
		return Optional.empty();
	}

	public Optional<PlacementCandidate> findPlacementSpot(ClientLevel level, LocalPlayer player, LivingEntity target, double range) {
		double rangeSq = range * range;
		BlockPos targetPos = target.blockPosition();
		List<PlacementCandidate> candidates = new ArrayList<>();

		// 检查目标周围 1-3 格的放置候选点
		for (int dx = -3; dx <= 3; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -3; dz <= 3; dz++) {
					BlockPos footPos = targetPos.offset(dx, dy, dz);
					if (player.distanceToSqr(Vec3.atCenterOf(footPos)) > rangeSq) {
						continue;
					}
					// 床需要 2 格水平空间（footPos 与 headPos）
					for (Direction facing : Direction.Plane.HORIZONTAL) {
						BlockPos headPos = footPos.relative(facing);
						if (player.distanceToSqr(Vec3.atCenterOf(headPos)) > rangeSq) {
							continue;
						}
						if (canPlaceBedAt(level, footPos, headPos)) {
							BlockPos supportPos = footPos.below();
							BlockHitResult hit = new BlockHitResult(
								Vec3.atCenterOf(supportPos).add(0, 0.5, 0),
								Direction.UP,
								supportPos,
								false
							);
							candidates.add(new PlacementCandidate(headPos, footPos, facing, hit));
						}
					}
				}
			}
		}

		if (candidates.isEmpty()) {
			return Optional.empty();
		}

		// 优先选择离目标最近、离玩家距离适中的位置
		candidates.sort(Comparator.comparingDouble(c ->
			Vec3.atCenterOf(c.footPos()).distanceToSqr(target.position())
		));

		return Optional.of(candidates.getFirst());
	}

	public static boolean canPlaceBedAt(ClientLevel level, BlockPos footPos, BlockPos headPos) {
		if (!level.getWorldBorder().isWithinBounds(footPos) || !level.getWorldBorder().isWithinBounds(headPos)) {
			return false;
		}
		BlockState footState = level.getBlockState(footPos);
		BlockState headState = level.getBlockState(headPos);
		if (!footState.canBeReplaced() || !headState.canBeReplaced()) {
			return false;
		}
		// 底部必须有坚固支撑方块
		BlockPos footSupport = footPos.below();
		BlockPos headSupport = headPos.below();
		return level.getBlockState(footSupport).isFaceSturdy(level, footSupport, Direction.UP)
			&& level.getBlockState(headSupport).isFaceSturdy(level, headSupport, Direction.UP);
	}
}
