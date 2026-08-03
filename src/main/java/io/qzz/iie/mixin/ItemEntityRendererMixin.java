package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.itemrendermode.ItemRenderMode;
import io.qzz.iie.module.impl.render.itemrendermode.ItemRenderModeHooks;
import io.qzz.iie.module.impl.render.itemrendermode.ItemRenderModeRenderState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 改写掉落物实体的旋转行为。
 *
 * <p>原版 {@code ItemEntityRenderer.submit} 只做 {@code Axis.YP.rotation(spin)}
 * 的 Y 轴自旋。2D 模式将其替换为面向相机角度的垂直 billboard（与火焰精灵
 * 朝向相机的做法一致）；冻结模式替换为状态提取阶段记录的角度。Fabric 没有
 * 掉落物渲染旋转的合适钩子，因此必须注入 {@code ItemEntityRenderer}。</p>
 */
@Mixin(ItemEntityRenderer.class)
abstract class ItemEntityRendererMixin {
	@Inject(
		method = "createRenderState()Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void edgeClient$createCustomRenderState(
		CallbackInfoReturnable<ItemEntityRenderState> cir
	) {
		cir.setReturnValue(new ItemRenderModeRenderState());
	}

	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V",
		at = @At("TAIL")
	)
	private void edgeClient$resolveFrozenSpin(
		ItemEntity entity,
		ItemEntityRenderState state,
		float partialTicks,
		CallbackInfo ci
	) {
		float spin = ItemEntity.getSpin(state.ageInTicks, state.bobOffset);
		ItemRenderModeHooks.applyFrozenSpin(entity.getUUID(), spin, state);
	}

	@Redirect(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotation(F)Lorg/joml/Quaternionf;")
	)
	private Quaternionf edgeClient$redirectItemSpin(
		Axis axis,
		float spin,
		ItemEntityRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CameraRenderState camera
	) {
		return switch (ItemRenderModeHooks.renderMode()) {
			case BILLBOARD -> Axis.YP.rotationDegrees(180.0F - camera.yRot);
			case FREEZE_ROTATION -> state instanceof ItemRenderModeRenderState renderState
				&& renderState.frozenSpin != null
				? Axis.YP.rotation(renderState.frozenSpin)
				: Axis.YP.rotation(spin);
			case VANILLA -> Axis.YP.rotation(spin);
		};
	}
}
