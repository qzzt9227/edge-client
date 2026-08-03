package io.qzz.iie.module.impl.render.itemrendermode;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.KeybindSetting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 改变世界内掉落物（{@link ItemEntity}）渲染方式的纯客户端模块。
 *
 * <p>模块只声明模式与快捷键；实际旋转改写由 {@link ItemRenderModeHooks}
 * 在 {@code ItemEntityRenderer} 的 Mixin 注入点完成。2D 模式让掉落物正面
 * 始终面向玩家相机，冻结模式让每个掉落物保持自己当前的旋转角度。</p>
 */
public final class ItemRenderModeModule extends Module {
	private final ChoiceSetting<ItemRenderMode> renderMode = setting(new ChoiceSetting<>(
		"render_mode",
		"client.setting.item_render_mode.render_mode",
		ItemRenderMode.BILLBOARD,
		List.of(
			new ChoiceOption<>(
				"billboard",
				"client.option.item_render_mode.billboard",
				ItemRenderMode.BILLBOARD
			),
			new ChoiceOption<>(
				"freeze_rotation",
				"client.option.item_render_mode.freeze_rotation",
				ItemRenderMode.FREEZE_ROTATION
			)
		)
	));
	private final KeybindSetting shortcut = keybind(
		new KeybindSetting("keybind", "client.setting.module_keybind")
	);
	private boolean freezeActive;

	public ItemRenderModeModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "item_render_mode"),
			"client.module.item_render_mode.name",
			"client.module.item_render_mode.description",
			100
		));
	}

	public ChoiceSetting<ItemRenderMode> renderModeSetting() {
		return renderMode;
	}

	@Override
	protected void onEnable() {
		freezeActive = renderMode.value() == ItemRenderMode.FREEZE_ROTATION;
		ItemRenderModeHooks.clearFrozenAngles();
	}

	@Override
	protected void onDisable() {
		freezeActive = false;
		ItemRenderModeHooks.clearFrozenAngles();
	}

	@Override
	protected void onClientTick() {
		if (renderMode.value() == ItemRenderMode.FREEZE_ROTATION) {
			if (!freezeActive) {
				ItemRenderModeHooks.clearFrozenAngles();
				freezeActive = true;
			}
			pruneFrozenAngles();
		} else {
			freezeActive = false;
		}
	}

	/**
	 * 只保留当前仍存在的掉落物，避免冻结角度快照无界增长。
	 */
	private void pruneFrozenAngles() {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}
		Set<UUID> liveIds = new HashSet<>();
		for (Entity entity : level.entitiesForRendering()) {
			if (entity instanceof ItemEntity) {
				liveIds.add(entity.getUUID());
			}
		}
		ItemRenderModeHooks.pruneFrozenAngles(liveIds);
	}
}
