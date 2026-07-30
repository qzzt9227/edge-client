package io.qzz.iie.api.hud;

import net.minecraft.client.gui.screens.Screen;

/**
 * 注册并打开模块无关的 HUD 位置编辑器。
 */
public interface HudPositionEditorApi {
	void register(HudPositionSetting setting, HudElementPreview preview);

	void open(HudPositionSetting setting, Screen parent);

	static HudPositionEditorApi noop() {
		return Noop.INSTANCE;
	}

	enum Noop implements HudPositionEditorApi {
		INSTANCE;

		@Override
		public void register(HudPositionSetting setting, HudElementPreview preview) {
		}

		@Override
		public void open(HudPositionSetting setting, Screen parent) {
		}
	}
}
