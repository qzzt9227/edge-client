package io.qzz.iie.setting;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

public abstract class Setting<T> {
	private final String id;
	private final String translationKey;
	private final T defaultValue;
	private final List<Runnable> changeListeners = new ArrayList<>();
	private java.util.function.BooleanSupplier visiblePredicate = () -> true;
	private int indent;
	private T value;

	protected Setting(String id, String translationKey, T defaultValue) {
		this.id = validateId(id);
		this.translationKey = Objects.requireNonNull(translationKey, "translationKey");
		this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
		this.value = defaultValue;
	}

	public final String id() {
		return id;
	}

	public final String translationKey() {
		return translationKey;
	}

	public final T defaultValue() {
		return defaultValue;
	}

	public final T value() {
		return value;
	}

	public final void set(T requestedValue) {
		T normalized = Objects.requireNonNull(normalize(requestedValue), "normalized value");
		if (value.equals(normalized)) {
			return;
		}
		value = normalized;
		for (Runnable listener : List.copyOf(changeListeners)) {
			listener.run();
		}
	}

	public final void reset() {
		set(defaultValue);
	}

	/**
	 * 订阅规范化后的值变化。返回值用于取消订阅。
	 */
	public final Runnable addChangeListener(Runnable listener) {
		Runnable checked = Objects.requireNonNull(listener, "listener");
		changeListeners.add(checked);
		return () -> changeListeners.remove(checked);
	}

	@SuppressWarnings("unchecked")
	public final <S extends Setting<T>> S visibleWhen(java.util.function.BooleanSupplier condition) {
		this.visiblePredicate = Objects.requireNonNull(condition, "condition");
		return (S) this;
	}

	public final boolean isVisible() {
		return visiblePredicate.getAsBoolean();
	}

	public final int indent() {
		return indent;
	}

	@SuppressWarnings("unchecked")
	public final <S extends Setting<T>> S indent(int indent) {
		if (indent < 0) {
			throw new IllegalArgumentException("indent must be non-negative: " + indent);
		}
		this.indent = indent;
		return (S) this;
	}

	protected abstract T normalize(T requestedValue);

	private static String validateId(String id) {
		Objects.requireNonNull(id, "id");
		if (!id.matches("[a-z0-9][a-z0-9_.-]*")) {
			throw new IllegalArgumentException("Invalid setting ID: " + id);
		}
		return id;
	}
}
