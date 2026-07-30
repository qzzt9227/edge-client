package io.qzz.iie.setting;

public final class DoubleSetting extends Setting<Double> {
	private final double minimum;
	private final double maximum;
	private final double step;

	public DoubleSetting(
		String id,
		String translationKey,
		double defaultValue,
		double minimum,
		double maximum,
		double step
	) {
		super(id, translationKey, normalize(defaultValue, minimum, maximum, step));
		validateRange(minimum, maximum, step);
		this.minimum = minimum;
		this.maximum = maximum;
		this.step = step;
	}

	public double minimum() {
		return minimum;
	}

	public double maximum() {
		return maximum;
	}

	public double step() {
		return step;
	}

	public double fraction() {
		if (minimum == maximum) {
			return 0.0;
		}
		return (value() - minimum) / (maximum - minimum);
	}

	public void setFraction(double fraction) {
		set(minimum + Math.clamp(fraction, 0.0, 1.0) * (maximum - minimum));
	}

	@Override
	protected Double normalize(Double requestedValue) {
		return normalize(requestedValue, minimum, maximum, step);
	}

	private static double normalize(double value, double minimum, double maximum, double step) {
		validateRange(minimum, maximum, step);
		double clamped = Math.clamp(value, minimum, maximum);
		double steps = Math.round((clamped - minimum) / step);
		double aligned = minimum + steps * step;
		return Math.clamp(Math.round(aligned * 1_000_000_000D) / 1_000_000_000D, minimum, maximum);
	}

	private static void validateRange(double minimum, double maximum, double step) {
		if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || !Double.isFinite(step)) {
			throw new IllegalArgumentException("Double setting bounds must be finite");
		}
		if (maximum < minimum) {
			throw new IllegalArgumentException("maximum must be >= minimum");
		}
		if (step <= 0.0) {
			throw new IllegalArgumentException("step must be > 0");
		}
	}
}
