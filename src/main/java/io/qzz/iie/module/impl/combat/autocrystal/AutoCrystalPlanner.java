package io.qzz.iie.module.impl.combat.autocrystal;

import io.qzz.iie.module.impl.combat.autocrystal.AutoCrystalTypes.TargetPriority;

import java.util.Objects;

/**
 * 自动水晶决策规划器（纯逻辑模型）。
 */
public final class AutoCrystalPlanner {
	private AutoCrystalPlanner() {
	}

	/**
	 * 放置水晶候选点记录。
	 */
	public record PlacementCandidate(
		double blockX,
		double blockY,
		double blockZ,
		double targetDamage,
		double selfDamage,
		boolean isFacePlace
	) {
	}

	/**
	 * 击碎水晶候选记录。
	 */
	public record BreakCandidate(
		int entityId,
		double crystalX,
		double crystalY,
		double crystalZ,
		double targetDamage,
		double selfDamage
	) {
	}

	/**
	 * 评估放置候选点是否满足安全与伤害策略。
	 */
	public static boolean isValidPlacement(
		double playerDist,
		double placeRange,
		double wallRange,
		double minDistance,
		boolean hasLineOfSight,
		double targetDamage,
		double selfDamage,
		double minDamage,
		double maxSelfDamage,
		boolean isFacePlaceTriggered
	) {
		// 1. 距离检查
		if (playerDist > placeRange || playerDist < minDistance) {
			return false;
		}
		if (!hasLineOfSight && playerDist > wallRange) {
			return false;
		}

		// 2. 自伤防护检查
		if (selfDamage > maxSelfDamage) {
			return false;
		}

		// 3. 伤害门槛（面朝放置激活时降低伤害门槛）
		if (isFacePlaceTriggered) {
			return targetDamage >= 1.5;
		}
		return targetDamage >= minDamage;
	}

	/**
	 * 比较两个放置候选点的优劣。
	 */
	public static int comparePlacement(
		PlacementCandidate a,
		PlacementCandidate b,
		TargetPriority priority
	) {
		Objects.requireNonNull(priority, "priority");
		if (a == null && b == null) return 0;
		if (a == null) return -1;
		if (b == null) return 1;

		// 优先比较伤害/自伤比或绝对伤害
		switch (priority) {
			case HIGHEST_DAMAGE -> {
				int cmp = Double.compare(a.targetDamage(), b.targetDamage());
				if (cmp != 0) return cmp;
				return Double.compare(b.selfDamage(), a.selfDamage()); // 自伤越小越优
			}
			case LOWEST_HEALTH -> {
				int cmp = Boolean.compare(a.isFacePlace(), b.isFacePlace());
				if (cmp != 0) return cmp;
				return Double.compare(a.targetDamage(), b.targetDamage());
			}
			case NEAREST -> {
				return Double.compare(a.targetDamage(), b.targetDamage());
			}
		}
		return 0;
	}
}
