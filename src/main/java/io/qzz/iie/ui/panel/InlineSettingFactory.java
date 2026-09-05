package io.qzz.iie.ui.panel;

import io.qzz.iie.api.hud.HudPositionEditorApi;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.api.setting.SettingEditorApi;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.ColorSetting;
import io.qzz.iie.setting.DoubleRangeSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.EditorSetting;
import io.qzz.iie.setting.FoldSetting;
import io.qzz.iie.setting.KeybindSetting;
import io.qzz.iie.setting.Setting;
import net.minecraft.client.gui.screens.Screen;

import java.util.Objects;
import java.util.function.Supplier;

public final class InlineSettingFactory {
	private final HudPositionEditorApi hudPositions;
	private final SettingEditorApi settingEditors;
	private final Supplier<Screen> parentScreenSupplier;

	public InlineSettingFactory(
		HudPositionEditorApi hudPositions,
		SettingEditorApi settingEditors,
		Supplier<Screen> parentScreenSupplier
	) {
		this.hudPositions = Objects.requireNonNull(hudPositions, "hudPositions");
		this.settingEditors = Objects.requireNonNull(settingEditors, "settingEditors");
		this.parentScreenSupplier = Objects.requireNonNull(parentScreenSupplier, "parentScreenSupplier");
	}

	public InlineSettingItem create(Setting<?> setting) {
		if (setting instanceof FoldSetting fold) {
			return new FoldSettingItem(fold);
		}
		if (setting instanceof BooleanSetting bool) {
			return new BooleanSettingItem(bool);
		}
		if (setting instanceof DoubleSetting num) {
			return new DoubleSettingItem(num);
		}
		if (setting instanceof DoubleRangeSetting range) {
			return new DoubleRangeSettingItem(range);
		}
		if (setting instanceof ChoiceSetting<?> choice) {
			return new ChoiceSettingItem<>(choice);
		}
		if (setting instanceof ColorSetting color) {
			return new ColorSettingItem(color);
		}
		if (setting instanceof KeybindSetting bind) {
			return new KeybindSettingItem(bind);
		}
		if (setting instanceof HudPositionSetting hudPos) {
			return new HudPositionSettingItem(hudPos, s -> hudPositions.open(s, parentScreenSupplier.get()));
		}
		if (setting instanceof EditorSetting<?> editor && settingEditors.supports(editor)) {
			return new EditorSettingItem(editor, s -> settingEditors.open(s, parentScreenSupplier.get()));
		}
		return new UnsupportedSettingItem(setting);
	}
}
