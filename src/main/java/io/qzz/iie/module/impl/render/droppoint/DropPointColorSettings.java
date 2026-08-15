package io.qzz.iie.module.impl.render.droppoint;

import io.qzz.iie.setting.DoubleSetting;

import java.util.Objects;
import java.util.function.Function;

/** Four slider-backed settings that expose one independently translucent RGBA color. */
public final class DropPointColorSettings {
	private final DoubleSetting red;
	private final DoubleSetting green;
	private final DoubleSetting blue;
	private final DoubleSetting opacity;

	public DropPointColorSettings(
		Function<DoubleSetting, DoubleSetting> register,
		String idPrefix,
		double defaultRed,
		double defaultGreen,
		double defaultBlue,
		double defaultOpacity
	) {
		Objects.requireNonNull(register, "register");
		Objects.requireNonNull(idPrefix, "idPrefix");
		red = register.apply(channel(idPrefix + "_red", idPrefix + ".red", defaultRed));
		green = register.apply(channel(idPrefix + "_green", idPrefix + ".green", defaultGreen));
		blue = register.apply(channel(idPrefix + "_blue", idPrefix + ".blue", defaultBlue));
		opacity = register.apply(new DoubleSetting(
			idPrefix + "_opacity",
			"client.setting.drop_point." + idPrefix + ".opacity",
			defaultOpacity,
			0.0,
			1.0,
			0.05
		));
	}

	public DropPointColor color() {
		return new DropPointColor(red.value(), green.value(), blue.value(), opacity.value());
	}

	public DoubleSetting red() {
		return red;
	}

	public DoubleSetting green() {
		return green;
	}

	public DoubleSetting blue() {
		return blue;
	}

	public DoubleSetting opacity() {
		return opacity;
	}

	private static DoubleSetting channel(String id, String suffix, double defaultValue) {
		return new DoubleSetting(
			id,
			"client.setting.drop_point." + suffix,
			defaultValue,
			0.0,
			1.0,
			0.05
		);
	}
}
