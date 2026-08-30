package io.qzz.iie.setting;

import java.util.Objects;

/**
 * 范围滑动条设置，用于支持用户在 [rangeMinimum, rangeMaximum] 边界内自由调节
 * 起始值 (min) 与结束值 (max) 的区间范围。
 */
public final class DoubleRangeSetting extends Setting<DoubleRange> {
	private final double rangeMinimum;
	private final double rangeMaximum;
	private final double step;

	public DoubleRangeSetting(
		String id,
		String translationKey,
		DoubleRange defaultValue,
		double rangeMinimum,
		double rangeMaximum,
		double step
	) {
		super(
			id,
			translationKey,
			normalize(defaultValue, rangeMinimum, rangeMaximum, step)
		);
		validateBounds(rangeMinimum, rangeMaximum, step);
		this.rangeMinimum = rangeMinimum;
		this.rangeMaximum = rangeMaximum;
		this.step = step;
	}

	public DoubleRangeSetting(
		String id,
		String translationKey,
		double defaultMin,
		double defaultMax,
		double rangeMinimum,
		double rangeMaximum,
		double step
	) {
		this(
			id,
			translationKey,
			new DoubleRange(defaultMin, defaultMax),
			rangeMinimum,
			rangeMaximum,
			step
		);
	}

	public double minimum() {
		return value().min();
	}

	public double maximum() {
		return value().max();
	}

	public double rangeMinimum() {
		return rangeMinimum;
	}

	public double rangeMaximum() {
		return rangeMaximum;
	}

	public double step() {
		return step;
	}

	public double minFraction() {
		if (rangeMinimum == rangeMaximum) {
			return 0.0;
		}
		return (value().min() - rangeMinimum) / (rangeMaximum - rangeMinimum);
	}

	public double maxFraction() {
		if (rangeMinimum == rangeMaximum) {
			return 1.0;
		}
		return (value().max() - rangeMinimum) / (rangeMaximum - rangeMinimum);
	}

	public void setMin(double min) {
		set(new DoubleRange(min, value().max()));
	}

	public void setMax(double max) {
		set(new DoubleRange(value().min(), max));
	}

	public void setRange(double min, double max) {
		set(new DoubleRange(min, max));
	}

	public void setMinFraction(double fraction) {
		setMin(rangeMinimum + Math.clamp(fraction, 0.0, 1.0) * (rangeMaximum - rangeMinimum));
	}

	public void setMaxFraction(double fraction) {
		setMax(rangeMinimum + Math.clamp(fraction, 0.0, 1.0) * (rangeMaximum - rangeMinimum));
	}

	public double randomValue() {
		return value().randomValue();
	}

	@Override
	protected DoubleRange normalize(DoubleRange requestedValue) {
		return normalize(requestedValue, rangeMinimum, rangeMaximum, step);
	}

	private static DoubleRange normalize(
		DoubleRange range,
		double rangeMin,
		double rangeMax,
		double step
	) {
		Objects.requireNonNull(range, "range");
		validateBounds(rangeMin, rangeMax, step);

		double normMin = normalizeValue(range.min(), rangeMin, rangeMax, step);
		double normMax = normalizeValue(range.max(), rangeMin, rangeMax, step);

		if (normMin > normMax) {
			double temp = normMin;
			normMin = normMax;
			normMax = temp;
		}
		return new DoubleRange(normMin, normMax);
	}

	private static double normalizeValue(
		double value,
		double minimum,
		double maximum,
		double step
	) {
		double clamped = Math.clamp(value, minimum, maximum);
		double steps = Math.round((clamped - minimum) / step);
		double aligned = minimum + steps * step;
		return Math.clamp(Math.round(aligned * 1_000_000_000D) / 1_000_000_000D, minimum, maximum);
	}

	private static void validateBounds(double minimum, double maximum, double step) {
		if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || !Double.isFinite(step)) {
			throw new IllegalArgumentException("Range bounds must be finite");
		}
		if (maximum < minimum) {
			throw new IllegalArgumentException("maximum must be >= minimum");
		}
		if (step <= 0.0) {
			throw new IllegalArgumentException("step must be > 0");
		}
	}
}
