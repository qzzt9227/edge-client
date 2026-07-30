package io.qzz.iie.module;

import java.util.Objects;

public record ModuleMetadata(
	ModuleId id,
	String nameTranslationKey,
	String descriptionTranslationKey,
	ModuleCategory category,
	int order,
	boolean toggleable
) {
	public ModuleMetadata(
		ModuleId id,
		String nameTranslationKey,
		String descriptionTranslationKey,
		ModuleCategory category,
		int order
	) {
		this(id, nameTranslationKey, descriptionTranslationKey, category, order, true);
	}

	public ModuleMetadata {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(nameTranslationKey, "nameTranslationKey");
		Objects.requireNonNull(descriptionTranslationKey, "descriptionTranslationKey");
		Objects.requireNonNull(category, "category");
	}
}
