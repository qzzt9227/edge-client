package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.world.phys.Vec3;

/**
 * 从掉落物快照中筛选范围内离玩家最近的有效讲台。
 */
public final class LecternDropSelection {
	private LecternDropSelection() {
	}

	public static <T> T nearest(
		Iterable<Candidate<T>> candidates,
		Vec3 playerPosition,
		Vec3 scanCenter,
		double radius
	) {
		T nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		double radiusSquared = radius * radius;
		for (Candidate<T> candidate : candidates) {
			if (!candidate.alive()
				|| !candidate.lectern()
				|| candidate.position().distanceToSqr(scanCenter) > radiusSquared) {
				continue;
			}
			double distance = candidate.position().distanceToSqr(playerPosition);
			if (distance < nearestDistance) {
				nearest = candidate.value();
				nearestDistance = distance;
			}
		}
		return nearest;
	}

	public record Candidate<T>(
		T value,
		Vec3 position,
		boolean alive,
		boolean lectern
	) {
	}
}
