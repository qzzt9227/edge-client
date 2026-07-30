package io.qzz.iie.module.impl.player.autolibrarian;

import java.util.Locale;

/**
 * 一条附魔、等级模式和绿宝石价格规则。
 */
public record EnchantmentTarget(
	String enchantmentId,
	int level,
	boolean anyLevel,
	int minEmeraldPrice,
	int maxEmeraldPrice
) {
	public EnchantmentTarget {
		enchantmentId = normalizeId(enchantmentId);
		level = Math.clamp(level, 1, 255);
		minEmeraldPrice = Math.clamp(minEmeraldPrice, 1, 64);
		maxEmeraldPrice = Math.clamp(maxEmeraldPrice, 1, 64);
		if (minEmeraldPrice > maxEmeraldPrice) {
			maxEmeraldPrice = minEmeraldPrice;
		}
	}

	public EnchantmentTarget withLevel(int requestedLevel) {
		return new EnchantmentTarget(
			enchantmentId,
			requestedLevel,
			anyLevel,
			minEmeraldPrice,
			maxEmeraldPrice
		);
	}

	public EnchantmentTarget withAnyLevel(boolean requestedAnyLevel) {
		return new EnchantmentTarget(
			enchantmentId,
			level,
			requestedAnyLevel,
			minEmeraldPrice,
			maxEmeraldPrice
		);
	}

	public EnchantmentTarget withPrices(int requestedMinimum, int requestedMaximum) {
		return new EnchantmentTarget(
			enchantmentId,
			level,
			anyLevel,
			requestedMinimum,
			requestedMaximum
		);
	}

	public static String normalizeId(String value) {
		if (value == null || value.isBlank()) {
			return "minecraft:mending";
		}
		String normalized = value.trim().toLowerCase(Locale.ROOT);
		return normalized.contains(":") ? normalized : "minecraft:" + normalized;
	}
}
