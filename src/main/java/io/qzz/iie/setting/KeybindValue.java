package io.qzz.iie.setting;

/**
 * A keyboard shortcut represented by a GLFW key code.
 */
public record KeybindValue(int keyCode) {
	public static final int UNBOUND_KEY = -1;

	public static KeybindValue unbound() {
		return new KeybindValue(UNBOUND_KEY);
	}

	public boolean isBound() {
		return keyCode != UNBOUND_KEY;
	}
}
