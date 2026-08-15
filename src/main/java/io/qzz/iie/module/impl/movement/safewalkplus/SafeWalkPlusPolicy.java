package io.qzz.iie.module.impl.movement.safewalkplus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiPredicate;

/**
 * 安全行走+ 的纯几何与决策逻辑，负责计算玩家脚底碰撞箱在支撑方块上的覆盖率。
 */
public final class SafeWalkPlusPolicy {
	private SafeWalkPlusPolicy() {
	}

	/**
	 * 计算玩家水平投影碰撞箱在固体支撑方块上的覆盖率 [0.0, 1.0]。
	 *
	 * @param minX 碰撞箱最小 X
	 * @param maxX 碰撞箱最大 X
	 * @param minZ 碰撞箱最小 Z
	 * @param maxZ 碰撞箱最大 Z
	 * @param isSolidBlock 判定 (x, z) 方块是否为有效固体支撑
	 * @return 覆盖比例，范围 [0.0, 1.0]
	 */
	public static double calculateSupportCoverage(
		double minX,
		double maxX,
		double minZ,
		double maxZ,
		BiPredicate<Integer, Integer> isSolidBlock
	) {
		double totalArea = (maxX - minX) * (maxZ - minZ);
		if (totalArea <= 0.0) {
			return 0.0;
		}

		int startX = (int) Math.floor(minX);
		int endX = (int) Math.floor(maxX);
		int startZ = (int) Math.floor(minZ);
		int endZ = (int) Math.floor(maxZ);

		double supportedArea = 0.0;

		for (int bx = startX; bx <= endX; bx++) {
			for (int bz = startZ; bz <= endZ; bz++) {
				if (isSolidBlock.test(bx, bz)) {
					double overlapX = Math.max(0.0, Math.min(maxX, (double) bx + 1.0) - Math.max(minX, (double) bx));
					double overlapZ = Math.max(0.0, Math.min(maxZ, (double) bz + 1.0) - Math.max(minZ, (double) bz));
					supportedArea += overlapX * overlapZ;
				}
			}
		}

		return Math.clamp(supportedArea / totalArea, 0.0, 1.0);
	}

	/**
	 * 判定当前是否应该强制潜行。
	 *
	 * @param coverage 当前脚底碰撞箱支撑覆盖率 [0.0, 1.0]
	 * @param thresholdPercent 设定的最低支撑百分比阈值 [1.0, 100.0]
	 * @param onGround 玩家是否在地面上
	 * @return 如果覆盖率低于设定阈值且在地面上，返回 true（强制潜行）
	 */
	public static boolean shouldForceSneak(
		double coverage,
		double thresholdPercent,
		boolean onGround
	) {
		if (!onGround) {
			return false;
		}
		double requiredCoverage = Math.clamp(thresholdPercent / 100.0, 0.01, 1.0);
		return coverage < requiredCoverage;
	}

	/**
	 * 判断世界指定坐标是否有实体碰撞形状（即有效支撑方块）。
	 */
	public static boolean isSolidAt(Level level, int x, int y, int z) {
		if (level == null) {
			return false;
		}
		BlockPos pos = new BlockPos(x, y, z);
		BlockState state = level.getBlockState(pos);
		if (state.isAir()) {
			return false;
		}
		VoxelShape shape = state.getCollisionShape(level, pos);
		return !shape.isEmpty();
	}
}
