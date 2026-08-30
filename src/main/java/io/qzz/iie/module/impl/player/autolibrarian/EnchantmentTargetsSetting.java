package io.qzz.iie.module.impl.player.autolibrarian;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.qzz.iie.setting.EditorSetting;
import io.qzz.iie.setting.JsonSetting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 自动图书管理员的不可变目标规则集合。
 */
public final class EnchantmentTargetsSetting
	extends EditorSetting<List<EnchantmentTarget>>
	implements JsonSetting {

	public static final String EDITOR_ID = "auto_librarian_targets";
	private static final List<EnchantmentTarget> DEFAULT_TARGETS = List.of(
		new EnchantmentTarget("minecraft:unbreaking", 3, false, 4, 64),
		new EnchantmentTarget("minecraft:frost_walker", 2, false, 1, 64),
		new EnchantmentTarget("minecraft:protection", 4, false, 1, 64),
		new EnchantmentTarget("minecraft:punch", 2, false, 18, 64),
		new EnchantmentTarget("minecraft:knockback", 1, false, 1, 64)
	);

	public EnchantmentTargetsSetting(String id, String translationKey) {
		super(id, translationKey, DEFAULT_TARGETS, EDITOR_ID);
	}

	@Override
	protected List<EnchantmentTarget> normalize(List<EnchantmentTarget> requestedValue) {
		LinkedHashMap<String, EnchantmentTarget> unique = new LinkedHashMap<>();
		for (EnchantmentTarget target : requestedValue) {
			if (target != null) {
				unique.putIfAbsent(target.enchantmentId(), target);
			}
		}
		return List.copyOf(unique.values());
	}

	public boolean add(EnchantmentTarget target) {
		EnchantmentTarget normalized = target == null ? null : new EnchantmentTarget(
			target.enchantmentId(),
			target.level(),
			target.anyLevel(),
			target.minEmeraldPrice(),
			target.maxEmeraldPrice()
		);
		if (normalized == null || value().stream().anyMatch(
			existing -> existing.enchantmentId().equals(normalized.enchantmentId())
		)) {
			return false;
		}
		ArrayList<EnchantmentTarget> updated = new ArrayList<>(value());
		updated.add(normalized);
		set(updated);
		return true;
	}

	public void remove(String enchantmentId) {
		String normalizedId = EnchantmentTarget.normalizeId(enchantmentId);
		set(value().stream()
			.filter(target -> !target.enchantmentId().equals(normalizedId))
			.toList());
	}

	public void replace(int index, EnchantmentTarget target) {
		ArrayList<EnchantmentTarget> updated = new ArrayList<>(value());
		updated.set(index, target);
		set(updated);
	}

	@Override
	public JsonElement encodeJson() {
		JsonArray targets = new JsonArray();
		for (EnchantmentTarget target : value()) {
			JsonObject encoded = new JsonObject();
			encoded.addProperty("enchantmentId", target.enchantmentId());
			encoded.addProperty("level", target.level());
			encoded.addProperty("anyLevel", target.anyLevel());
			encoded.addProperty("minEmeraldPrice", target.minEmeraldPrice());
			encoded.addProperty("maxEmeraldPrice", target.maxEmeraldPrice());
			targets.add(encoded);
		}
		return targets;
	}

	@Override
	public void decodeJson(JsonElement value) {
		if (!value.isJsonArray()) {
			throw new IllegalArgumentException("Enchantment targets must be an array");
		}
		ArrayList<EnchantmentTarget> decoded = new ArrayList<>();
		for (JsonElement element : value.getAsJsonArray()) {
			if (!element.isJsonObject()) {
				throw new IllegalArgumentException("Enchantment target must be an object");
			}
			JsonObject target = element.getAsJsonObject();
			decoded.add(new EnchantmentTarget(
				requiredString(target, "enchantmentId"),
				requiredInt(target, "level"),
				requiredBoolean(target, "anyLevel"),
				requiredInt(target, "minEmeraldPrice"),
				requiredInt(target, "maxEmeraldPrice")
			));
		}
		set(decoded);
	}

	private static String requiredString(JsonObject object, String name) {
		JsonElement value = object.get(name);
		if (value == null || !value.isJsonPrimitive()
			|| !value.getAsJsonPrimitive().isString()) {
			throw new IllegalArgumentException(name + " must be a string");
		}
		return value.getAsString();
	}

	private static int requiredInt(JsonObject object, String name) {
		JsonElement value = object.get(name);
		if (value == null || !value.isJsonPrimitive()
			|| !value.getAsJsonPrimitive().isNumber()) {
			throw new IllegalArgumentException(name + " must be a number");
		}
		return value.getAsInt();
	}

	private static boolean requiredBoolean(JsonObject object, String name) {
		JsonElement value = object.get(name);
		if (value == null || !value.isJsonPrimitive()
			|| !value.getAsJsonPrimitive().isBoolean()) {
			throw new IllegalArgumentException(name + " must be a boolean");
		}
		return value.getAsBoolean();
	}
}
