package io.qzz.iie.ui.animation;

import io.qzz.iie.ui.layout.Rect;

import java.util.Objects;

public final class AnimatedRect {
	private final AnimatedDouble x;
	private final AnimatedDouble y;
	private final AnimatedDouble width;
	private final AnimatedDouble height;

	public AnimatedRect(Rect initialBounds, AnimationSpec spec) {
		Objects.requireNonNull(initialBounds, "initialBounds");
		x = new AnimatedDouble(initialBounds.x(), spec);
		y = new AnimatedDouble(initialBounds.y(), spec);
		width = new AnimatedDouble(initialBounds.width(), spec);
		height = new AnimatedDouble(initialBounds.height(), spec);
	}

	public Rect value() {
		return new Rect(x.value(), y.value(), width.value(), height.value());
	}

	public boolean isRunning() {
		return x.isRunning() || y.isRunning() || width.isRunning() || height.isRunning();
	}

	public void animateTo(Rect bounds) {
		Objects.requireNonNull(bounds, "bounds");
		x.animateTo(bounds.x());
		y.animateTo(bounds.y());
		width.animateTo(bounds.width());
		height.animateTo(bounds.height());
	}

	public void snapTo(Rect bounds) {
		Objects.requireNonNull(bounds, "bounds");
		x.snapTo(bounds.x());
		y.snapTo(bounds.y());
		width.snapTo(bounds.width());
		height.snapTo(bounds.height());
	}

	public Rect advance(double deltaSeconds) {
		x.advance(deltaSeconds);
		y.advance(deltaSeconds);
		width.advance(deltaSeconds);
		height.advance(deltaSeconds);
		return value();
	}
}
