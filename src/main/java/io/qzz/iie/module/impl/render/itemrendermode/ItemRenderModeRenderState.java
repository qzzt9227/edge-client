package io.qzz.iie.module.impl.render.itemrendermode;

import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;

/**
 * 携带冻结旋转角度的 {@link ItemEntityRenderState} 子类。
 *
 * <p>26.2 的渲染状态每帧新建，绘制阶段（{@code ItemEntityRenderer.submit}）
 * 只能拿到状态而不能拿到实体，因此冻结角度必须在状态提取阶段写入此字段，
 * 供提交阶段直接读取。{@code null} 表示当前帧无需使用冻结角度。</p>
 */
public final class ItemRenderModeRenderState extends ItemEntityRenderState {
	public Float frozenSpin;
}
