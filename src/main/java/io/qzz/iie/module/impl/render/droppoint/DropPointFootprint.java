package io.qzz.iie.module.impl.render.droppoint;

import java.util.Objects;

/** Selects the one block with the largest horizontal overlap under a collision box. */
public final class DropPointFootprint {
	private DropPointFootprint() {
	}

	public static BlockCell largestCoveredCell(
		double minX,
		double maxX,
		double minZ,
		double maxZ,
		int baseY
	) {
		if (!(maxX > minX) || !(maxZ > minZ)
			|| !Double.isFinite(minX) || !Double.isFinite(maxX)
			|| !Double.isFinite(minZ) || !Double.isFinite(maxZ)) {
			return null;
		}
		int firstX = (int) Math.floor(minX);
		int lastX = (int) Math.ceil(maxX) - 1;
		int firstZ = (int) Math.floor(minZ);
		int lastZ = (int) Math.ceil(maxZ) - 1;
		BlockCell best = null;
		for (int x = firstX; x <= lastX; x++) {
			for (int z = firstZ; z <= lastZ; z++) {
				double area = Math.max(0.0, Math.min(maxX, x + 1.0) - Math.max(minX, x))
					* Math.max(0.0, Math.min(maxZ, z + 1.0) - Math.max(minZ, z));
				BlockCell candidate = new BlockCell(x, baseY, z, area);
				if (best == null || candidate.area() > best.area()
					|| candidate.area() == best.area() && candidate.isBefore(best)) {
					best = candidate;
				}
			}
		}
		return best;
	}

	public record BlockCell(int x, int y, int z, double area) {
		public BlockCell {
			if (!Double.isFinite(area) || area < 0.0) {
				throw new IllegalArgumentException("area must be finite and non-negative");
			}
		}

		private boolean isBefore(BlockCell other) {
			Objects.requireNonNull(other, "other");
			return x < other.x || x == other.x && z < other.z;
		}
	}
}
