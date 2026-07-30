package io.qzz.iie.api.setting;

import io.qzz.iie.setting.EditorSetting;
import net.minecraft.client.gui.screens.Screen;

/**
 * 复杂设置编辑子页的注册与打开 API。
 */
public interface SettingEditorApi {
	void register(String editorId, SettingEditorFactory factory);

	boolean supports(EditorSetting<?> setting);

	void open(EditorSetting<?> setting, Screen parent);

	static SettingEditorApi noop() {
		return new SettingEditorApi() {
			@Override
			public void register(String editorId, SettingEditorFactory factory) {
			}

			@Override
			public boolean supports(EditorSetting<?> setting) {
				return false;
			}

			@Override
			public void open(EditorSetting<?> setting, Screen parent) {
			}
		};
	}
}
