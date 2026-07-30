package io.qzz.iie.module.impl.combat.autoweb;

public final class AutoWebRotation {
	private AutoWebRotation() {
	}

	public static float interpolateAngle(float start, float target, double progress) {
		double safeProgress = Math.clamp(progress, 0.0, 1.0);
		return (float) (start + wrapDegrees(target - start) * safeProgress);
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
