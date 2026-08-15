package io.qzz.iie.module.impl.render.droppoint;

/** Immutable RGBA color used by the world-surface renderer. */
public record DropPointColor(double red, double green, double blue, double opacity) {
	public DropPointColor {
		red = channel(red, "red");
		green = channel(green, "green");
		blue = channel(blue, "blue");
		opacity = channel(opacity, "opacity");
	}

	public float redFloat() {
		return (float) red;
	}

	public float greenFloat() {
		return (float) green;
	}

	public float blueFloat() {
		return (float) blue;
	}

	public float opacityFloat() {
		return (float) opacity;
	}

	public int argb() {
		return ((int) Math.round(opacity * 255.0) << 24)
			| ((int) Math.round(red * 255.0) << 16)
			| ((int) Math.round(green * 255.0) << 8)
			| (int) Math.round(blue * 255.0);
	}

	private static double channel(double value, String name) {
		if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
			throw new IllegalArgumentException(name + " must be finite and between 0 and 1");
		}
		return value;
	}
}
