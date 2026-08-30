package io.qzz.iie.module.impl.render.zoom;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;

/**
 * 放大玩家视野的纯客户端模块。
 *
 * <p>模块声明放大倍率、平滑过渡、降低鼠标灵敏度与快捷键；
 * 实际 FOV 缩放与灵敏度缩减由 {@link ZoomHooks} 在
 * {@code Camera} 和 {@code MouseHandler} 的 Mixin 注入点完成。</p>
 */
public final class ZoomModule extends Module {
	private final DoubleSetting zoomFactor = setting(new DoubleSetting(
		"zoom_factor",
		"client.setting.zoom.zoom_factor",
		7.5,
		1.5,
		20.0,
		0.5
	));
	private final BooleanSetting smoothZoom = setting(new BooleanSetting(
		"smooth_zoom",
		"client.setting.zoom.smooth_zoom",
		true
	));
	private final BooleanSetting reduceSensitivity = setting(new BooleanSetting(
		"reduce_sensitivity",
		"client.setting.zoom.reduce_sensitivity",
		true
	));
	private final KeybindSetting shortcut = keybind(
		new KeybindSetting("keybind", "client.setting.module_keybind")
	);

	public ZoomModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "zoom"),
			"client.module.zoom.name",
			"client.module.zoom.description",
			100
		));
	}

	public DoubleSetting zoomFactor() {
		return zoomFactor;
	}

	public BooleanSetting smoothZoom() {
		return smoothZoom;
	}

	public BooleanSetting reduceSensitivity() {
		return reduceSensitivity;
	}

	@Override
	protected void onEnable() {
		ZoomHooks.onStateChanged();
	}

	@Override
	protected void onDisable() {
		ZoomHooks.onStateChanged();
	}
}
