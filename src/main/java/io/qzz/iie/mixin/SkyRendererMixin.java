package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
abstract class SkyRendererMixin {

	@Inject(
		method = "extractRenderState(Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/SkyRenderState;)V",
		at = @At("TAIL")
	)
	private void edgeClient$modifySkyRenderState(
		ClientLevel clientLevel,
		float f,
		Camera camera,
		SkyRenderState state,
		CallbackInfo ci
	) {
		if (state == null || !NoRenderHooks.isEnabled()) {
			return;
		}

		if (NoRenderHooks.shouldNoRenderSky()) {
			state.skybox = DimensionType.Skybox.NONE;
			state.starBrightness = 0.0F;
			state.sunriseAndSunsetColor = 0;
		}

		if (NoRenderHooks.shouldNoRenderSkyColors()) {
			state.skyColor = 0xFF000000;
		}
	}
}
