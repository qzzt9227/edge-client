package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import io.qzz.iie.module.impl.render.norender.SpriteNameHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.renderer.texture.SpriteContents$AnimationState")
abstract class SpriteContentsAnimationStateMixin implements SpriteNameHolder {

	@Unique
	private String edgeClient$spriteName;

	@Override
	public String edgeClient$getSpriteName() {
		return edgeClient$spriteName;
	}

	@Override
	public void edgeClient$setSpriteName(String name) {
		this.edgeClient$spriteName = name;
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void edgeClient$cancelAnimatedSpriteTick(CallbackInfo ci) {
		if (NoRenderHooks.shouldFreezeSpriteAnimation(edgeClient$spriteName)) {
			ci.cancel();
		}
	}
}
