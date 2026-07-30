package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.betterhealth.BetterHealthBarHooks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.gui.Hud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 只修改血条方法中最终得到的最大生命值，不覆盖原版 HUD 绘制实现。
 */
@Mixin(Hud.class)
abstract class HudMixin {
	@ModifyExpressionValue(
		method = "extractPlayerHealth",
		at = @At(
			value = "INVOKE",
			target = "Ljava/lang/Math;max(FF)F",
			ordinal = 0
		),
		require = 1
	)
	private float edgeClient$limitExtraHeartRows(float originalMaximumHealth) {
		return BetterHealthBarHooks.clampMaximumHealth(originalMaximumHealth);
	}
}
