package io.qzz.iie.module.impl.render.droppoint;

import io.qzz.iie.setting.ColorSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.Setting;

import java.util.Objects;
import java.util.function.Function;

/** 包含 ColorSetting 与 opacity DoubleSetting 的独立半透明颜色设置组合。 */
public final class DropPointColorSettings {
	private final ColorSetting color;
	private final DoubleSetting opacity;

	public DropPointColorSettings(
		Function<Setting<?>, Setting<?>> register,
		String idPrefix,
		int defaultRgb,
		double defaultOpacity
	) {
		Objects.requireNonNull(register, "register");
		Objects.requireNonNull(idPrefix, "idPrefix");
		color = (ColorSetting) register.apply(new ColorSetting(
			idPrefix + "_color",
			"client.setting.drop_point." + idPrefix + ".color",
			defaultRgb,
			false
		));
		opacity = (DoubleSetting) register.apply(new DoubleSetting(
			idPrefix + "_opacity",
			"client.setting.drop_point." + idPrefix + ".opacity",
			defaultOpacity,
			0.0,
			1.0,
			0.05
		));
	}

	public DropPointColor color() {
		return new DropPointColor(
			color.red() / 255.0,
			color.green() / 255.0,
			color.blue() / 255.0,
			opacity.value()
		);
	}

	public ColorSetting colorSetting() {
		return color;
	}

	public DoubleSetting opacity() {
		return opacity;
	}
}
