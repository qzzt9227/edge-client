package io.qzz.iie.module.impl.render.droppoint;

/** Pure fall-damage calculation matching the vanilla fall-distance formula and landing modifier. */
public final class DropPointFallDamage {
	private DropPointFallDamage() {
	}

	public static double calculate(
		double fallDistance,
		double safeFallDistance,
		double multiplier,
		double landingMultiplier
	) {
		if (!Double.isFinite(fallDistance) || !Double.isFinite(safeFallDistance)
			|| !Double.isFinite(multiplier) || !Double.isFinite(landingMultiplier)) {
			return 0.0;
		}
		return Math.max(
			0.0,
			Math.floor(Math.max(0.0, fallDistance + 1.0E-6 - safeFallDistance) * multiplier * landingMultiplier)
		);
	}
}
