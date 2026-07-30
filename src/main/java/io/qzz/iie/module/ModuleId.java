package io.qzz.iie.module;

import java.util.Objects;

public record ModuleId(String namespace, String path) implements Comparable<ModuleId> {
	public ModuleId {
		namespace = validate(namespace, "namespace");
		path = validate(path, "path");
	}

	public static ModuleId of(String namespace, String path) {
		return new ModuleId(namespace, path);
	}

	@Override
	public int compareTo(ModuleId other) {
		return toString().compareTo(other.toString());
	}

	@Override
	public String toString() {
		return namespace + ":" + path;
	}

	private static String validate(String value, String part) {
		Objects.requireNonNull(value, part);
		if (!value.matches("[a-z0-9][a-z0-9_.-]*")) {
			throw new IllegalArgumentException("Invalid module ID " + part + ": " + value);
		}
		return value;
	}
}
