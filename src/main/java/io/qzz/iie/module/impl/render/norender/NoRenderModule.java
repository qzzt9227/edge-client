package io.qzz.iie.module.impl.render.norender;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.KeybindSetting;

public final class NoRenderModule extends Module {

	private final BooleanSetting particles = setting(new BooleanSetting(
		"particles",
		"client.setting.no_render.particles",
		false
	));

	private final BooleanSetting signText = setting(new BooleanSetting(
		"sign_text",
		"client.setting.no_render.sign_text",
		true
	));

	private final BooleanSetting maps = setting(new BooleanSetting(
		"maps",
		"client.setting.no_render.maps",
		true
	));

	private final BooleanSetting bannerPatterns = setting(new BooleanSetting(
		"banner_patterns",
		"client.setting.no_render.banner_patterns",
		true
	));

	private final BooleanSetting fire = setting(new BooleanSetting(
		"fire",
		"client.setting.no_render.fire",
		true
	));

	private final BooleanSetting darkness = setting(new BooleanSetting(
		"darkness",
		"client.setting.no_render.darkness",
		true
	));

	private final BooleanSetting blindness = setting(new BooleanSetting(
		"blindness",
		"client.setting.no_render.blindness",
		true
	));

	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public NoRenderModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "no_render"),
			"client.module.no_render.name",
			"client.module.no_render.description",
			310
		));
	}

	public BooleanSetting particles() {
		return particles;
	}

	public BooleanSetting signText() {
		return signText;
	}

	public BooleanSetting maps() {
		return maps;
	}

	public BooleanSetting bannerPatterns() {
		return bannerPatterns;
	}

	public BooleanSetting fire() {
		return fire;
	}

	public BooleanSetting darkness() {
		return darkness;
	}

	public BooleanSetting blindness() {
		return blindness;
	}
}
