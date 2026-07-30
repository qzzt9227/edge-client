package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.world.phys.Vec3;

/**
 * 与运行时控制器分离的回收完成判定。
 */
public final class RecoveryDecision {
	private static final double HORIZONTAL_TOLERANCE_SQUARED = 0.2 * 0.2;
	private static final double VERTICAL_TOLERANCE = 0.1;
	private static final double PICKUP_HORIZONTAL_TOLERANCE_SQUARED = 0.5 * 0.5;
	private static final double PICKUP_VERTICAL_TOLERANCE = 1.5;

	private RecoveryDecision() {
	}

	public static boolean inventoryRestored(int expectedLecterns, int currentLecterns) {
		return currentLecterns >= expectedLecterns;
	}

	public static boolean reached(Vec3 origin, Vec3 current) {
		double deltaX = origin.x - current.x;
		double deltaZ = origin.z - current.z;
		return deltaX * deltaX + deltaZ * deltaZ <= HORIZONTAL_TOLERANCE_SQUARED
			&& Math.abs(origin.y - current.y) <= VERTICAL_TOLERANCE;
	}

	public static boolean reachedPickupArea(Vec3 dropPosition, Vec3 playerPosition) {
		double deltaX = dropPosition.x - playerPosition.x;
		double deltaZ = dropPosition.z - playerPosition.z;
		return deltaX * deltaX + deltaZ * deltaZ
			<= PICKUP_HORIZONTAL_TOLERANCE_SQUARED
			&& Math.abs(dropPosition.y - playerPosition.y)
				<= PICKUP_VERTICAL_TOLERANCE;
	}
}
