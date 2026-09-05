package io.qzz.iie.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandRenderer.class)
abstract class ArmorStandRendererMixin {

	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void edgeClient$cancelArmorStandRender(
		ArmorStandRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CameraRenderState cameraRenderState,
		CallbackInfo ci
	) {
		if (NoRenderHooks.shouldNoRenderArmorStands()) {
			ci.cancel();
		}
	}
}
