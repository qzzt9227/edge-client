package io.qzz.iie.module.impl.player.autolibrarian;

import io.qzz.iie.api.message.MessageBoxApi;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;

import java.util.Objects;

/**
 * 自动图书管理员的声明式模块入口。
 *
 * <p>GUI 只枚举这些设置；本类不依赖任何屏幕、控件或输入事件。</p>
 */
public final class AutoLibrarianModule extends Module {
	private final MessageBoxApi messages;
	private AutoLibrarianController controller;
	private boolean waitingForScreenClose;
	private final EnchantmentTargetsSetting targets = setting(
		new EnchantmentTargetsSetting(
			"targets",
			"client.setting.auto_librarian.targets"
		)
	);
	private final DoubleSetting searchRadius = setting(number(
		"search_radius",
		3,
		1,
		16
	));
	private final DoubleSetting placementRadius = setting(number(
		"placement_radius",
		2,
		1,
		8
	));
	private final BooleanSetting allowHandMining = setting(booleanSetting(
		"allow_hand_mining",
		true
	));
	private final BooleanSetting reportTrades = setting(booleanSetting(
		"report_trades",
		true
	));
	private final BooleanSetting autoRecycle = setting(booleanSetting(
		"auto_recycle",
		false
	));
	private final DoubleSetting recycleRadius = setting(number(
		"recycle_radius",
		3,
		1,
		16
	));
	private final DoubleSetting beforeRecycleTicks = setting(number(
		"before_recycle_ticks",
		20,
		10,
		60
	));
	private final DoubleSetting recycleSearchTimeoutTicks = setting(number(
		"recycle_search_timeout_ticks",
		40,
		1,
		200
	));
	private final DoubleSetting rotationTicks = setting(number(
		"rotation_ticks",
		6,
		1,
		40
	));
	private final DoubleSetting beforePlaceTicks = setting(number(
		"before_place_ticks",
		4,
		1,
		100
	));
	private final DoubleSetting beforeOpenTradeTicks = setting(number(
		"before_open_trade_ticks",
		6,
		1,
		100
	));
	private final DoubleSetting beforeBreakTicks = setting(number(
		"before_break_ticks",
		4,
		1,
		100
	));
	private final DoubleSetting afterBreakTicks = setting(number(
		"after_break_ticks",
		10,
		1,
		200
	));
	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public AutoLibrarianModule(MessageBoxApi messages) {
		super(new ModuleMetadata(
			ModuleId.of("client", "auto_librarian"),
			"client.module.auto_librarian.name",
			"client.module.auto_librarian.description",
			100
		));
		this.messages = Objects.requireNonNull(messages, "messages");
	}

	public EnchantmentTargetsSetting targets() {
		return targets;
	}

	public DoubleSetting searchRadius() {
		return searchRadius;
	}

	public DoubleSetting placementRadius() {
		return placementRadius;
	}

	public BooleanSetting allowHandMining() {
		return allowHandMining;
	}

	public BooleanSetting reportTrades() {
		return reportTrades;
	}

	public BooleanSetting autoRecycle() {
		return autoRecycle;
	}

	public DoubleSetting recycleRadius() {
		return recycleRadius;
	}

	public DoubleSetting beforeRecycleTicks() {
		return beforeRecycleTicks;
	}

	public DoubleSetting recycleSearchTimeoutTicks() {
		return recycleSearchTimeoutTicks;
	}

	public DoubleSetting rotationTicks() {
		return rotationTicks;
	}

	public DoubleSetting beforePlaceTicks() {
		return beforePlaceTicks;
	}

	public DoubleSetting beforeOpenTradeTicks() {
		return beforeOpenTradeTicks;
	}

	public DoubleSetting beforeBreakTicks() {
		return beforeBreakTicks;
	}

	public DoubleSetting afterBreakTicks() {
		return afterBreakTicks;
	}

	MessageBoxApi messages() {
		return messages;
	}

	AutoLibrarianSettings snapshot() {
		return new AutoLibrarianSettings(
			targets.value(),
			integer(searchRadius),
			integer(placementRadius),
			allowHandMining.value(),
			reportTrades.value(),
			autoRecycle.value(),
			integer(recycleRadius),
			integer(beforeRecycleTicks),
			integer(recycleSearchTimeoutTicks),
			integer(rotationTicks),
			integer(beforePlaceTicks),
			AutoLibrarianSettings.PROFESSION_CHECK_INTERVAL_TICKS,
			integer(beforeOpenTradeTicks),
			integer(beforeBreakTicks),
			integer(afterBreakTicks)
		);
	}

	public String statusTranslationKey() {
		return controller == null
			? "client.status.auto_librarian.idle"
			: controller.statusTranslationKey();
	}

	@Override
	protected void onEnable() {
		net.minecraft.client.Minecraft client =
			net.minecraft.client.Minecraft.getInstance();
		if (client.player != null
			&& client.level != null
			&& client.gameMode != null
			&& client.gui.screen() != null) {
			waitingForScreenClose = true;
			return;
		}
		if (!controller().start()) {
			throw new IllegalStateException("Auto Librarian start preconditions were not met");
		}
	}

	@Override
	protected void onClientTick() {
		if (waitingForScreenClose) {
			if (net.minecraft.client.Minecraft.getInstance().gui.screen() != null) {
				return;
			}
			waitingForScreenClose = false;
			if (!controller().start()) {
				requestDisable();
				return;
			}
		}
		controller().tick();
		if (!controller().isActive()) {
			requestDisable();
		}
	}

	@Override
	protected void onDisable() {
		waitingForScreenClose = false;
		if (controller != null && controller.isActive()) {
			controller.stopSilently();
		}
	}

	private AutoLibrarianController controller() {
		if (controller == null) {
			controller = new AutoLibrarianController(
				net.minecraft.client.Minecraft.getInstance(),
				this
			);
		}
		return controller;
	}

	private static DoubleSetting number(String id, int value, int minimum, int maximum) {
		return new DoubleSetting(
			id,
			"client.setting.auto_librarian." + id,
			value,
			minimum,
			maximum,
			1
		);
	}

	private static BooleanSetting booleanSetting(String id, boolean value) {
		return new BooleanSetting(
			id,
			"client.setting.auto_librarian." + id,
			value
		);
	}

	private static int integer(DoubleSetting setting) {
		return (int) Math.round(setting.value());
	}
}
