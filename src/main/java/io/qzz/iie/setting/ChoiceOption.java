package io.qzz.iie.setting;

import java.util.Objects;

public record ChoiceOption<T>(String id, String translationKey, T value) {
	public ChoiceOption {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(translationKey, "translationKey");
		Objects.requireNonNull(value, "value");
		if (!id.matches("[a-z0-9][a-z0-9_.-]*")) {
			throw new IllegalArgumentException("Invalid choice option ID: " + id);
		}
	}
}
