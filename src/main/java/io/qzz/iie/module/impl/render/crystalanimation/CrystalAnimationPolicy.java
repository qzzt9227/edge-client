package io.qzz.iie.module.impl.render.crystalanimation;

/**
 * 末地水晶动画微调的纯逻辑策略，用于计算角度与姿态。
 */
public final class CrystalAnimationPolicy {
	/**
	 * 原版每 tick 旋转角度基数（度）。
	 */
	public static final float BASE_DEGREES_PER_TICK = 3.0F;

	/**
	 * 固定高度偏移量（对应原版 tick 0 时 EndCrystalRenderer.getY(0.0F) * 8.0F 的模型空间位移）。
	 *
	 * <p>原版 outerGlass 定义位姿 y=24.0F，通过累加该偏移量后回落到基准高度 15.2F，
	 * 保证在去除上下浮动动画后水晶仍处于标准中心高度，不发生位置偏上或偏下。</p>
	 */
	public static final float FIXED_Y_OFFSET = -8.8F;

	/**
	 * 世界方块偏移换算为模型空间单位的系数（EndCrystalRenderer 预放大 2.0 倍，16 / 2 = 8）。
	 */
	public static final float MODEL_UNITS_PER_BLOCK = 8.0F;

	private CrystalAnimationPolicy() {
	}

	/**
	 * 获取去除浮动后的固定垂直位移量。
	 */
	public static float fixedYOffset() {
		return FIXED_Y_OFFSET;
	}

	/**
	 * 将以世界方块为单位的偏移量转换为模型空间坐标偏移。
	 */
	public static float toModelUnits(double blockOffset) {
		return (float) (blockOffset * MODEL_UNITS_PER_BLOCK);
	}

	/**
	 * 根据自定义 Y 轴方块偏移量计算最终应累加到 outerGlass.y 的模型位移。
	 *
	 * <p>因为在实体模型坐标系中 +Y 朝向地面，因此升高水晶（正 offset）对应减少模型 Y 坐标。</p>
	 */
	public static float calculateY(double blockOffsetY) {
		return FIXED_Y_OFFSET - toModelUnits(blockOffsetY);
	}

	/**
	 * 根据实体的生存 tick 时间与速度倍率计算自旋角度（度）。
	 */
	public static float calculateSpinAngle(float ageInTicks, double speedMultiplier) {
		return ageInTicks * BASE_DEGREES_PER_TICK * (float) speedMultiplier;
	}

	/**
	 * 判断是否应覆盖原版水晶动画逻辑。
	 */
	public static boolean shouldOverride(boolean moduleEnabled) {
		return moduleEnabled;
	}

	/**
	 * 判断当前是否处于静止模式。
	 */
	public static boolean isStatic(boolean moduleEnabled, CrystalAnimationMode mode) {
		return moduleEnabled && mode == CrystalAnimationMode.STATIC;
	}

	/**
	 * 判断当前是否处于旋转模式。
	 */
	public static boolean isSpin(boolean moduleEnabled, CrystalAnimationMode mode) {
		return moduleEnabled && mode == CrystalAnimationMode.SPIN;
	}
}
