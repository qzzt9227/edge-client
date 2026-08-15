package io.qzz.iie.module.impl.render.explosionwarning;

/** Pure placement policy for keeping world-space countdowns visible. */
public final class ExplosionWarningPlacement {
	private static final double FRONT_PADDING = 0.12;
	private static final double DIRECTION_EPSILON = 1.0E-6;

	private ExplosionWarningPlacement() {
	}

	public static Position resolve(
		ExplosionTargetKind kind,
		double entityX,
		double automaticY,
		double entityZ,
		double halfWidth,
		double halfDepth,
		double cameraX,
		double cameraZ,
		double offsetX,
		double offsetY,
		double offsetZ
	) {
		double targetX = entityX;
		double targetZ = entityZ;
		if (kind == ExplosionTargetKind.CREEPER) {
			double directionX = cameraX - entityX;
			double directionZ = cameraZ - entityZ;
			double length = Math.hypot(directionX, directionZ);
			if (length < DIRECTION_EPSILON) {
				directionX = 0.0;
				directionZ = 1.0;
			} else {
				directionX /= length;
				directionZ /= length;
			}
			double distanceToFace = Math.min(
				distanceToFace(Math.max(0.0, halfWidth), directionX),
				distanceToFace(Math.max(0.0, halfDepth), directionZ)
			);
			double frontDistance = distanceToFace + FRONT_PADDING;
			targetX += directionX * frontDistance;
			targetZ += directionZ * frontDistance;
		}
		return new Position(targetX + offsetX, automaticY + offsetY, targetZ + offsetZ);
	}

	private static double distanceToFace(double halfExtent, double direction) {
		return Math.abs(direction) < DIRECTION_EPSILON
			? Double.POSITIVE_INFINITY
			: halfExtent / Math.abs(direction);
	}

	public record Position(double x, double y, double z) {
	}
}
