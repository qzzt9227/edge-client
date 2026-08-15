package io.qzz.iie.module.impl.render.explosionwarning;

import java.util.Locale;

/** Pure countdown math shared by TNT and creeper render snapshots. */
public final class ExplosionCountdown {
	private static final double MILLIS_PER_TICK = 50.0;
	private static final double CREEPER_MAX_SWELL = 30.0;
	private static final double CREEPER_SWELL_RENDER_DENOMINATOR = 28.0;

	private ExplosionCountdown() {
	}

	public static long tntRemainingMillis(int fuseTicks, float partialTick) {
		return ticksToMillis(Math.max(0.0, fuseTicks - clampPartialTick(partialTick)));
	}

	public static long creeperRemainingMillis(float swelling) {
		if (!Float.isFinite(swelling)) {
			return 0L;
		}
		double swellTicks = Math.max(0.0, swelling) * CREEPER_SWELL_RENDER_DENOMINATOR;
		return ticksToMillis(Math.max(0.0, CREEPER_MAX_SWELL - swellTicks));
	}

	public static String formatMillis(long millis) {
		return String.format(Locale.ROOT, "%.3f s", Math.max(0L, millis) / 1000.0);
	}

	private static long ticksToMillis(double ticks) {
		return Math.max(0L, Math.round(ticks * MILLIS_PER_TICK));
	}

	private static float clampPartialTick(float partialTick) {
		return Float.isFinite(partialTick) ? Math.clamp(partialTick, 0.0F, 1.0F) : 0.0F;
	}
}
