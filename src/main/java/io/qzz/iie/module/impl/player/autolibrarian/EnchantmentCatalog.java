package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 从当前世界注册表生成可搜索附魔目录。
 */
public final class EnchantmentCatalog {
	private static final Map<String, String> CHINESE_ALIASES = Map.ofEntries(
		Map.entry("minecraft:aqua_affinity", "水下速掘"),
		Map.entry("minecraft:bane_of_arthropods", "节肢杀手"),
		Map.entry("minecraft:binding_curse", "绑定诅咒"),
		Map.entry("minecraft:blast_protection", "爆炸保护"),
		Map.entry("minecraft:breach", "破甲"),
		Map.entry("minecraft:channeling", "引雷"),
		Map.entry("minecraft:density", "致密"),
		Map.entry("minecraft:depth_strider", "深海探索者"),
		Map.entry("minecraft:mending", "经验修补 修补"),
		Map.entry("minecraft:unbreaking", "耐久"),
		Map.entry("minecraft:efficiency", "效率"),
		Map.entry("minecraft:protection", "保护"),
		Map.entry("minecraft:sharpness", "锋利"),
		Map.entry("minecraft:feather_falling", "摔落缓冲"),
		Map.entry("minecraft:fire_aspect", "火焰附加"),
		Map.entry("minecraft:fire_protection", "火焰保护"),
		Map.entry("minecraft:flame", "火矢"),
		Map.entry("minecraft:fortune", "时运"),
		Map.entry("minecraft:frost_walker", "冰霜行者"),
		Map.entry("minecraft:impaling", "穿刺"),
		Map.entry("minecraft:silk_touch", "精准采集"),
		Map.entry("minecraft:looting", "抢夺"),
		Map.entry("minecraft:infinity", "无限"),
		Map.entry("minecraft:knockback", "击退"),
		Map.entry("minecraft:loyalty", "忠诚"),
		Map.entry("minecraft:luck_of_the_sea", "海之眷顾"),
		Map.entry("minecraft:lunge", "突进"),
		Map.entry("minecraft:lure", "饵钓"),
		Map.entry("minecraft:multishot", "多重射击"),
		Map.entry("minecraft:piercing", "穿透"),
		Map.entry("minecraft:power", "力量"),
		Map.entry("minecraft:projectile_protection", "弹射物保护"),
		Map.entry("minecraft:punch", "冲击"),
		Map.entry("minecraft:quick_charge", "快速装填"),
		Map.entry("minecraft:respiration", "水下呼吸"),
		Map.entry("minecraft:riptide", "激流"),
		Map.entry("minecraft:smite", "亡灵杀手"),
		Map.entry("minecraft:soul_speed", "灵魂疾行"),
		Map.entry("minecraft:sweeping_edge", "横扫之刃"),
		Map.entry("minecraft:swift_sneak", "迅捷潜行"),
		Map.entry("minecraft:thorns", "荆棘"),
		Map.entry("minecraft:vanishing_curse", "消失诅咒"),
		Map.entry("minecraft:wind_burst", "风爆")
	);

	private EnchantmentCatalog() {
	}

	public static List<Entry> load(Minecraft client) {
		if (client.level == null) {
			return List.of();
		}
		ArrayList<Entry> entries = new ArrayList<>();
		var registry = client.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		for (var registryEntry : registry.entrySet()) {
			Identifier id = registryEntry.getKey().identifier();
			Enchantment enchantment = registryEntry.getValue();
			String idString = id.toString();
			entries.add(new Entry(
				idString,
				enchantment.description().getString(),
				toTitle(id.getPath()),
				CHINESE_ALIASES.getOrDefault(idString, ""),
				enchantment.definition().maxLevel()
			));
		}
		entries.sort(Comparator.comparing(Entry::displayName, String.CASE_INSENSITIVE_ORDER));
		return List.copyOf(entries);
	}

	public static List<Entry> search(List<Entry> entries, String query, int limit) {
		String needle = normalize(query);
		return entries.stream()
			.filter(entry -> needle.isEmpty() || entry.searchText().contains(needle))
			.limit(Math.max(0, limit))
			.toList();
	}

	public static Entry find(List<Entry> entries, String id) {
		return entries.stream()
			.filter(entry -> entry.id().equals(id))
			.findFirst()
			.orElse(null);
	}

	public static String displayName(List<Entry> entries, String id) {
		Entry entry = find(entries, id);
		return entry == null ? id : entry.displayName();
	}

	public static int maxLevel(List<Entry> entries, String id) {
		Entry entry = find(entries, id);
		return entry == null ? 1 : Math.max(1, entry.maxLevel());
	}

	private static String normalize(String text) {
		return text == null ? "" : text.toLowerCase(Locale.ROOT)
			.replace(" ", "")
			.replace("_", "")
			.replace("-", "");
	}

	private static String toTitle(String path) {
		StringBuilder result = new StringBuilder();
		for (String part : path.split("_")) {
			if (!result.isEmpty()) {
				result.append(' ');
			}
			if (!part.isEmpty()) {
				result.append(Character.toUpperCase(part.charAt(0)))
					.append(part.substring(1));
			}
		}
		return result.toString();
	}

	public record Entry(
		String id,
		String translatedName,
		String englishName,
		String chineseAliases,
		int maxLevel
	) {
		public String displayName() {
			return translatedName == null || translatedName.isBlank()
				? englishName
				: translatedName;
		}

		private String searchText() {
			return normalize(
				id + " " + translatedName + " " + englishName + " " + chineseAliases
			);
		}
	}
}
