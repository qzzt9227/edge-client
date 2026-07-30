package io.qzz.iie.ui.input;

public enum MouseButton {
	LEFT,
	RIGHT,
	MIDDLE,
	OTHER;

	public static MouseButton fromCode(int button) {
		return switch (button) {
			case 0 -> LEFT;
			case 1 -> RIGHT;
			case 2 -> MIDDLE;
			default -> OTHER;
		};
	}
}
