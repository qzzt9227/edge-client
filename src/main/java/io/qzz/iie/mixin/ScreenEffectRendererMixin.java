package io.qzz.iie.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
abstract class ScreenEffectRendererMixin {

	@Inject(
		method = "submitWater(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void edgeClient$cancelScreenWater(
		Minecraft minecraft,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CallbackInfo ci
	) {
		if (NoRenderHooks.shouldNoRenderUnderwaterLavaOverlay()) {
			ci.cancel();
		}
	}

	@Inject(
		method = "submitBlockSprite(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void edgeClient$cancelScreenBlockSprite(
		TextureAtlasSprite textureAtlasSprite,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int i,
		CallbackInfo ci
	) {
		if (NoRenderHooks.shouldNoRenderUnderwaterLavaOverlay()) {
			ci.cancel();
		}
	}

	@Inject(
		method = "submitFire(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void edgeClient$cancelScreenFire(
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		TextureAtlasSprite textureAtlasSprite,
		CallbackInfo ci
	) {
		if (NoRenderHooks.shouldNoRenderAnimationFire()) {
			ci.cancel();
		}
	}
}
