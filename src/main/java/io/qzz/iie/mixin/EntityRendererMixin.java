package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin {

	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V",
		at = @At("TAIL")
	)
	private void edgeClient$onExtractEntityRenderState(
		Entity entity,
		EntityRenderState state,
		float partialTick,
		CallbackInfo ci
	) {
		if (NoRenderHooks.shouldNoRenderAnimationFire()) {
			state.displayFireAnimation = false;
		}

		if (entity instanceof Player && NoRenderHooks.shouldNoRenderPlayerNameTags()) {
			state.nameTag = null;
		} else if (entity instanceof ItemFrame && NoRenderHooks.shouldNoRenderItemFrameNameTags()) {
			state.nameTag = null;
		}
	}
}
