package io.qzz.iie.module.impl.render.crystalanimation;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;

import java.util.List;

/**
 * 调整末地水晶实体旋转与动画的渲染模块。
 *
 * <p>支持“静止”与“旋转”两种模式：静止模式下水晶完全不转不晃，旋转模式下
 * 禁止上下浮动并支持自定义水平自旋速度。</p>
 */
public final class CrystalAnimationModule extends Module {
	private final ChoiceSetting<CrystalAnimationMode> mode = setting(new ChoiceSetting<>(
		"mode",
		"client.setting.crystal_animation.mode",
		CrystalAnimationMode.SPIN,
		List.of(
			new ChoiceOption<>(
				"static",
				"client.option.crystal_animation.static",
				CrystalAnimationMode.STATIC
			),
			new ChoiceOption<>(
				"spin",
				"client.option.crystal_animation.spin",
				CrystalAnimationMode.SPIN
			)
		)
	));

	private final DoubleSetting speed = setting(new DoubleSetting(
		"speed",
		"client.setting.crystal_animation.speed",
		5.2,
		0.0,
		10.0,
		0.1
	).visibleWhen(() -> mode.value() == CrystalAnimationMode.SPIN));

	private final DoubleSetting offsetX = setting(new DoubleSetting(
		"offset_x",
		"client.setting.crystal_animation.offset_x",
		0.0,
		-5.0,
		5.0,
		0.1
	));

	private final DoubleSetting offsetY = setting(new DoubleSetting(
		"offset_y",
		"client.setting.crystal_animation.offset_y",
		0.0,
		-5.0,
		5.0,
		0.1
	));

	private final DoubleSetting offsetZ = setting(new DoubleSetting(
		"offset_z",
		"client.setting.crystal_animation.offset_z",
		0.0,
		-5.0,
		5.0,
		0.1
	));

	private final DoubleSetting scale = setting(new DoubleSetting(
		"scale",
		"client.setting.crystal_animation.scale",
		1.1,
		0.1,
		5.0,
		0.1
	));

	private final KeybindSetting shortcut = keybind(
		new KeybindSetting("keybind", "client.setting.module_keybind")
	);

	public CrystalAnimationModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "crystal_animation"),
			"client.module.crystal_animation.name",
			"client.module.crystal_animation.description",
			100
		));
	}

	public ChoiceSetting<CrystalAnimationMode> modeSetting() {
		return mode;
	}

	public DoubleSetting speedSetting() {
		return speed;
	}

	public DoubleSetting offsetXSetting() {
		return offsetX;
	}

	public DoubleSetting offsetYSetting() {
		return offsetY;
	}

	public DoubleSetting offsetZSetting() {
		return offsetZ;
	}

	public DoubleSetting scaleSetting() {
		return scale;
	}
}
