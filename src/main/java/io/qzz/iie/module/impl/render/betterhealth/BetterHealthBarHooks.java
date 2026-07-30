package io.qzz.iie.module.impl.render.betterhealth;

import java.util.Objects;

/**
 * 将版本敏感的 Minecraft 注入点限制在一个极小的桥接面上。
 */
public final class BetterHealthBarHooks {
	private static volatile BetterHealthBarModule module;

	private BetterHealthBarHooks() {
	}

	public static void install(BetterHealthBarModule installedModule) {
		module = Objects.requireNonNull(installedModule, "installedModule");
	}

	public static float clampMaximumHealth(float originalMaximumHealth) {
		BetterHealthBarModule current = module;
		if (current == null) {
			return originalMaximumHealth;
		}
		return BetterHealthBarPolicy.visibleMaximumHealth(
			current.isEnabled(),
			current.thresholdRows().value(),
			originalMaximumHealth
		);
	}
}
