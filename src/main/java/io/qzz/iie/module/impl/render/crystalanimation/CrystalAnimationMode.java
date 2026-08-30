package io.qzz.iie.module.impl.render.crystalanimation;

/**
 * 末地水晶动画微调模式枚举。
 */
public enum CrystalAnimationMode {
	/**
	 * 静止模式：去除旋转与浮动动画，水晶方正直立、完全静止。
	 */
	STATIC,

	/**
	 * 旋转模式：去除浮动动画，水晶方正直立绕 Y 轴水平自旋，可调节转速。
	 */
	SPIN
}
