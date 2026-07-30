package io.qzz.iie.module.impl.player.autolibrarian;

import java.util.List;
import java.util.Objects;

/**
 * 保留旧模块的常用附魔推荐顺序。
 */
public final class RecommendedEnchantments {
	private static final List<String> IDS = List.of(
		"minecraft:mending",
		"minecraft:unbreaking",
		"minecraft:efficiency",
		"minecraft:protection",
		"minecraft:sharpness"
	);

	private RecommendedEnchantments() {
	}

	public static List<EnchantmentCatalog.Entry> select(
		List<EnchantmentCatalog.Entry> catalog,
		int limit
	) {
		return IDS.stream()
			.map(id -> EnchantmentCatalog.find(catalog, id))
			.filter(Objects::nonNull)
			.limit(Math.max(0, limit))
			.toList();
	}
}
