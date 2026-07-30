package io.qzz.iie.setting;

public final class KeybindSetting extends Setting<KeybindValue> {
	public KeybindSetting(String id, String translationKey) {
		this(id, translationKey, KeybindValue.unbound());
	}

	public KeybindSetting(String id, String translationKey, KeybindValue defaultValue) {
		super(id, translationKey, defaultValue);
	}

	public void bind(int keyCode) {
		set(new KeybindValue(keyCode));
	}

	public void clear() {
		set(KeybindValue.unbound());
	}

	@Override
	protected KeybindValue normalize(KeybindValue requestedValue) {
		if (requestedValue.keyCode() < KeybindValue.UNBOUND_KEY) {
			throw new IllegalArgumentException(
				"Key code must be unbound (-1) or a non-negative GLFW key code"
			);
		}
		return requestedValue;
	}
}
