package io.qzz.iie.ui.screen;

public final class ClickGuiLayoutContract {
	private ClickGuiLayoutContract() {
	}

	public static void verifyComfortableCenteredWindow() {
		ClickGuiLayout layout = ClickGuiLayout.calculate(1000, 600, false);
		check(layout.windowWidth() <= 860, "GUI width must leave comfortable side margins");
		check(layout.windowHeight() <= 510, "GUI height must leave comfortable top and bottom margins");
		check(
			layout.windowX() * 2 + layout.windowWidth() == 1000,
			"GUI must be horizontally centered"
		);
		check(
			layout.windowY() * 2 + layout.windowHeight() == 600,
			"GUI must be vertically centered"
		);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
