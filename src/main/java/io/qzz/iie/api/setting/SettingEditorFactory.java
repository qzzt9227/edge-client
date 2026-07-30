package io.qzz.iie.api.setting;

import io.qzz.iie.setting.EditorSetting;
import net.minecraft.client.gui.screens.Screen;

/**
 * 为一个声明式复杂设置创建编辑子页。
 */
@FunctionalInterface
public interface SettingEditorFactory {
	Screen create(EditorSetting<?> setting, Screen parent);
}
