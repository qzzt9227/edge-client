package io.qzz.iie.setting;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ChoiceSetting<T> extends Setting<T> {
	private final List<ChoiceOption<T>> options;

	public ChoiceSetting(
		String id,
		String translationKey,
		T defaultValue,
		List<ChoiceOption<T>> options
	) {
		super(id, translationKey, defaultValue);
		this.options = validateOptions(options);
		normalize(defaultValue);
	}

	public List<ChoiceOption<T>> options() {
		return options;
	}

	public ChoiceOption<T> selectedOption() {
		return optionFor(value());
	}

	public void selectOption(int index) {
		set(options.get(index).value());
	}

	/**
	 * 按稳定选项 ID 选择值，供配置、命令等不依赖枚举实现的 API 使用。
	 */
	public void selectOptionId(String optionId) {
		Objects.requireNonNull(optionId, "optionId");
		for (ChoiceOption<T> option : options) {
			if (option.id().equals(optionId)) {
				set(option.value());
				return;
			}
		}
		throw new IllegalArgumentException("Unknown choice option ID: " + optionId);
	}

	@Override
	protected T normalize(T requestedValue) {
		return optionFor(requestedValue).value();
	}

	private ChoiceOption<T> optionFor(T value) {
		for (ChoiceOption<T> option : options) {
			if (option.value().equals(value)) {
				return option;
			}
		}
		throw new IllegalArgumentException("Value is not a registered choice: " + value);
	}

	private static <T> List<ChoiceOption<T>> validateOptions(
		List<ChoiceOption<T>> requestedOptions
	) {
		Objects.requireNonNull(requestedOptions, "options");
		List<ChoiceOption<T>> options = List.copyOf(requestedOptions);
		if (options.isEmpty()) {
			throw new IllegalArgumentException("Choice setting requires at least one option");
		}

		Set<String> ids = new HashSet<>();
		Set<T> values = new HashSet<>();
		for (ChoiceOption<T> option : options) {
			if (!ids.add(option.id())) {
				throw new IllegalArgumentException("Duplicate choice option ID: " + option.id());
			}
			if (!values.add(option.value())) {
				throw new IllegalArgumentException("Duplicate choice option value: " + option.value());
			}
		}
		return options;
	}
}
