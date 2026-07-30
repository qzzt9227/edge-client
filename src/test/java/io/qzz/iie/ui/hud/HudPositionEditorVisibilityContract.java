package io.qzz.iie.ui.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public final class HudPositionEditorVisibilityContract {
	private HudPositionEditorVisibilityContract() {
	}

	public static void verify() {
		check(
			HudPositionEditorManager.isVanillaElementVisibleDuringEditing(
				VanillaHudElements.CROSSHAIR
			),
			"crosshair must remain visible while positioning a HUD element"
		);
		check(
			HudPositionEditorManager.isVanillaElementVisibleDuringEditing(
				VanillaHudElements.HOTBAR
			),
			"hotbar must remain visible while positioning a HUD element"
		);
		check(
			HudPositionEditorManager.isVanillaElementVisibleDuringEditing(
				VanillaHudElements.HEALTH_BAR
			),
			"health bar must remain visible while positioning a HUD element"
		);
		check(
			!HudPositionEditorManager.isVanillaElementVisibleDuringEditing(
				VanillaHudElements.ARMOR_BAR
			),
			"unrelated HUD layers must remain hidden while positioning"
		);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
