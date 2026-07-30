package io.qzz.iie.ui.binding;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleChangeResult;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleManager;

import java.util.Objects;

public final class ModuleEnabledBinding implements ValueBinding<Boolean> {
	private final ModuleManager moduleManager;
	private final ModuleId moduleId;

	public ModuleEnabledBinding(ModuleManager moduleManager, ModuleId moduleId) {
		this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager");
		this.moduleId = Objects.requireNonNull(moduleId, "moduleId");
	}

	@Override
	public Boolean get() {
		return moduleManager.find(moduleId).map(Module::isEnabled).orElse(false);
	}

	@Override
	public BindingUpdateResult set(Boolean value) {
		ModuleChangeResult result = moduleManager.setEnabled(moduleId, value);
		if (result instanceof ModuleChangeResult.Failed failed) {
			return new BindingUpdateResult.Rejected(failed.cause());
		}
		return new BindingUpdateResult.Accepted();
	}

	@Override
	public boolean isEnabled() {
		return moduleManager.find(moduleId).isPresent();
	}
}
