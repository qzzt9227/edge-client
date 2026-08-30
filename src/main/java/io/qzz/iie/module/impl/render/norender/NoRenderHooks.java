package io.qzz.iie.module.impl.render.norender;

import java.util.Objects;

/**
 * 桥接 {@link NoRenderModule} 与 Minecraft 渲染管线 Mixin 的极简钩子类。
 */
public final class NoRenderHooks {
	private static volatile NoRenderModule module;

	private NoRenderHooks() {
	}

	public static void install(NoRenderModule installedModule) {
		module = Objects.requireNonNull(installedModule, "installedModule");
	}

	public static boolean isEnabled() {
		NoRenderModule current = module;
		return current != null && current.isEnabled();
	}

	public static boolean shouldNoRenderParticles() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.particles().value();
	}

	public static boolean shouldNoRenderSignText() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.signText().value();
	}

	public static boolean shouldNoRenderMaps() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.maps().value();
	}

	public static boolean shouldNoRenderBannerPatterns() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.bannerPatterns().value();
	}

	public static boolean shouldNoRenderFire() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.fire().value();
	}

	public static boolean shouldNoRenderDarkness() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.darkness().value();
	}

	public static boolean shouldNoRenderBlindness() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.blindness().value();
	}
}
