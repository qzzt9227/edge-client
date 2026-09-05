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

	// ================= 1. 粒子效果 =================
	public static boolean shouldNoRenderParticle(String particleId) {
		NoRenderModule current = module;
		if (current == null || !current.isEnabled() || particleId == null) {
			return false;
		}

		if (current.particleCustomBlacklist().isBlocked(particleId)) {
			return true;
		}

		String lower = particleId.toLowerCase();

		if (current.particleExplosion().value()) {
			if (lower.contains("explosion") || lower.contains("sonic_boom") || lower.contains("flash")) {
				return true;
			}
		}

		if (current.particleEnvironment().value()) {
			if (lower.contains("ambient") || lower.contains("bubble") || lower.contains("smoke")
				|| lower.contains("ash") || lower.contains("spore") || lower.contains("mycelium")
				|| lower.contains("campfire") || lower.contains("glow") || lower.contains("cloud")) {
				return true;
			}
		}

		if (current.particleVillager().value()) {
			if (lower.contains("villager") || lower.contains("heart")) {
				return true;
			}
		}

		if (current.particleComposter().value()) {
			if (lower.contains("compost")) {
				return true;
			}
		}

		if (current.particleRain().value()) {
			if (lower.contains("rain") || lower.contains("splash") || lower.contains("water_drop")
				|| lower.contains("drip_water") || lower.contains("dripping_water")) {
				return true;
			}
		}

		if (current.particleBlockBreak().value()) {
			if (lower.contains("block") || lower.contains("dust")) {
				return true;
			}
		}

		return false;
	}

	// ================= 2. 静态实体与界面 =================
	public static boolean shouldNoRenderItemFrames() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.itemFrames().value();
	}

	public static boolean shouldNoRenderArmorStands() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.armorStands().value();
	}

	public static boolean shouldNoRenderPaintings() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.paintings().value();
	}

	public static boolean shouldNoRenderItemFrameNameTags() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.itemFrameNameTags().value();
	}

	public static boolean shouldNoRenderPlayerNameTags() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.playerNameTags().value();
	}

	public static boolean shouldNoRenderBeaconBeams() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.beaconBeams().value();
	}

	public static boolean shouldNoRenderEnchantingTableBooks() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.enchantingTableBooks().value();
	}

	public static boolean shouldNoRenderMovingPistons() {
		NoRenderModule current = module;
		return current != null && current.isEnabled()
			&& (current.movingPistons().value() || current.animationBlocks().value());
	}

	public static boolean shouldNoRenderUnderwaterLavaOverlay() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.underwaterLavaOverlay().value();
	}

	// ================= 3. 物效 =================
	public static double getGlobalFogDistance() {
		NoRenderModule current = module;
		if (current == null || !current.isEnabled()) {
			return 1.0;
		}
		return current.globalFogDistance().value();
	}

	public static boolean shouldNoRenderOverworldFog() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.fogOverworld().value();
	}

	public static boolean shouldNoRenderNetherFog() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.fogNether().value();
	}

	public static boolean shouldNoRenderEndFog() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.fogEnd().value();
	}

	public static boolean shouldNoRenderSky() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.sky().value();
	}

	public static boolean shouldNoRenderWeather() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.weather().value();
	}

	public static boolean shouldNoRenderBiomeColors() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.biomeColors().value();
	}

	public static boolean shouldNoRenderSkyColors() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.skyColors().value();
	}

	public static boolean shouldNoRenderLightUpdates() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.lightUpdates().value();
	}

	// ================= 4. 动画 =================
	public static boolean shouldNoRenderAnimationWater() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.animationWater().value();
	}

	public static boolean shouldNoRenderAnimationLava() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.animationLava().value();
	}

	public static boolean shouldNoRenderAnimationFire() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.animationFire().value();
	}

	public static boolean shouldNoRenderAnimationPortals() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.animationPortals().value();
	}

	public static boolean shouldNoRenderAnimationBlocks() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.animationBlocks().value();
	}

	public static boolean shouldNoRenderAnimationSculkSensors() {
		NoRenderModule current = module;
		return current != null && current.isEnabled() && current.animationSculkSensors().value();
	}

	/**
	 * 判断贴图精灵是否需要冻结动画帧。
	 */
	public static boolean shouldFreezeSpriteAnimation(String spriteName) {
		if (!isEnabled() || spriteName == null) {
			return false;
		}
		String lower = spriteName.toLowerCase();
		if (shouldNoRenderAnimationWater() && lower.contains("water")) {
			return true;
		}
		if (shouldNoRenderAnimationLava() && lower.contains("lava")) {
			return true;
		}
		if (shouldNoRenderAnimationFire() && (lower.contains("fire") || lower.contains("flame"))) {
			return true;
		}
		if (shouldNoRenderAnimationPortals() && lower.contains("portal")) {
			return true;
		}
		if (shouldNoRenderAnimationSculkSensors() && lower.contains("sculk_sensor")) {
			return true;
		}
		return false;
	}
}
