package io.qzz.iie.ui.binding;

import io.qzz.iie.setting.Setting;

import java.util.Objects;

public final class SettingValueBinding<T> implements ValueBinding<T> {
	private final Setting<T> setting;

	public SettingValueBinding(Setting<T> setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	@Override
	public T get() {
		return setting.value();
	}

	@Override
	public BindingUpdateResult set(T value) {
		try {
			setting.set(value);
			return new BindingUpdateResult.Accepted();
		} catch (RuntimeException cause) {
			return new BindingUpdateResult.Rejected(cause);
		}
	}
}
