package io.qzz.iie.ui.screen;

import io.qzz.iie.api.hud.HudPositionEditorApi;
import io.qzz.iie.api.setting.SettingEditorApi;
import io.qzz.iie.module.ModuleManager;

import net.minecraft.client.gui.screens.Screen;

import java.util.Objects;
import java.util.function.DoubleSupplier;

public final class ClickGuiScreenFactory {
	private final ModuleManager moduleManager;
	private final String version;
	private final DoubleSupplier textScale;
	private final HudPositionEditorApi hudPositions;
	private final SettingEditorApi settingEditors;

	public ClickGuiScreenFactory(ModuleManager moduleManager, String version) {
		this(
			moduleManager,
			version,
			() -> 1.0,
			HudPositionEditorApi.noop(),
			SettingEditorApi.noop()
		);
	}

	public ClickGuiScreenFactory(
		ModuleManager moduleManager,
		String version,
		DoubleSupplier textScale
	) {
		this(
			moduleManager,
			version,
			textScale,
			HudPositionEditorApi.noop(),
			SettingEditorApi.noop()
		);
	}

	public ClickGuiScreenFactory(
		ModuleManager moduleManager,
		String version,
		DoubleSupplier textScale,
		HudPositionEditorApi hudPositions
	) {
		this(
			moduleManager,
			version,
			textScale,
			hudPositions,
			SettingEditorApi.noop()
		);
	}

	public ClickGuiScreenFactory(
		ModuleManager moduleManager,
		String version,
		DoubleSupplier textScale,
		HudPositionEditorApi hudPositions,
		SettingEditorApi settingEditors
	) {
		this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager");
		this.version = Objects.requireNonNull(version, "version");
		this.textScale = Objects.requireNonNull(textScale, "textScale");
		this.hudPositions = Objects.requireNonNull(hudPositions, "hudPositions");
		this.settingEditors = Objects.requireNonNull(settingEditors, "settingEditors");
	}

	public Screen create(Screen parent) {
		return new ClickGuiScreen(
			moduleManager,
			version,
			parent,
			textScale,
			hudPositions,
			settingEditors
		);
	}
}
