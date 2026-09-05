package io.qzz.iie.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import io.qzz.iie.module.impl.render.norender.SpriteNameHolder;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpriteContents.class)
abstract class SpriteContentsMixin {

	@Shadow
	@Final
	private Identifier name;

	@Inject(
		method = "createAnimationState(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;I)Lnet/minecraft/client/renderer/texture/SpriteContents$AnimationState;",
		at = @At("RETURN")
	)
	private void edgeClient$attachSpriteName(
		GpuBufferSlice gpuBufferSlice,
		int i,
		CallbackInfoReturnable<SpriteContents.AnimationState> cir
	) {
		SpriteContents.AnimationState state = cir.getReturnValue();
		if (state instanceof SpriteNameHolder holder && name != null) {
			holder.edgeClient$setSpriteName(name.toString());
		}
	}
}
