package io.qzz.iie.ui.animation;

@FunctionalInterface
public interface Easing {
	Easing LINEAR = progress -> progress;
	Easing CUBIC_OUT = progress -> 1.0 - Math.pow(1.0 - progress, 3.0);
	Easing CUBIC_IN_OUT = progress -> progress < 0.5
		? 4.0 * progress * progress * progress
		: 1.0 - Math.pow(-2.0 * progress + 2.0, 3.0) / 2.0;

	double apply(double progress);
}
