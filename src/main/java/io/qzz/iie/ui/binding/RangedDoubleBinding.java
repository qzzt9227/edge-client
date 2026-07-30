package io.qzz.iie.ui.binding;

/**
 * 可复用滑块与实际数值来源之间的绑定。
 *
 * <p>绑定可以直接指向普通设置，也可以指向复合设置中的一个数值，避免为了绘制滑块
 * 再创建一份不同步的临时设置。</p>
 */
public interface RangedDoubleBinding extends ValueBinding<Double> {
	double minimum();

	double maximum();

	double step();

	default double fraction() {
		if (minimum() == maximum()) {
			return 0.0;
		}
		return (normalize(get()) - minimum()) / (maximum() - minimum());
	}

	default BindingUpdateResult setFraction(double fraction) {
		double value = minimum()
			+ Math.clamp(fraction, 0.0, 1.0) * (maximum() - minimum());
		return set(normalize(value));
	}

	default double normalize(double requestedValue) {
		validateRange();
		double clamped = Math.clamp(requestedValue, minimum(), maximum());
		double steps = Math.round((clamped - minimum()) / step());
		double aligned = minimum() + steps * step();
		return Math.clamp(
			Math.round(aligned * 1_000_000_000D) / 1_000_000_000D,
			minimum(),
			maximum()
		);
	}

	private void validateRange() {
		if (!Double.isFinite(minimum())
			|| !Double.isFinite(maximum())
			|| !Double.isFinite(step())) {
			throw new IllegalStateException("Slider bounds must be finite");
		}
		if (maximum() < minimum()) {
			throw new IllegalStateException("Slider maximum must be >= minimum");
		}
		if (step() <= 0.0) {
			throw new IllegalStateException("Slider step must be > 0");
		}
	}
}
