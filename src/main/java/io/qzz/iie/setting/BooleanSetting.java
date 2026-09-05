package io.qzz.iie.setting;

public class BooleanSetting extends Setting<Boolean> {
	public BooleanSetting(String id, String translationKey, boolean defaultValue) {
		super(id, translationKey, defaultValue);
	}

	public void toggle() {
		set(!value());
	}

	@Override
	protected Boolean normalize(Boolean requestedValue) {
		return requestedValue;
	}
}
