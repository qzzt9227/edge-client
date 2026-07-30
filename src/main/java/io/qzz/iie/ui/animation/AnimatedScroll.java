package io.qzz.iie.ui.animation;

import java.util.Objects;

/**
 * 有边界的平滑滚动状态。
 *
 * <p>滚轮输入只移动目标位置，显示位置由共享帧时间推进，因此连续输入和反向输入
 * 都会从当前可见位置平滑重定向。</p>
 */
public final class AnimatedScroll {
	private final AnimatedDouble offset;
	private double maximum;

	public AnimatedScroll(AnimationSpec spec) {
		offset = new AnimatedDouble(0.0, Objects.requireNonNull(spec, "spec"));
	}

	public double value() {
		return offset.value();
	}

	public double target() {
		return offset.target();
	}

	public boolean isRunning() {
		return offset.isRunning();
	}

	public void setMaximum(double maximumValue) {
		requireFinite(maximumValue, "maximumValue");
		if (maximumValue < 0.0) {
			throw new IllegalArgumentException("Maximum scroll must be non-negative");
		}

		double clampedCurrent = Math.clamp(offset.value(), 0.0, maximumValue);
		double clampedTarget = Math.clamp(offset.target(), 0.0, maximumValue);
		maximum = maximumValue;
		if (Double.compare(clampedCurrent, offset.value()) != 0) {
			offset.snapTo(clampedCurrent);
		}
		offset.animateTo(clampedTarget);
	}

	public void scrollBy(double amount) {
		requireFinite(amount, "amount");
		offset.animateTo(Math.clamp(offset.target() + amount, 0.0, maximum));
	}

	public double advance(double deltaSeconds) {
		return offset.advance(deltaSeconds);
	}

	public void reset() {
		offset.snapTo(0.0);
	}

	private static void requireFinite(double value, String name) {
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException(name + " must be finite");
		}
	}
}
