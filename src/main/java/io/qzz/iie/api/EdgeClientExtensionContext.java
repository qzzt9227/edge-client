package io.qzz.iie.api;

import io.qzz.iie.api.message.MessageBoxApi;
import io.qzz.iie.api.hud.HudPositionEditorApi;
import io.qzz.iie.api.setting.SettingEditorApi;
import io.qzz.iie.module.Module;

/**
 * Registration-only surface exposed to dependent client mods.
 */
@FunctionalInterface
public interface EdgeClientExtensionContext {
	void registerModule(Module module);

	/**
	 * 返回共享消息提示框 API。默认空实现保持旧上下文实现的源码兼容性。
	 */
	default MessageBoxApi messages() {
		return MessageBoxApi.noop();
	}

	/**
	 * 返回共享 HUD 位置编辑 API。默认空实现保持旧扩展上下文兼容。
	 */
	default HudPositionEditorApi hudPositions() {
		return HudPositionEditorApi.noop();
	}

	/**
	 * 返回复杂设置编辑子页注册 API。
	 */
	default SettingEditorApi settingEditors() {
		return SettingEditorApi.noop();
	}
}
