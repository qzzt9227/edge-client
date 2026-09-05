package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
abstract class ParticleEngineMixin {

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
		if (options != null) {
			Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType());
			if (id != null && NoRenderHooks.shouldNoRenderParticle(id.toString())) {
				cir.setReturnValue(null);
			}
		}
	}

	@Inject(
		method = "createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;I)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void edgeClient$cancelTrackingEmitter(
		Entity entity,
		ParticleOptions options,
		int lifetime,
		CallbackInfo ci
	) {
		if (options != null) {
			Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType());
			if (id != null && NoRenderHooks.shouldNoRenderParticle(id.toString())) {
				ci.cancel();
			}
		}
	}
}
