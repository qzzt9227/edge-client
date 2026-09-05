package io.qzz.iie.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconRenderer.class)
abstract class BeaconRendererMixin {

	@Inject(
		method = "submitBeaconBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/resources/Identifier;FFIIIFF)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void edgeClient$cancelBeaconBeam(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		Identifier identifier,
		float f,
		float g,
		int i,
		int j,
		int k,
		float h,
		float l,
		CallbackInfo ci
	) {
		if (NoRenderHooks.shouldNoRenderBeaconBeams()) {
			ci.cancel();
		}
	}
}
