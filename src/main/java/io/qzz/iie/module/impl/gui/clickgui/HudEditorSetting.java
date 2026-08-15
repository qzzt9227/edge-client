package io.qzz.iie.module.impl.gui.clickgui;

import io.qzz.iie.setting.EditorSetting;

/**
 * 触发打开统一全屏 HUD 编辑器的特殊设置项。
 */
public final class HudEditorSetting extends EditorSetting<Boolean> {
	public static final String EDITOR_ID = "hud_editor";

	public HudEditorSetting(String id, String translationKey) {
		super(id, translationKey, Boolean.TRUE, EDITOR_ID);
	}

	@Override
	public Boolean normalize(Boolean value) {
		return Boolean.TRUE;
	}
}
