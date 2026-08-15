package io.qzz.iie.module.impl.combat.autocrystal;

/**
 * 自动水晶爆炸伤害与自伤计算器（纯数学与逻辑模型，不依赖 Minecraft 渲染）。
 */
public final class AutoCrystalDamageCalculator {
	public static final double CRYSTAL_EXPLOSION_POWER = 6.0;
	public static final double DOUBLE_POWER = CRYSTAL_EXPLOSION_POWER * 2.0;

	private AutoCrystalDamageCalculator() {
	}

	/**
	 * 计算水晶爆炸对实体的基础理论伤害。
	 *
	 * @param crystalX 水晶 X 坐标
	 * @param crystalY 水晶 Y 坐标
	 * @param crystalZ 水晶 Z 坐标
	 * @param targetX  目标 X 坐标
	 * @param targetY  目标 Y 坐标
	 * @param targetZ  目标 Z 坐标
	 * @param exposure 暴露度 (0.0 ~ 1.0, 1.0 代表无阻挡)
	 * @return 理论计算伤害值（未扣减护甲）
	 */
	public static double calculateRawDamage(
		double crystalX,
		double crystalY,
		double crystalZ,
		double targetX,
		double targetY,
		double targetZ,
		double exposure
	) {
		double dx = targetX - crystalX;
		double dy = targetY - crystalY;
		double dz = targetZ - crystalZ;
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

		if (distance >= DOUBLE_POWER) {
			return 0.0;
		}

		double normalizedDistance = distance / DOUBLE_POWER;
		double clampedExposure = Math.clamp(exposure, 0.0, 1.0);
		double impact = (1.0 - normalizedDistance) * clampedExposure;
		return ((impact * impact + impact) / 2.0) * 7.0 * DOUBLE_POWER + 1.0;
	}

	/**
	 * 结合护甲与抗性效果计算实际受到伤害。
	 *
	 * @param rawDamage        基础爆炸伤害
	 * @param armor            护甲点数 (0 ~ 30)
	 * @param armorToughness   盔甲韧性 (0 ~ 20)
	 * @param resistanceLevel  抗性提升药水等级 (0 ~ 5)
	 * @param blastProtLevel   爆炸保护附魔等级 (0 ~ 20)
	 * @return 最终实际伤害
	 */
	public static double calculateActualDamage(
		double rawDamage,
		double armor,
		double armorToughness,
		int resistanceLevel,
		int blastProtLevel
	) {
		if (rawDamage <= 0.0) {
			return 0.0;
		}

		// 1. 护甲与韧性削减
		double clampedArmor = Math.clamp(armor, 0.0, 30.0);
		double clampedToughness = Math.max(0.0, armorToughness);
		double f = 2.0 + clampedToughness / 4.0;
		double effectiveArmor = Math.clamp(clampedArmor - rawDamage / f, clampedArmor * 0.2, 20.0);
		double damageAfterArmor = rawDamage * (1.0 - effectiveArmor / 25.0);

		// 2. 抗性提升削减 (每级 20%)
		if (resistanceLevel > 0) {
			double resistanceReduction = Math.min(1.0, resistanceLevel * 0.2);
			damageAfterArmor *= (1.0 - resistanceReduction);
		}

		// 3. 爆炸保护附魔削减 (每级 EPF，上限 80%)
		if (blastProtLevel > 0) {
			int clampedEpf = Math.clamp(blastProtLevel * 2, 0, 20);
			damageAfterArmor *= (1.0 - clampedEpf / 25.0);
		}

		return Math.max(0.0, damageAfterArmor);
	}
}
