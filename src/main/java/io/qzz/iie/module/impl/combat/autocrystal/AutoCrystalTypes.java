package io.qzz.iie.module.impl.combat.autocrystal;

/**
 * 自动水晶模块的核心枚举与类型定义。
 */
public final class AutoCrystalTypes {
	private AutoCrystalTypes() {
	}

	/**
	 * 目标优先级模式。
	 */
	public enum TargetPriority {
		HIGHEST_DAMAGE,
		LOWEST_HEALTH,
		NEAREST
	}

	/**
	 * 目标筛选类型。
	 */
	public enum TargetType {
		PLAYERS,
		MONSTERS,
		ALL
	}

	/**
	 * 视角旋转与防作弊模式。
	 */
	public enum RotationMode {
		OFF,
		VANILLA,
		PACKET,
		SMOOTH,
		GRIMAC
	}

	/**
	 * 水晶与武器物品切换模式。
	 */
	public enum SwitchMode {
		OFF,
		NORMAL,
		SILENT,
		GHOST_HAND
	}
}
