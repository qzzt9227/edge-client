package io.qzz.iie.module.impl.render.itemrendermode;

/**
 * 掉落物的渲染行为。
 *
 * <p>{@link #VANILLA} 表示模块未启用，仅由 {@link ItemRenderModeHooks}
 * 作为“不修改渲染”的哨兵使用，不暴露为设置选项。</p>
 */
public enum ItemRenderMode {
	VANILLA,
	BILLBOARD,
	FREEZE_ROTATION
}
