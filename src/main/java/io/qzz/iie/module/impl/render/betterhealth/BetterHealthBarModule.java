package io.qzz.iie.module.impl.render.betterhealth;

import io.qzz.iie.api.hud.HudPosition;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.DoubleSetting;

import java.util.ArrayList;
import java.util.List;

/**
 * 声明更好的血条功能及设置；具体 HUD 绘制和拖拽由通用 API 处理。
 */
public final class BetterHealthBarModule extends Module {
	private final ChoiceSetting<Integer> thresholdRows = setting(
		new ChoiceSetting<>(
			"threshold_rows",
			"client.setting.better_health_bar.threshold_rows",
			1,
			thresholdOptions()
		)
	);
	private final HudPositionSetting numberPosition = setting(
		new HudPositionSetting(
			"number_position",
			"client.setting.better_health_bar.number_position",
			new HudPosition(0.4978768577494692, 0.5325004840271056)
		)
	);
	private final DoubleSetting numberScale = setting(
		new DoubleSetting(
			"number_scale",
			"client.setting.better_health_bar.number_scale",
			1.2,
			0.5,
			3.0,
			0.05
		)
	);

	public BetterHealthBarModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "better_health_bar"),
			"client.module.better_health_bar.name",
			"client.module.better_health_bar.description",
			200
		));
	}

	public ChoiceSetting<Integer> thresholdRows() {
		return thresholdRows;
	}

	public HudPositionSetting numberPosition() {
		return numberPosition;
	}

	public DoubleSetting numberScale() {
		return numberScale;
	}

	private static List<ChoiceOption<Integer>> thresholdOptions() {
		List<ChoiceOption<Integer>> options = new ArrayList<>();
		options.add(new ChoiceOption<>(
			"always",
			"client.option.better_health_bar.threshold.always",
			-1
		));
		for (int rows = 1; rows <= 10; rows++) {
			options.add(new ChoiceOption<>(
				"rows_" + rows,
				"client.option.better_health_bar.threshold.rows_" + rows,
				rows
			));
		}
		return List.copyOf(options);
	}
}
