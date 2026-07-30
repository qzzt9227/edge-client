package io.qzz.iie.ui.layout;

public record Rect(double x, double y, double width, double height) {
	public Rect {
		if (width < 0.0 || height < 0.0) {
			throw new IllegalArgumentException("Rect size must be non-negative");
		}
	}

	public boolean contains(double pointX, double pointY) {
		return pointX >= x
			&& pointX < x + width
			&& pointY >= y
			&& pointY < y + height;
	}

	public int left() {
		return (int) Math.round(x);
	}

	public int top() {
		return (int) Math.round(y);
	}

	public int right() {
		return (int) Math.round(x + width);
	}

	public int bottom() {
		return (int) Math.round(y + height);
	}
}
