package io.qzz.iie.module;

public sealed interface ModuleChangeResult {
	record Changed(boolean enabled) implements ModuleChangeResult {
	}

	record Unchanged(boolean enabled) implements ModuleChangeResult {
	}

	record Failed(boolean requestedState, Throwable cause) implements ModuleChangeResult {
	}
}
