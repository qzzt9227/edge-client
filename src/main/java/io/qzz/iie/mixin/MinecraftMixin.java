package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.player.antiquit.AntiQuitHooks;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 拦截游戏主循环中的窗口关闭检测，支持防误关确认。
 */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Redirect(
		method = "runTick",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/platform/Window;shouldClose()Z"
		)
	)
	private boolean edgeClient$onWindowShouldClose(Window window) {
		boolean shouldClose = window.shouldClose();
		if (!shouldClose) {
			return false;
		}
		return AntiQuitHooks.handleWindowShouldClose((Minecraft) (Object) this, window);
	}
}
