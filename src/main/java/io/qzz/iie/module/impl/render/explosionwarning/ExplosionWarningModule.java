package io.qzz.iie.module.impl.render.explosionwarning;

import io.qzz.iie.api.message.MessageBoxApi;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ColorSetting;
import io.qzz.iie.setting.DoubleSetting;

import java.util.Objects;
import java.util.List;

public final class ExplosionWarningModule extends Module {
	private final MessageBoxApi messages;
	private final DoubleSetting radius = setting(new DoubleSetting(
		"radius",
		"client.setting.explosion_warning.radius",
		6.0,
		1.0,
		10.0,
		0.5
	));
	private final BooleanSetting impendingMessage = setting(new BooleanSetting(
		"impending_message",
		"client.setting.explosion_warning.impending_message",
		true
	));
	private final BooleanSetting creeperRangeMessage = setting(new BooleanSetting(
		"creeper_range_message",
		"client.setting.explosion_warning.creeper_range_message",
		false
	));
	private final DoubleSetting countdownOffsetX = setting(countdownOffset("x"));
	private final DoubleSetting countdownOffsetY = setting(countdownOffset("y"));
	private final DoubleSetting countdownOffsetZ = setting(countdownOffset("z"));
	private final ColorSetting messageColor = setting(new ColorSetting(
		"message_color",
		"client.setting.explosion_warning.message_color",
		0xFF0000,
		false
	));
	private List<ExplosionWarningRenderState> renderStates = List.of();

	public ExplosionWarningModule(MessageBoxApi messages) {
		super(new ModuleMetadata(
			ModuleId.of("client", "explosion_warning"),
			"client.module.explosion_warning.name",
			"client.module.explosion_warning.description",
			230
		));
		this.messages = Objects.requireNonNull(messages, "messages");
	}

	public MessageBoxApi messages() {
		return messages;
	}

	public DoubleSetting radius() {
		return radius;
	}

	public BooleanSetting impendingMessage() {
		return impendingMessage;
	}

	public BooleanSetting creeperRangeMessage() {
		return creeperRangeMessage;
	}

	public DoubleSetting countdownOffsetX() {
		return countdownOffsetX;
	}

	public DoubleSetting countdownOffsetY() {
		return countdownOffsetY;
	}

	public DoubleSetting countdownOffsetZ() {
		return countdownOffsetZ;
	}

	public int messageColor() {
		return 0xFF000000 | messageColor.rgb();
	}

	public ColorSetting messageColorSetting() {
		return messageColor;
	}

	public List<ExplosionWarningRenderState> renderStates() {
		return renderStates;
	}

	void setRenderStates(List<ExplosionWarningRenderState> renderStates) {
		this.renderStates = List.copyOf(Objects.requireNonNull(renderStates, "renderStates"));
	}

	@Override
	protected void onDisable() {
		renderStates = List.of();
	}

	private static DoubleSetting countdownOffset(String axis) {
		return new DoubleSetting(
			"countdown_offset_" + axis,
			"client.setting.explosion_warning.countdown_offset_" + axis,
			0.0,
			-2.0,
			2.0,
			0.05
		);
	}
}
