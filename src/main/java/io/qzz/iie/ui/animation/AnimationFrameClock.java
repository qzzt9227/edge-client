package io.qzz.iie.ui.animation;

import java.util.Objects;
import java.util.function.LongSupplier;

public final class AnimationFrameClock {
	private static final double MAXIMUM_DELTA_SECONDS = 0.05;

	private final LongSupplier nanoTime;
	private long previousNanos = Long.MIN_VALUE;

	public AnimationFrameClock() {
		this(System::nanoTime);
	}

	public AnimationFrameClock(LongSupplier nanoTime) {
		this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
	}

	public double nextDeltaSeconds() {
		long currentNanos = nanoTime.getAsLong();
		if (previousNanos == Long.MIN_VALUE) {
			previousNanos = currentNanos;
			return 0.0;
		}

		long elapsedNanos = Math.max(0L, currentNanos - previousNanos);
		previousNanos = currentNanos;
		return Math.min(elapsedNanos / 1_000_000_000.0, MAXIMUM_DELTA_SECONDS);
	}

	public void reset() {
		previousNanos = Long.MIN_VALUE;
	}
}
