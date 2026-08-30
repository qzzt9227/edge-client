package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.freelook.FreeLookHooks;
import io.qzz.iie.module.impl.render.zoom.ZoomHooks;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 注入相机 FOV 计算与自由视角（FreeLook）旋转。
 */
@Mixin(Camera.class)
abstract class CameraMixin {
	@Inject(
		method = "calculateFov(F)F",
		at = @At("RETURN"),
		cancellable = true
	)
	private void edgeClient$modifyZoomFov(float partialTicks, CallbackInfoReturnable<Float> cir) {
		float zoomMultiplier = ZoomHooks.getZoomMultiplier(partialTicks);
		if (zoomMultiplier > 1.0f) {
			cir.setReturnValue(cir.getReturnValueF() / zoomMultiplier);
		}
	}

	@Redirect(
		method = "alignWithEntity",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"
		)
	)
	private float edgeClient$freeLookYRot(Entity entity, float partialTicks) {
		if (FreeLookHooks.isActive()) {
			return FreeLookHooks.getYaw(partialTicks);
		}
		return entity.getViewYRot(partialTicks);
	}

	@Redirect(
		method = "alignWithEntity",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"
		)
	)
	private float edgeClient$freeLookXRot(Entity entity, float partialTicks) {
		if (FreeLookHooks.isActive()) {
			return FreeLookHooks.getPitch(partialTicks);
		}
		return entity.getViewXRot(partialTicks);
	}
}
