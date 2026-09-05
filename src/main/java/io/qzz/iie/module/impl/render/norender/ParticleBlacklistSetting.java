package io.qzz.iie.module.impl.render.norender;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.qzz.iie.setting.EditorSetting;
import io.qzz.iie.setting.JsonSetting;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 粒子黑名单配置项。
 *
 * <p>存储需要屏蔽的粒子类型命名空间 ID 集合。
 * 由专用设置子页 {@link io.qzz.iie.ui.screen.ParticleListEditorScreen} 进行编辑交互。</p>
 */
public final class ParticleBlacklistSetting extends EditorSetting<Set<String>> implements JsonSetting {
	public static final String EDITOR_ID = "particle_blacklist";

	public ParticleBlacklistSetting(String id, String translationKey) {
		super(id, translationKey, Set.of(), EDITOR_ID);
	}

	@Override
	protected Set<String> normalize(Set<String> requestedValue) {
		Objects.requireNonNull(requestedValue, "requestedValue");
		return Collections.unmodifiableSet(new LinkedHashSet<>(requestedValue));
	}

	public boolean isBlocked(String particleId) {
		if (particleId == null) {
			return false;
		}
		return value().contains(particleId);
	}

	public void toggle(String particleId) {
		Objects.requireNonNull(particleId, "particleId");
		Set<String> next = new LinkedHashSet<>(value());
		if (next.contains(particleId)) {
			next.remove(particleId);
		} else {
			next.add(particleId);
		}
		set(next);
	}

	public void clear() {
		set(Set.of());
	}

	@Override
	public JsonElement encodeJson() {
		JsonArray array = new JsonArray();
		for (String id : value()) {
			array.add(new JsonPrimitive(id));
		}
		return array;
	}

	@Override
	public void decodeJson(JsonElement element) {
		if (element == null || !element.isJsonArray()) {
			set(Set.of());
			return;
		}
		Set<String> loaded = new LinkedHashSet<>();
		for (JsonElement item : element.getAsJsonArray()) {
			if (item != null && item.isJsonPrimitive() && item.getAsJsonPrimitive().isString()) {
				loaded.add(item.getAsString());
			}
		}
		set(loaded);
	}
}
