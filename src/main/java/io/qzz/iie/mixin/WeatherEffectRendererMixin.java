package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
abstract class WeatherEffectRendererMixin {

	@Inject(
		method = "render(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/state/level/WeatherRenderState;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void edgeClient$cancelWeatherRender(
		Vec3 vec3,
		WeatherRenderState weatherRenderState,
		CallbackInfo ci
	) {
		if (NoRenderHooks.shouldNoRenderWeather()) {
			ci.cancel();
		}
	}
}
