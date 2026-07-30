package io.qzz.iie.ui.factory;

import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.EditorSetting;
import io.qzz.iie.setting.KeybindSetting;
import io.qzz.iie.setting.Setting;
import io.qzz.iie.ui.binding.ModuleEnabledBinding;
import io.qzz.iie.ui.component.control.SliderControl;
import io.qzz.iie.ui.component.control.ChoiceControl;
import io.qzz.iie.ui.component.control.KeybindControl;
import io.qzz.iie.ui.component.control.HudPositionControl;
import io.qzz.iie.ui.component.control.EditorSettingControl;
import io.qzz.iie.ui.component.control.ToggleControl;
import io.qzz.iie.ui.component.control.UnsupportedControl;
import io.qzz.iie.ui.input.UiInputTarget;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SettingControlFactory {
	private final Consumer<HudPositionSetting> positionEditor;
	private final Consumer<EditorSetting<?>> settingEditor;
	private final Predicate<EditorSetting<?>> settingEditorSupport;

	public SettingControlFactory() {
		this(setting -> {}, setting -> false, setting -> {});
	}

	public SettingControlFactory(Consumer<HudPositionSetting> positionEditor) {
		this(positionEditor, setting -> false, setting -> {});
	}

	public SettingControlFactory(
		Consumer<HudPositionSetting> positionEditor,
		Consumer<EditorSetting<?>> settingEditor
	) {
		this(positionEditor, setting -> true, settingEditor);
	}

	public SettingControlFactory(
		Consumer<HudPositionSetting> positionEditor,
		Predicate<EditorSetting<?>> settingEditorSupport,
		Consumer<EditorSetting<?>> settingEditor
	) {
		this.positionEditor = Objects.requireNonNull(positionEditor, "positionEditor");
		this.settingEditorSupport =
			Objects.requireNonNull(settingEditorSupport, "settingEditorSupport");
		this.settingEditor = Objects.requireNonNull(settingEditor, "settingEditor");
	}

	public ToggleControl createModuleEnabled(ModuleManager moduleManager, ModuleId moduleId) {
		return new ToggleControl(new ModuleEnabledBinding(
			Objects.requireNonNull(moduleManager, "moduleManager"),
			Objects.requireNonNull(moduleId, "moduleId")
		));
	}

	public UiInputTarget create(Setting<?> setting) {
		Objects.requireNonNull(setting, "setting");
		if (setting instanceof BooleanSetting booleanSetting) {
			return new ToggleControl(booleanSetting);
		}
		if (setting instanceof DoubleSetting doubleSetting) {
			return new SliderControl(doubleSetting);
		}
		if (setting instanceof ChoiceSetting<?> choiceSetting) {
			return new ChoiceControl(choiceSetting);
		}
		if (setting instanceof KeybindSetting keybindSetting) {
			return new KeybindControl(keybindSetting);
		}
		if (setting instanceof HudPositionSetting positionSetting) {
			return new HudPositionControl(
				positionSetting,
				() -> positionEditor.accept(positionSetting)
			);
		}
		if (setting instanceof EditorSetting<?> editorSetting) {
			EditorSettingControl control = new EditorSettingControl(
				editorSetting,
				() -> settingEditor.accept(editorSetting)
			);
			control.setEnabled(settingEditorSupport.test(editorSetting));
			return control;
		}
		return new UnsupportedControl();
	}
}
