package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.input.invertmouse.InvertMouseHooks;
import io.qzz.iie.module.impl.input.specialflip.SpecialFlipHooks;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteVisualState;
import io.qzz.iie.module.impl.render.freelook.FreeLookHooks;
import io.qzz.iie.module.impl.render.zoom.ZoomHooks;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 反转鼠标视角（水平 yaw / 垂直 pitch）与缩放灵敏度的注入点。
 *
 * <p>目标方法：{@code MouseHandler.turnPlayer(double)} 中对
 * {@code LocalPlayer.turn(DD)V} 的调用。Fabric 没有提供视角旋转回调，
 * 因此只能注入；<a href="https://github.com/neoforged/NeoForge/tree/26.1.x/patches">
 * 26.1 官方源码</a>确认该方法结构在 26.x 中保持稳定，调用点语义不变。</p>
 */
@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
	@Redirect(
		method = "turnPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
		)
	)
	private void edgeClient$invertMouseTurn(
		LocalPlayer player,
		double yRot,
		double xRot
	) {
		if (AutoIgniteVisualState.shouldSuppressMouseTurn()) {
			return;
		}
		double sensitivityMultiplier = ZoomHooks.getSensitivityMultiplier();
		yRot *= sensitivityMultiplier;
		xRot *= sensitivityMultiplier;
		if (FreeLookHooks.shouldInterceptMouseTurn()) {
			FreeLookHooks.turn(yRot, xRot);
			return;
		}
		if (SpecialFlipHooks.shouldApply()) {
			SpecialFlipHooks.turn(player, yRot, xRot);
			return;
		}
		InvertMouseHooks.turn(player, yRot, xRot);
	}

	@org.spongepowered.asm.mixin.injection.Inject(
		method = "onButton",
		at = @At("HEAD")
	)
	private void edgeClient$onButton(
		long window,
		net.minecraft.client.input.MouseButtonInfo buttonInfo,
		int action,
		org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci
	) {
		if (action == 1) {
			if (buttonInfo.button() == 0) {
				io.qzz.iie.ui.hud.CpsTracker.recordLeftClick();
			} else if (buttonInfo.button() == 1) {
				io.qzz.iie.ui.hud.CpsTracker.recordRightClick();
			}
		}
	}
}
