package io.qzz.iie.module.impl.render.droppoint;

import io.qzz.iie.api.message.MessageBoxApi;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.DoubleSetting;

import java.util.Objects;

public final class DropPointModule extends Module {
	private final MessageBoxApi messages;
	private final DropPointColorSettings defaultColor;
	private final DropPointColorSettings dangerColor;
	private final DropPointColorSettings safeColor;
	private final DropPointColorSettings scaffoldSneakingColor;
	private final DropPointColorSettings scaffoldNeedsSneakingColor;
	private final DoubleSetting scaffoldHintCount;
	private DropPointRenderState renderState;

	public DropPointModule(MessageBoxApi messages) {
		super(new ModuleMetadata(
			ModuleId.of("client", "drop_point"),
			"client.module.drop_point.name",
			"client.module.drop_point.description",
			220
		));
		this.messages = Objects.requireNonNull(messages, "messages");
		defaultColor = colors("default", 0.2, 0.75, 1.0, 0.35);
		dangerColor = colors("danger", 1.0, 0.1, 0.08, 0.45);
		safeColor = colors("safe", 0.1, 1.0, 0.15, 0.4);
		scaffoldSneakingColor = colors("scaffold_sneaking", 0.1, 1.0, 0.15, 0.4);
		scaffoldNeedsSneakingColor = colors("scaffold_needs_sneaking", 1.0, 0.75, 0.05, 0.45);
		scaffoldHintCount = setting(new DoubleSetting(
			"scaffold_hint_count",
			"client.setting.drop_point.scaffold_hint_count",
			2.0,
			1.0,
			8.0,
			1.0
		));
	}

	public MessageBoxApi messages() {
		return messages;
	}

	public DropPointColor defaultColor() {
		return defaultColor.color();
	}

	public DropPointColor dangerColor() {
		return dangerColor.color();
	}

	public DropPointColor safeColor() {
		return safeColor.color();
	}

	public DropPointColor scaffoldSneakingColor() {
		return scaffoldSneakingColor.color();
	}

	public DropPointColor scaffoldNeedsSneakingColor() {
		return scaffoldNeedsSneakingColor.color();
	}

	public DoubleSetting scaffoldHintCount() {
		return scaffoldHintCount;
	}

	public DropPointRenderState renderState() {
		return renderState;
	}

	void setRenderState(DropPointRenderState renderState) {
		this.renderState = renderState;
	}

	void clearRenderState() {
		this.renderState = null;
	}

	private DropPointColorSettings colors(
		String idPrefix,
		double red,
		double green,
		double blue,
		double opacity
	) {
		return new DropPointColorSettings(this::setting, idPrefix, red, green, blue, opacity);
	}
}
