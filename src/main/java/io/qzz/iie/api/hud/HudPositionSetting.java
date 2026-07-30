package io.qzz.iie.api.hud;

import io.qzz.iie.setting.Setting;

/**
 * 自动获得配置持久化和 HUD 定位编辑控件的位置设置。
 */
public final class HudPositionSetting extends Setting<HudPosition> {
	public HudPositionSetting(
		String id,
		String translationKey,
		HudPosition defaultValue
	) {
		super(id, translationKey, defaultValue);
	}

	@Override
	protected HudPosition normalize(HudPosition requestedValue) {
		return new HudPosition(requestedValue.x(), requestedValue.y());
	}
}
