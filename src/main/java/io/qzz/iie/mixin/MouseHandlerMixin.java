package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.player.invertmouse.InvertMouseHooks;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 反转鼠标视角（水平 yaw / 垂直 pitch）的注入点。
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
		InvertMouseHooks.turn(player, yRot, xRot);
	}
}
