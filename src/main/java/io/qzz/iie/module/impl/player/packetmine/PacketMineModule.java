package io.qzz.iie.module.impl.player.packetmine;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;

/**
 * 包挖掘模块：向服务端发送静默平滑转头与方块挖掘发包，支持多样式世界进度渲染、严格距离与遮挡判定、以及鬼手最优工具静默切换。
 */
public final class PacketMineModule extends Module {
	private final DoubleSetting delayTicks = setting(new DoubleSetting(
		"delay_ticks",
		"client.setting.packet_mine.delay_ticks",
		1.0,
		0.0,
		10.0,
		1.0
	));

	private final DoubleSetting range = setting(new DoubleSetting(
		"range",
		"client.setting.packet_mine.range",
		3.5,
		1.0,
		8.0,
		0.5
	));

	private final DoubleSetting rotationTicks = setting(new DoubleSetting(
		"rotation_ticks",
		"client.setting.packet_mine.rotation_ticks",
		2.0,
		0.0,
		10.0,
		1.0
	));

	private final DoubleSetting resetRotationTicks = setting(new DoubleSetting(
		"reset_rotation_ticks",
		"client.setting.packet_mine.reset_rotation_ticks",
		4.0,
		0.0,
		10.0,
		1.0
	));

	private final BooleanSetting ghostHand = setting(new BooleanSetting(
		"ghost_hand",
		"client.setting.packet_mine.ghost_hand",
		false
	));

	private final ChoiceSetting<PacketMineRenderStyle> renderStyle = setting(new ChoiceSetting<>(
		"render_style",
		"client.setting.packet_mine.render_style",
		PacketMineRenderStyle.EXPAND,
		PacketMineRenderStyle.options()
	));

	private final DoubleSetting fillOpacity = setting(new DoubleSetting(
		"fill_opacity",
		"client.setting.packet_mine.fill_opacity",
		0.4,
		0.0,
		1.0,
		0.05
	));

	private final DoubleSetting lineOpacity = setting(new DoubleSetting(
		"line_opacity",
		"client.setting.packet_mine.line_opacity",
		1.00,
		0.0,
		1.0,
		0.05
	));

	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public PacketMineModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "packet_mine"),
			"client.module.packet_mine.name",
			"client.module.packet_mine.description",
			80,
			true
		));
	}

	public DoubleSetting delayTicks() {
		return delayTicks;
	}

	public DoubleSetting range() {
		return range;
	}

	public DoubleSetting rotationTicks() {
		return rotationTicks;
	}

	public DoubleSetting resetRotationTicks() {
		return resetRotationTicks;
	}

	public BooleanSetting ghostHand() {
		return ghostHand;
	}

	public ChoiceSetting<PacketMineRenderStyle> renderStyle() {
		return renderStyle;
	}

	public DoubleSetting fillOpacity() {
		return fillOpacity;
	}

	public DoubleSetting lineOpacity() {
		return lineOpacity;
	}

	public int fillColor() {
		int alpha = (int) Math.round(fillOpacity.value() * 255.0);
		return (alpha << 24) | 0x00E5FF;
	}

	public int lineColor() {
		int alpha = (int) Math.round(lineOpacity.value() * 255.0);
		return (alpha << 24) | 0x00E5FF;
	}

	@Override
	protected void onDisable() {
		PacketMineHooks.abort();
	}
}
