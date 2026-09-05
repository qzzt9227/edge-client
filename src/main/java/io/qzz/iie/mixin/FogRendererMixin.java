package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
abstract class FogRendererMixin {

	@Inject(
		method = "setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lnet/minecraft/client/renderer/fog/FogData;",
		at = @At("TAIL")
	)
	private void edgeClient$modifyFogData(
		Camera camera,
		int renderDistance,
		DeltaTracker deltaTracker,
		float f,
		ClientLevel clientLevel,
		CallbackInfoReturnable<FogData> cir
	) {
		FogData data = cir.getReturnValue();
		if (data == null || !NoRenderHooks.isEnabled()) {
			return;
		}

		if (NoRenderHooks.shouldNoRenderUnderwaterLavaOverlay() && camera != null) {
			FogType fluid = camera.getFluidInCamera();
			if (fluid == FogType.WATER || fluid == FogType.LAVA) {
				data.environmentalStart = -8.0F;
				data.environmentalEnd = 100000.0F;
				data.renderDistanceStart = 100000.0F;
				data.renderDistanceEnd = 100000.0F;
				return;
			}
		}

		if (clientLevel != null) {
			if (clientLevel.dimension().equals(Level.OVERWORLD) && NoRenderHooks.shouldNoRenderOverworldFog()) {
				data.environmentalEnd = 100000.0F;
				data.renderDistanceEnd = 100000.0F;
			} else if (clientLevel.dimension().equals(Level.NETHER) && NoRenderHooks.shouldNoRenderNetherFog()) {
				data.environmentalEnd = 100000.0F;
				data.renderDistanceEnd = 100000.0F;
			} else if (clientLevel.dimension().equals(Level.END) && NoRenderHooks.shouldNoRenderEndFog()) {
				data.environmentalEnd = 100000.0F;
				data.renderDistanceEnd = 100000.0F;
			}
		}

		double multiplier = NoRenderHooks.getGlobalFogDistance();
		if (multiplier > 1.0) {
			data.environmentalEnd = (float) (data.environmentalEnd * multiplier);
			data.renderDistanceEnd = (float) (data.renderDistanceEnd * multiplier);
		}
	}
}
