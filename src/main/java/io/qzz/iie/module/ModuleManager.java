package io.qzz.iie.module;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ModuleManager {
	private final Map<ModuleId, Module> byId = new LinkedHashMap<>();
	private final List<Runnable> changeListeners = new ArrayList<>();

	public <M extends Module> M register(M module) {
		Objects.requireNonNull(module, "module");
		Module previous = byId.putIfAbsent(module.id(), module);
		if (previous != null) {
			throw new IllegalArgumentException("Duplicate module ID: " + module.id());
		}
		notifyChanged();
		return module;
	}

	/**
	 * 订阅模块注册或启用状态变化。返回值用于取消订阅。
	 */
	public Runnable addChangeListener(Runnable listener) {
		Runnable checked = Objects.requireNonNull(listener, "listener");
		changeListeners.add(checked);
		return () -> changeListeners.remove(checked);
	}

	public Optional<Module> find(ModuleId id) {
		return Optional.ofNullable(byId.get(id));
	}

	public List<Module> modules() {
		return List.copyOf(byId.values());
	}

	public List<ModuleCategory> categories() {
		List<ModuleCategory> categories = new ArrayList<>();
		for (Module module : byId.values()) {
			ModuleCategory category = module.category();
			if (!categories.contains(category)) {
				categories.add(category);
			}
		}
		categories.sort(null);
		return List.copyOf(categories);
	}

	public ModuleChangeResult setEnabled(ModuleId id, boolean enabled) {
		Module module = byId.get(Objects.requireNonNull(id, "id"));
		if (module == null) {
			return new ModuleChangeResult.Failed(
				enabled,
				new IllegalArgumentException("Unknown module ID: " + id)
			);
		}
		if (!module.metadata().toggleable()) {
			return new ModuleChangeResult.Failed(
				enabled,
				new IllegalStateException("Module does not have an enabled state: " + id)
			);
		}
		ModuleChangeResult result = module.setEnabled(enabled);
		if (result instanceof ModuleChangeResult.Changed) {
			notifyChanged();
		}
		return result;
	}

	public void tickEnabledModules() {
		for (Module module : byId.values()) {
			module.clientTick();
			if (module.consumeDisableRequest()) {
				setEnabled(module.id(), false);
			}
		}
	}

	private void notifyChanged() {
		for (Runnable listener : List.copyOf(changeListeners)) {
			listener.run();
		}
	}
}
