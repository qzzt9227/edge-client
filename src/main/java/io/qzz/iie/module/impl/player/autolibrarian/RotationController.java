package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 按平滑曲线把玩家视角转向交互目标。
 */
final class RotationController {
	private Rotation rotation;

	void begin(
		LocalPlayer player,
		Vec3 target,
		int duration,
		AutomationState nextState,
		int nextTimer
	) {
		Vec3 delta = target.subtract(player.getEyePosition());
		double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		float targetYaw = (float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
		float targetPitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontal));
		rotation = new Rotation(
			player.getYRot(),
			player.getXRot(),
			targetYaw,
			Mth.clamp(targetPitch, -90.0F, 90.0F),
			Math.max(1, duration),
			nextState,
			nextTimer
		);
	}

	Completion tick(LocalPlayer player) {
		if (rotation == null) {
			throw new IllegalStateException("没有正在执行的转向");
		}
		rotation.elapsed++;
		float progress = Mth.clamp(
			(float) rotation.elapsed / rotation.duration,
			0.0F,
			1.0F
		);
		float smooth = progress * progress * (3.0F - 2.0F * progress);
		float yawDelta = Mth.wrapDegrees(rotation.targetYaw - rotation.startYaw);
		player.setYRot(rotation.startYaw + yawDelta * smooth);
		player.setXRot(Mth.lerp(smooth, rotation.startPitch, rotation.targetPitch));
		if (rotation.elapsed < rotation.duration) {
			return null;
		}
		Completion completion =
			new Completion(rotation.nextState, rotation.nextTimer);
		rotation = null;
		return completion;
	}

	void clear() {
		rotation = null;
	}

	record Completion(AutomationState nextState, int nextTimer) {
	}

	private static final class Rotation {
		private final float startYaw;
		private final float startPitch;
		private final float targetYaw;
		private final float targetPitch;
		private final int duration;
		private final AutomationState nextState;
		private final int nextTimer;
		private int elapsed;

		private Rotation(
			float startYaw,
			float startPitch,
			float targetYaw,
			float targetPitch,
			int duration,
			AutomationState nextState,
			int nextTimer
		) {
			this.startYaw = startYaw;
			this.startPitch = startPitch;
			this.targetYaw = targetYaw;
			this.targetPitch = targetPitch;
			this.duration = duration;
			this.nextState = nextState;
			this.nextTimer = nextTimer;
		}
	}
}
