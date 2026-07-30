package io.qzz.iie.ui.animation;

import java.util.Objects;

public final class AnimatedDouble {
	private final AnimationSpec spec;
	private double start;
	private double current;
	private double target;
	private double elapsedSeconds;
	private boolean running;

	public AnimatedDouble(double initialValue, AnimationSpec spec) {
		this.spec = Objects.requireNonNull(spec, "spec");
		requireFinite(initialValue, "initialValue");
		start = initialValue;
		current = initialValue;
		target = initialValue;
	}

	public double value() {
		return current;
	}

	public double target() {
		return target;
	}

	public boolean isRunning() {
		return running;
	}

	public void animateTo(double targetValue) {
		requireFinite(targetValue, "targetValue");
		if (Double.compare(target, targetValue) == 0) {
			return;
		}
		start = current;
		target = targetValue;
		elapsedSeconds = 0.0;
		running = Double.compare(start, target) != 0;
	}

	public void snapTo(double value) {
		requireFinite(value, "value");
		start = value;
		current = value;
		target = value;
		elapsedSeconds = 0.0;
		running = false;
	}

	public double advance(double deltaSeconds) {
		requireFinite(deltaSeconds, "deltaSeconds");
		if (deltaSeconds < 0.0) {
			throw new IllegalArgumentException("Animation delta must be non-negative");
		}
		if (!running) {
			return current;
		}

		elapsedSeconds = Math.min(spec.durationSeconds(), elapsedSeconds + deltaSeconds);
		double progress = elapsedSeconds / spec.durationSeconds();
		double easingOutput = spec.easing().apply(progress);
		if (!Double.isFinite(easingOutput)) {
			throw new IllegalStateException("Easing output must be finite");
		}
		double eased = Math.clamp(easingOutput, 0.0, 1.0);
		current = start + (target - start) * eased;
		if (elapsedSeconds >= spec.durationSeconds()) {
			current = target;
			running = false;
		}
		return current;
	}

	private static void requireFinite(double value, String name) {
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException(name + " must be finite");
		}
	}
}
