package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
abstract class ParticleEngineMixin {

	@Inject(
		method = "add(Lnet/minecraft/client/particle/Particle;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void edgeClient$cancelParticleAdd(Particle particle, CallbackInfo ci) {
		if (NoRenderHooks.shouldNoRenderParticles()) {
			ci.cancel();
		}
	}

	@Inject(
		method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void edgeClient$cancelParticleCreate(
		ParticleOptions options,
		double x,
		double y,
		double z,
		double vx,
		double vy,
		double vz,
		CallbackInfoReturnable<Particle> cir
	) {
		if (NoRenderHooks.shouldNoRenderParticles()) {
			cir.setReturnValue(null);
		}
	}
}
