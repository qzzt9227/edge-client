package io.qzz.iie.module.impl.render.explosionwarning;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/** Submits countdown text during the 26.2 world extraction phase. */
public final class ExplosionWarningWorldRenderer {
	private static ExplosionWarningModule module;
	private static boolean installed;

	private ExplosionWarningWorldRenderer() {
	}

	public static void install(ExplosionWarningModule installedModule) {
		module = Objects.requireNonNull(installedModule, "installedModule");
		if (!installed) {
			installed = true;
			LevelRenderEvents.COLLECT_SUBMITS.register(ExplosionWarningWorldRenderer::collect);
		}
	}

	private static void collect(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
		ExplosionWarningModule current = module;
		if (current == null || !current.isEnabled() || current.renderStates().isEmpty()) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		CameraRenderState cameraState = context.levelState().cameraRenderState;
		Vec3 camera = cameraState.pos;
		float partialTick = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		PoseStack pose = context.poseStack();
		SubmitNodeCollector collector = context.submitNodeCollector();
		for (ExplosionWarningRenderState state : current.renderStates()) {
			long remainingMillis = state.kind() == ExplosionTargetKind.TNT
				? ExplosionCountdown.tntRemainingMillis(state.tntFuse(), partialTick)
				: ExplosionCountdown.creeperRemainingMillis(state.creeperSwelling());
			Component text = Component.literal(ExplosionCountdown.formatMillis(remainingMillis))
				.withStyle(style -> style.withColor(current.messageColor()));
			ExplosionWarningPlacement.Position position = ExplosionWarningPlacement.resolve(
				state.kind(),
				state.x(),
				state.targetY(),
				state.z(),
				state.halfWidth(),
				state.halfDepth(),
				camera.x,
				camera.z,
				current.countdownOffsetX().value(),
				current.countdownOffsetY().value(),
				current.countdownOffsetZ().value()
			);
			pose.pushPose();
			try {
				pose.translate(-camera.x, -camera.y, -camera.z);
				pose.translate(position.x(), position.y(), position.z());
				pose.mulPose(cameraState.orientation);
				pose.scale(0.025F, -0.025F, 0.025F);
				int width = -client.font.width(text) / 2;
				collector.submitText(
					pose,
					width,
					0.0F,
					text.getVisualOrderText(),
					true,
					Font.DisplayMode.NORMAL,
					0x00F000F0,
					current.messageColor(),
					0x80000000,
					0
				);
			} finally {
				pose.popPose();
			}
		}
	}
}
