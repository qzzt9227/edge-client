package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;
import java.util.Optional;

/**
 * 解析图书管理员交易并匹配附魔、等级模式和绿宝石价格。
 */
public final class TradeMatcher {
	private TradeMatcher() {
	}

	public static Optional<Match> findMatch(
		Iterable<MerchantOffer> offers,
		List<EnchantmentTarget> targets
	) {
		for (MerchantOffer offer : offers) {
			ItemStack result = offer.getResult();
			if (!result.is(Items.ENCHANTED_BOOK)) {
				continue;
			}
			ItemEnchantments enchantments = result.get(DataComponents.STORED_ENCHANTMENTS);
			if (enchantments == null || enchantments.isEmpty()) {
				continue;
			}
			int emeraldPrice = emeraldCount(offer.getCostA()) + emeraldCount(offer.getCostB());
			for (var entry : enchantments.entrySet()) {
				Identifier id = entry.getKey().unwrapKey()
					.map(key -> key.identifier())
					.orElse(null);
				if (id != null && matches(
					id.toString(),
					entry.getIntValue(),
					emeraldPrice,
					targets
				)) {
					return Optional.of(
						new Match(id.toString(), entry.getIntValue(), emeraldPrice)
					);
				}
			}
		}
		return Optional.empty();
	}

	public static boolean matches(
		String enchantmentId,
		int level,
		int emeraldPrice,
		Iterable<EnchantmentTarget> targets
	) {
		for (EnchantmentTarget target : targets) {
			if (target.enchantmentId().equals(enchantmentId)
				&& (target.anyLevel() || target.level() == level)
				&& emeraldPrice >= target.minEmeraldPrice()
				&& emeraldPrice <= target.maxEmeraldPrice()) {
				return true;
			}
		}
		return false;
	}

	private static int emeraldCount(ItemStack stack) {
		return stack.is(Items.EMERALD) ? stack.getCount() : 0;
	}

	public record Match(String enchantmentId, int level, int emeraldPrice) {
	}
}
