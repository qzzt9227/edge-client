package io.qzz.iie.module.impl.player.airplace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * 空中放置计算策略，负责视线投射、放置朝向与自身碰撞判定。
 */
public final class AirPlacePolicy {
	private AirPlacePolicy() {
	}

	public record TargetPlacement(BlockPos blockPos, Vec3 hitVec, Direction direction) {
		public TargetPlacement {
			Objects.requireNonNull(blockPos, "blockPos");
			Objects.requireNonNull(hitVec, "hitVec");
			Objects.requireNonNull(direction, "direction");
		}
	}

	/**
	 * 根据视线起点、朝向向量与距离计算目标放置点与朝向。
	 */
	public static TargetPlacement calculatePlacement(
		Vec3 eyePos,
		Vec3 lookVec,
		double range,
		AirPlaceDirection mode,
		Direction playerHorizontalFacing
	) {
		Vec3 hitVec = eyePos.add(lookVec.scale(range));
		BlockPos blockPos = BlockPos.containing(hitVec);
		Direction direction = resolveDirection(mode, lookVec, playerHorizontalFacing);
		return new TargetPlacement(blockPos, hitVec, direction);
	}

	/**
	 * 解析放置接触面朝向。
	 */
	public static Direction resolveDirection(
		AirPlaceDirection mode,
		Vec3 lookVec,
		Direction playerHorizontalFacing
	) {
		return switch (mode) {
			case UP -> Direction.UP;
			case DOWN -> Direction.DOWN;
			case FACING -> playerHorizontalFacing != null ? playerHorizontalFacing.getOpposite() : Direction.UP;
			case AUTO -> {
				if (lookVec.y < -0.7) {
					yield Direction.UP;
				}
				if (lookVec.y > 0.7) {
					yield Direction.DOWN;
				}
				if (Math.abs(lookVec.x) > Math.abs(lookVec.z)) {
					yield lookVec.x > 0 ? Direction.WEST : Direction.EAST;
				} else {
					yield lookVec.z > 0 ? Direction.NORTH : Direction.SOUTH;
				}
			}
		};
	}

	/**
	 * 检查目标方块坐标是否与玩家实体碰撞箱冲突。
	 */
	public static boolean isPlacementAllowed(AABB playerBox, BlockPos targetPos) {
		if (playerBox == null || targetPos == null) {
			return true;
		}
		AABB blockBox = new AABB(
			targetPos.getX(), targetPos.getY(), targetPos.getZ(),
			targetPos.getX() + 1.0, targetPos.getY() + 1.0, targetPos.getZ() + 1.0
		);
		return !playerBox.intersects(blockBox);
	}
}
