package io.qzz.iie.module.impl.player.antiquit;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.KeybindSetting;

/**
 * 防误退模块。
 *
 * <p>启用后，在游戏菜单点击“断开连接”或点击系统窗口右上角“关闭”按钮时，
 * 弹出二次确认对话框，防止意外退出游戏。</p>
 */
public final class AntiQuitModule extends Module {
	private final BooleanSetting confirmDisconnect = setting(
		new BooleanSetting(
			"confirm_disconnect",
			"client.setting.anti_quit.confirm_disconnect",
			true
		)
	);

	private final BooleanSetting confirmWindowClose = setting(
		new BooleanSetting(
			"confirm_window_close",
			"client.setting.anti_quit.confirm_window_close",
			true
		)
	);

	private final KeybindSetting shortcut = keybind(
		new KeybindSetting(
			"keybind",
			"client.setting.module_keybind"
		)
	);

	public AntiQuitModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "anti_quit"),
			"client.module.anti_quit.name",
			"client.module.anti_quit.description",
			240
		));
	}

	public BooleanSetting confirmDisconnect() {
		return confirmDisconnect;
	}

	public BooleanSetting confirmWindowClose() {
		return confirmWindowClose;
	}

	@Override
	protected void onEnable() {
		AntiQuitHooks.install(this);
	}

	@Override
	protected void onDisable() {
		AntiQuitHooks.uninstall(this);
	}
}
