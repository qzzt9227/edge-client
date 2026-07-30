package io.qzz.iie.ui.animation;

import java.util.Objects;

public record AnimationSpec(double durationSeconds, Easing easing) {
	public AnimationSpec {
		if (!Double.isFinite(durationSeconds) || durationSeconds <= 0.0) {
			throw new IllegalArgumentException("Animation duration must be finite and positive");
		}
		Objects.requireNonNull(easing, "easing");
	}
}
