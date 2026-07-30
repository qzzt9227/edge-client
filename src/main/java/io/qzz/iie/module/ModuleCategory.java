package io.qzz.iie.module;

import java.util.Objects;

public record ModuleCategory(String id, String translationKey, int order)
	implements Comparable<ModuleCategory> {
	public ModuleCategory {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(translationKey, "translationKey");
		if (!id.matches("[a-z0-9][a-z0-9_.-]*")) {
			throw new IllegalArgumentException("Invalid category ID: " + id);
		}
	}

	@Override
	public int compareTo(ModuleCategory other) {
		int orderResult = Integer.compare(order, other.order);
		return orderResult != 0 ? orderResult : id.compareTo(other.id);
	}
}
