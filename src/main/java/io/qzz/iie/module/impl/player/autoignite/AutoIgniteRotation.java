package io.qzz.iie.module.impl.player.autoignite;

public final class AutoIgniteRotation {
	private AutoIgniteRotation() {
	}

	public static float interpolateAngle(float start, float target, double progress) {
		return (float) (start + wrapDegrees(target - start) * eased(progress));
	}

	public static float interpolateLinear(float start, float target, double progress) {
		return (float) (start + (target - start) * eased(progress));
	}

	private static double eased(double progress) {
		double clamped = Math.clamp(progress, 0.0, 1.0);
		return clamped * clamped * (3.0 - 2.0 * clamped);
	}

	private static float wrapDegrees(float degrees) {
		float wrapped = degrees % 360.0F;
		if (wrapped >= 180.0F) {
			wrapped -= 360.0F;
		}
		if (wrapped < -180.0F) {
			wrapped += 360.0F;
		}
		return wrapped;
	}
}
