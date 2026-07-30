package io.qzz.iie.module;

import io.qzz.iie.setting.KeybindSetting;
import io.qzz.iie.setting.KeybindValue;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntPredicate;

/**
 * Converts keyboard press edges into module state changes.
 */
public final class ModuleShortcutDispatcher {
	private final ModuleManager moduleManager;
	private Set<Integer> pressedKeys = Set.of();

	public ModuleShortcutDispatcher(ModuleManager moduleManager) {
		this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager");
	}

	/**
	 * Samples all declared module shortcuts.
	 *
	 * @param gameplayActive whether no screen or chat is open
	 * @param isKeyDown supplies the current physical state for a GLFW key code
	 */
	public void update(boolean gameplayActive, IntPredicate isKeyDown) {
		Objects.requireNonNull(isKeyDown, "isKeyDown");
		Set<Integer> currentlyPressed = new HashSet<>();

		for (Module module : moduleManager.modules()) {
			KeybindSetting setting = module.keybind().orElse(null);
			if (setting == null) {
				continue;
			}
			KeybindValue keybind = setting.value();
			if (!keybind.isBound() || !isKeyDown.test(keybind.keyCode())) {
				continue;
			}

			currentlyPressed.add(keybind.keyCode());
			if (gameplayActive && !pressedKeys.contains(keybind.keyCode())) {
				moduleManager.setEnabled(module.id(), !module.isEnabled());
			}
		}

		pressedKeys = Set.copyOf(currentlyPressed);
	}
}
