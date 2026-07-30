package io.qzz.iie.module.impl.render.betterhealth;

import java.util.Locale;

/**
 * 更好的血条中不依赖 Minecraft 渲染状态的显示规则。
 */
public final class BetterHealthBarPolicy {
	public static final float HEALTH_POINTS_PER_ROW = 20.0F;

	private BetterHealthBarPolicy() {
	}

	public static boolean shouldShowNumber(
		boolean enabled,
		int thresholdRows,
		float maximumHealth
	) {
		if (!enabled) {
			return false;
		}
		return thresholdRows == -1
			|| maximumHealth > thresholdRows * HEALTH_POINTS_PER_ROW;
	}

	public static float visibleMaximumHealth(
		boolean enabled,
		int thresholdRows,
		float maximumHealth
	) {
		if (!enabled || thresholdRows == -1) {
			return maximumHealth;
		}
		return Math.min(maximumHealth, thresholdRows * HEALTH_POINTS_PER_ROW);
	}

	public static String formatHealth(float health) {
		float safeHealth = Float.isFinite(health) ? Math.max(0.0F, health) : 0.0F;
		float rounded = Math.round(safeHealth * 10.0F) / 10.0F;
		if (rounded == Math.rint(rounded)) {
			return Integer.toString((int) rounded);
		}
		return String.format(Locale.ROOT, "%.1f", rounded);
	}
}
