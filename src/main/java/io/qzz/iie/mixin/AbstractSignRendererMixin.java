package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignRenderer.class)
abstract class AbstractSignRendererMixin {

	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/level/block/entity/SignBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/SignRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
		at = @At("TAIL")
	)
	private void edgeClient$cancelSignText(
		SignBlockEntity signBlockEntity,
		SignRenderState state,
		float partialTick,
		Vec3 cameraPos,
		ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
		CallbackInfo ci
	) {
		if (NoRenderHooks.shouldNoRenderSignText()) {
			state.frontText = null;
			state.backText = null;
		}
	}
}
