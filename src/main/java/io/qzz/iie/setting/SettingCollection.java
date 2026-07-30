package io.qzz.iie.setting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SettingCollection {
	private final Map<String, Setting<?>> byId = new LinkedHashMap<>();

	public <S extends Setting<?>> S register(S setting) {
		Objects.requireNonNull(setting, "setting");
		Setting<?> previous = byId.putIfAbsent(setting.id(), setting);
		if (previous != null) {
			throw new IllegalArgumentException("Duplicate setting ID: " + setting.id());
		}
		return setting;
	}

	public List<Setting<?>> values() {
		return List.copyOf(byId.values());
	}

	public boolean isEmpty() {
		return byId.isEmpty();
	}
}
