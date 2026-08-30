package io.qzz.iie.module.impl.player.packetmine;

import io.qzz.iie.setting.ChoiceOption;
import java.util.List;

/**
 * 包挖掘的方块进度渲染样式。
 */
public enum PacketMineRenderStyle {
	EXPAND("expand", "client.option.packet_mine.expand"),
	RISE("rise", "client.option.packet_mine.rise"),
	PULSE_FRAME("pulse_frame", "client.option.packet_mine.pulse_frame");

	private final String id;
	private final String translationKey;

	PacketMineRenderStyle(String id, String translationKey) {
		this.id = id;
		this.translationKey = translationKey;
	}

	public String id() {
		return id;
	}

	public String translationKey() {
		return translationKey;
	}

	public static List<ChoiceOption<PacketMineRenderStyle>> options() {
		return List.of(
			new ChoiceOption<>(EXPAND.id, EXPAND.translationKey, EXPAND),
			new ChoiceOption<>(RISE.id, RISE.translationKey, RISE),
			new ChoiceOption<>(PULSE_FRAME.id, PULSE_FRAME.translationKey, PULSE_FRAME)
		);
	}
}
