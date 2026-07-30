package io.qzz.iie.ui.animation;

public final class ArgbColor {
	private ArgbColor() {
	}

	public static int interpolate(int from, int to, double progress) {
		if (!Double.isFinite(progress)) {
			throw new IllegalArgumentException("Color interpolation progress must be finite");
		}
		double clamped = Math.clamp(progress, 0.0, 1.0);
		int alpha = channel(from, to, 24, clamped);
		int red = channel(from, to, 16, clamped);
		int green = channel(from, to, 8, clamped);
		int blue = channel(from, to, 0, clamped);
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	private static int channel(int from, int to, int shift, double progress) {
		int start = from >>> shift & 0xFF;
		int end = to >>> shift & 0xFF;
		return (int) Math.round(start + (end - start) * progress);
	}
}
