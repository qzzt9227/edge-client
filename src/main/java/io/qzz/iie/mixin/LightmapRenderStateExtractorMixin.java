package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
abstract class LightmapRenderStateExtractorMixin {

	@Inject(
		method = "extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V",
		at = @At("TAIL")
	)
	private void edgeClient$cancelDarknessLight(
		LightmapRenderState state,
		float partialTick,
		CallbackInfo ci
	) {
		if (NoRenderHooks.shouldNoRenderDarkness()) {
			state.darknessEffectScale = 0.0F;
		}
	}
}
