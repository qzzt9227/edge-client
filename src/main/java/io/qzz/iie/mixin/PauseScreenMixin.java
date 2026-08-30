package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.player.antiquit.AntiQuitHooks;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截游戏菜单断开连接按钮点击，支持防误退确认。
 */
@Mixin(PauseScreen.class)
abstract class PauseScreenMixin extends Screen {
	protected PauseScreenMixin(Component title) {
		super(title);
	}

	@Inject(
		method = "lambda$createPauseMenu$11",
		at = @At("HEAD"),
		cancellable = true
	)
	private void edgeClient$onDisconnectButtonPressed(Button button, CallbackInfo ci) {
		if (AntiQuitHooks.shouldConfirmDisconnect()) {
			ci.cancel();
			AntiQuitHooks.promptDisconnect(this.minecraft, this, () -> {
				if (this.minecraft != null) {
					this.minecraft.getReportingContext().draftReportHandled(
						this.minecraft,
						this,
						() -> this.minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE),
						true
					);
				}
			});
		}
	}
}
