package io.qzz.iie.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.MapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.class)
abstract class MapRendererMixin {

	@Inject(
		method = "render(Lnet/minecraft/client/renderer/state/MapRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ZI)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void edgeClient$cancelMapRender(
		MapRenderState mapRenderState,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		boolean hasFrame,
		int packedLight,
		CallbackInfo ci
	) {
		if (NoRenderHooks.shouldNoRenderMaps()) {
			ci.cancel();
		}
	}
}
