package io.qzz.iie.module.impl.render.itemrendermode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;

/**
 * 将版本敏感的 {@code ItemEntityRenderer} 注入点限制在一个极小的桥接面中。
 *
 * <p>Mixin 只负责在提取阶段按实体 UUID 解析冻结角度、在提交阶段改写旋转，
 * “当前模式”和“冻结角度快照”都集中在本类，便于脱离 Minecraft 做纯逻辑测试。</p>
 */
public final class ItemRenderModeHooks {
	private static volatile ItemRenderModeModule module;
	private static final Map<UUID, Float> FROZEN_ANGLES = new HashMap<>();

	private ItemRenderModeHooks() {
	}

	public static void install(ItemRenderModeModule installedModule) {
		module = Objects.requireNonNull(installedModule, "installedModule");
	}

	/**
	 * 当前应生效的渲染行为；模块未启用或未安装时返回 {@link ItemRenderMode#VANILLA}。
	 */
	public static ItemRenderMode renderMode() {
		ItemRenderModeModule current = module;
		if (current == null || !current.isEnabled()) {
			return ItemRenderMode.VANILLA;
		}
		return current.renderModeSetting().value();
	}

	/**
	 * 状态提取阶段调用：把解析后的冻结角度写入渲染状态。
	 *
	 * <p>非冻结模式下把角度清空，避免把上一帧的冻结值带到 2D/原版渲染中。</p>
	 */
	public static void applyFrozenSpin(UUID uuid, float currentSpin, ItemEntityRenderState state) {
		if (!(state instanceof ItemRenderModeRenderState renderState)) {
			return;
		}
		if (renderMode() != ItemRenderMode.FREEZE_ROTATION) {
			renderState.frozenSpin = null;
			return;
		}
		renderState.frozenSpin = resolveFrozenSpin(uuid, currentSpin);
	}

	/**
	 * 返回实体的冻结角度；首次出现时以当前角度作为冻结快照并记录。
	 */
	public static float resolveFrozenSpin(UUID uuid, float currentSpin) {
		Float frozen = FROZEN_ANGLES.get(uuid);
		if (frozen == null) {
			frozen = currentSpin;
			FROZEN_ANGLES.put(uuid, frozen);
		}
		return frozen;
	}

	/**
	 * 移除已不存在实体的冻结角度，防止长时间游戏积累无界状态。
	 */
	public static void pruneFrozenAngles(Collection<UUID> liveIds) {
		FROZEN_ANGLES.keySet().retainAll(liveIds);
	}

	public static void clearFrozenAngles() {
		FROZEN_ANGLES.clear();
	}
}
