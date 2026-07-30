package io.qzz.iie.ui.hud;

import io.qzz.iie.api.hud.HudElementPreview;
import io.qzz.iie.api.hud.HudPositionEditorApi;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.ui.screen.HudPositionEditorScreen;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 通用 HUD 位置编辑器的运行时注册表。
 */
public final class HudPositionEditorManager implements HudPositionEditorApi {
	private static final List<Identifier> HIDDEN_WHILE_EDITING = List.of(
		VanillaHudElements.MISC_OVERLAYS,
		VanillaHudElements.SPECTATOR_MENU,
		VanillaHudElements.ARMOR_BAR,
		VanillaHudElements.FOOD_BAR,
		VanillaHudElements.AIR_BAR,
		VanillaHudElements.MOUNT_HEALTH,
		VanillaHudElements.INFO_BAR,
		VanillaHudElements.EXPERIENCE_LEVEL,
		VanillaHudElements.HELD_ITEM_TOOLTIP,
		VanillaHudElements.SPECTATOR_TOOLTIP,
		VanillaHudElements.MOB_EFFECTS,
		VanillaHudElements.BOSS_BAR,
		VanillaHudElements.SLEEP,
		VanillaHudElements.DEMO_TIMER,
		VanillaHudElements.SCOREBOARD,
		VanillaHudElements.OVERLAY_MESSAGE,
		VanillaHudElements.TITLE_AND_SUBTITLE,
		VanillaHudElements.CHAT,
		VanillaHudElements.PLAYER_LIST,
		VanillaHudElements.SUBTITLES
	);

	private final Map<HudPositionSetting, HudElementPreview> previews =
		new IdentityHashMap<>();
	private boolean vanillaVisibilityInstalled;

	@Override
	public void register(HudPositionSetting setting, HudElementPreview preview) {
		Objects.requireNonNull(setting, "setting");
		Objects.requireNonNull(preview, "preview");
		if (previews.putIfAbsent(setting, preview) != null) {
			throw new IllegalArgumentException(
				"HUD position setting is already registered: " + setting.id()
			);
		}
	}

	@Override
	public void open(HudPositionSetting setting, Screen parent) {
		HudElementPreview preview = previews.get(
			Objects.requireNonNull(setting, "setting")
		);
		if (preview == null) {
			throw new IllegalArgumentException(
				"HUD position setting has no registered preview: " + setting.id()
			);
		}
		Minecraft.getInstance().setScreenAndShow(
			new HudPositionEditorScreen(setting, preview, parent)
		);
	}

	public boolean isEditing() {
		return Minecraft.getInstance().gui.screen() instanceof HudPositionEditorScreen;
	}

	static boolean isVanillaElementVisibleDuringEditing(Identifier id) {
		return !HIDDEN_WHILE_EDITING.contains(Objects.requireNonNull(id, "id"));
	}

	public void installVanillaVisibility() {
		if (vanillaVisibilityInstalled) {
			return;
		}
		vanillaVisibilityInstalled = true;
		for (Identifier id : HIDDEN_WHILE_EDITING) {
			HudElementRegistry.replaceElement(id, oldElement -> (graphics, deltaTracker) -> {
				if (!isEditing()) {
					oldElement.extractRenderState(graphics, deltaTracker);
				}
			});
		}
	}
}
