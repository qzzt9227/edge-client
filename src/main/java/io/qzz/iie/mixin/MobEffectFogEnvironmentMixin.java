package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectFogEnvironment.class)
abstract class MobEffectFogEnvironmentMixin {

	@Shadow
	public abstract Holder<MobEffect> getMobEffect();

	@Inject(
		method = "isApplicable(Lnet/minecraft/world/level/material/FogType;Lnet/minecraft/world/entity/Entity;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void edgeClient$cancelMobEffectFog(
		FogType fogType,
		Entity entity,
		CallbackInfoReturnable<Boolean> cir
	) {
		Holder<MobEffect> effect = getMobEffect();
		if (effect != null) {
			if (effect.equals(MobEffects.BLINDNESS) && NoRenderHooks.shouldNoRenderBlindness()) {
				cir.setReturnValue(false);
				return;
			}
			if (effect.equals(MobEffects.DARKNESS) && NoRenderHooks.shouldNoRenderDarkness()) {
				cir.setReturnValue(false);
			}
		}
	}
}
