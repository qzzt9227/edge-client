package io.qzz.iie.module;

import io.qzz.iie.setting.Setting;
import io.qzz.iie.setting.SettingCollection;
import io.qzz.iie.setting.KeybindSetting;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class Module {
	private final ModuleMetadata metadata;
	private final SettingCollection settings = new SettingCollection();
	private KeybindSetting moduleKeybind;
	private boolean enabled;
	private boolean disableRequested;

	protected Module(ModuleMetadata metadata) {
		this.metadata = Objects.requireNonNull(metadata, "metadata");
	}

	public final ModuleMetadata metadata() {
		return metadata;
	}

	public final ModuleId id() {
		return metadata.id();
	}

	public final boolean isEnabled() {
		return enabled;
	}

	public final List<Setting<?>> settings() {
		return settings.values();
	}

	public final boolean hasSettings() {
		return !settings.isEmpty();
	}

	public final Optional<KeybindSetting> keybind() {
		return Optional.ofNullable(moduleKeybind);
	}

	protected final <S extends Setting<?>> S setting(S setting) {
		return settings.register(setting);
	}

	protected final KeybindSetting keybind(KeybindSetting keybind) {
		Objects.requireNonNull(keybind, "keybind");
		if (moduleKeybind != null) {
			throw new IllegalStateException("A module may declare only one shortcut");
		}
		moduleKeybind = setting(keybind);
		return moduleKeybind;
	}

	final ModuleChangeResult setEnabled(boolean requestedState) {
		if (enabled == requestedState) {
			return new ModuleChangeResult.Unchanged(enabled);
		}

		boolean previousState = enabled;
		enabled = requestedState;
		disableRequested = false;
		try {
			if (requestedState) {
				onEnable();
			} else {
				onDisable();
			}
			return new ModuleChangeResult.Changed(enabled);
		} catch (Throwable cause) {
			enabled = previousState;
			return new ModuleChangeResult.Failed(requestedState, cause);
		}
	}

	final void clientTick() {
		if (enabled) {
			onClientTick();
		}
	}

	final boolean consumeDisableRequest() {
		boolean requested = enabled && disableRequested;
		disableRequested = false;
		return requested;
	}

	/**
	 * 请求模块管理器在当前 tick 回调结束后安全关闭此模块。
	 */
	protected final void requestDisable() {
		if (enabled) {
			disableRequested = true;
		}
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}

	/**
	 * Runs at the end of each client tick while this module is enabled.
	 */
	protected void onClientTick() {
	}
}
