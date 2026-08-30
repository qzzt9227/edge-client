package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.player.autoignite.AutoIgniteVisualState;
import io.qzz.iie.module.impl.player.packetmine.PacketMineVisualState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 只改写本地玩家的渲染快照，让第三人称完整显示静默 yaw/pitch。
 *
 * <p>Fabric 26.2 没有修改 {@link AvatarRenderState} 的实体渲染事件；在状态
 * 提取结束后写入快照可以避免修改玩家实体角度，因此不会带动相机。</p>
 */
@Mixin(AvatarRenderer.class)
abstract class AvatarRendererMixin {
	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
		at = @At("TAIL")
	)
	private void edgeClient$applySilentRotationPreview(
		Avatar avatar,
		AvatarRenderState state,
		float partialTick,
		CallbackInfo ci
	) {
		if (avatar != Minecraft.getInstance().player) {
			return;
		}
		AutoIgniteVisualState.Snapshot ignitePreview = AutoIgniteVisualState.snapshot();
		if (ignitePreview.active()) {
			state.bodyRot = ignitePreview.yaw();
			state.yRot = 0.0F;
			state.xRot = ignitePreview.pitch();
			return;
		}
		PacketMineVisualState.Snapshot minePreview = PacketMineVisualState.snapshot();
		if (minePreview.rotationActive()) {
			state.bodyRot = minePreview.yaw();
			state.yRot = 0.0F;
			state.xRot = minePreview.pitch();
		}
	}
}
