package io.qzz.iie.module.impl.combat.autototem;

public final class AutoTotemPolicy {
	private AutoTotemPolicy() {
	}

	/**
	 * 预测摔落伤害（点数）：
	 * 正常摔落伤害 = max(0, ceil(fallDistance - 3.0 - jumpBoostLevel))
	 * 如果具有缓降（Slow Falling）效果，则摔落伤害为 0。
	 */
	public static double calculateFallDamage(double fallDistance, float jumpBoostLevel, boolean hasSlowFalling) {
		if (hasSlowFalling) {
			return 0.0;
		}
		double effectiveDistance = fallDistance - 3.0 - jumpBoostLevel;
		if (effectiveDistance <= 0.0) {
			return 0.0;
		}
		return Math.ceil(effectiveDistance);
	}

	/**
	 * 评估药水效果免伤情况：
	 * 抗性提升等级 >= 5 时完全免伤（100% 减免）。
	 */
	public static boolean isCompletelyInvulnerable(int resistanceLevel) {
		return resistanceLevel >= 5;
	}

	/**
	 * 综合评估是否需要装备图腾：
	 * @param currentHealth 玩家当前生命值
	 * @param absorption 伤害吸收额外生命值
	 * @param predictedFallDamage 预测摔落伤害
	 * @param healthThreshold 血量阈值
	 * @param fallDamageThreshold 摔落伤害阈值
	 * @param onlyOnLowHealth 是否仅在低血量/危险时生效
	 * @param isInvulnerable 是否因抗性等效果完全无敌
	 * @return 是否触发装备图腾
	 */
	public static boolean shouldEquipTotem(
		double currentHealth,
		double absorption,
		double predictedFallDamage,
		double healthThreshold,
		double fallDamageThreshold,
		boolean onlyOnLowHealth,
		boolean isInvulnerable
	) {
		if (isInvulnerable) {
			return false;
		}
		if (!onlyOnLowHealth) {
			return true;
		}

		double totalEffectiveHealth = currentHealth + absorption;
		if (totalEffectiveHealth <= healthThreshold) {
			return true;
		}
		if (predictedFallDamage >= fallDamageThreshold && predictedFallDamage > 0.0) {
			return true;
		}
		if (totalEffectiveHealth - predictedFallDamage <= healthThreshold) {
			return true;
		}
		return false;
	}
}
