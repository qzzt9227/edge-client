package io.qzz.iie.ui.message;

import net.minecraft.network.chat.Component;

import java.util.Objects;

public record MessageBoxSnapshot(Component message, double visibility) {
	public MessageBoxSnapshot {
		Objects.requireNonNull(message, "message");
		if (!Double.isFinite(visibility) || visibility < 0.0 || visibility > 1.0) {
			throw new IllegalArgumentException("visibility must be between 0 and 1");
		}
	}
}
