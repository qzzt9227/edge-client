package io.qzz.iie.ui.setting;

import io.qzz.iie.api.setting.SettingEditorApi;
import io.qzz.iie.api.setting.SettingEditorFactory;
import io.qzz.iie.setting.EditorSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 设置编辑器注册表的 Minecraft 屏幕适配实现。
 */
public final class SettingEditorManager implements SettingEditorApi {
	private final Minecraft client;
	private final Map<String, SettingEditorFactory> factories = new LinkedHashMap<>();

	public SettingEditorManager(Minecraft client) {
		this.client = Objects.requireNonNull(client, "client");
	}

	@Override
	public void register(String editorId, SettingEditorFactory factory) {
		String checkedId = Objects.requireNonNull(editorId, "editorId");
		if (!checkedId.matches("[a-z0-9][a-z0-9_.-]*")) {
			throw new IllegalArgumentException("Invalid setting editor ID: " + checkedId);
		}
		SettingEditorFactory previous = factories.putIfAbsent(
			checkedId,
			Objects.requireNonNull(factory, "factory")
		);
		if (previous != null) {
			throw new IllegalArgumentException(
				"Duplicate setting editor ID: " + checkedId
			);
		}
	}

	@Override
	public boolean supports(EditorSetting<?> setting) {
		return factories.containsKey(
			Objects.requireNonNull(setting, "setting").editorId()
		);
	}

	@Override
	public void open(EditorSetting<?> setting, Screen parent) {
		SettingEditorFactory factory = factories.get(
			Objects.requireNonNull(setting, "setting").editorId()
		);
		if (factory == null) {
			return;
		}
		client.setScreenAndShow(factory.create(setting, parent));
	}
}
