package io.qzz.iie.ui.binding;

public interface ValueBinding<T> {
	T get();

	BindingUpdateResult set(T value);

	default boolean isEnabled() {
		return true;
	}
}
