package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.crystalanimation.CrystalAnimationHooks;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截末地水晶模型的动画计算，应用自定义静止或旋转姿态。
 */
@Mixin(EndCrystalModel.class)
abstract class EndCrystalModelMixin {
	@Inject(
		method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void edgeClient$setupCrystalAnim(
		EndCrystalRenderState state,
		CallbackInfo ci
	) {
		if (CrystalAnimationHooks.isEnabled()) {
			CrystalAnimationHooks.applyCustomAnim((EndCrystalModel) (Object) this, state);
			ci.cancel();
		}
	}
}
