package io.qzz.iie.module.impl.combat.autoclicker;

import io.qzz.iie.setting.DoubleRange;

/**
 * 连点器时序控制器，独立调度左键与右键的随机波动点击。
 */
public final class AutoClickerController {
	private long nextLeftClickTimeMs;
	private long nextRightClickTimeMs;

	public void reset() {
		nextLeftClickTimeMs = 0L;
		nextRightClickTimeMs = 0L;
	}

	public boolean checkAndScheduleLeft(
		long currentTimeMs,
		boolean enabled,
		boolean isHolding,
		boolean holdOnly,
		DoubleRange cpsRange
	) {
		if (!enabled) {
			return false;
		}
		if (holdOnly && !isHolding) {
			nextLeftClickTimeMs = 0L;
			return false;
		}
		if (currentTimeMs < nextLeftClickTimeMs) {
			return false;
		}

		double cps = cpsRange != null ? cpsRange.randomValue() : 10.0;
		long intervalMs = calculateIntervalMs(cps);
		nextLeftClickTimeMs = currentTimeMs + intervalMs;
		return true;
	}

	public boolean checkAndScheduleRight(
		long currentTimeMs,
		boolean enabled,
		boolean isHolding,
		boolean holdOnly,
		DoubleRange cpsRange
	) {
		if (!enabled) {
			return false;
		}
		if (holdOnly && !isHolding) {
			nextRightClickTimeMs = 0L;
			return false;
		}
		if (currentTimeMs < nextRightClickTimeMs) {
			return false;
		}

		double cps = cpsRange != null ? cpsRange.randomValue() : 10.0;
		long intervalMs = calculateIntervalMs(cps);
		nextRightClickTimeMs = currentTimeMs + intervalMs;
		return true;
	}

	public long nextLeftClickTimeMs() {
		return nextLeftClickTimeMs;
	}

	public long nextRightClickTimeMs() {
		return nextRightClickTimeMs;
	}

	public static long calculateIntervalMs(double cps) {
		if (cps <= 0.0) {
			return 1000L;
		}
		return Math.max(1L, Math.round(1000.0 / cps));
	}
}
