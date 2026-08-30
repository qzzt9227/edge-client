package io.qzz.iie.module.impl.player.copynbt;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;

import java.util.EnumSet;
import java.util.Set;

/**
 * 复制方块/实体 NBT 模块。
 *
 * <p>启用后，玩家在创造模式下使用鼠标中键（选取方块/实体）时，
 * 自动携带该方块或实体的完整 NBT 标签与属性数据（无需按住 Ctrl 键）。</p>
 */
public final class CopyNbtModule extends Module {
	private final BooleanSetting limitSize = setting(new BooleanSetting(
		"limit_size",
		"client.setting.copy_nbt.limit_size",
		false
	));

	private final DoubleSetting maxSizeKb = setting(new DoubleSetting(
		"max_size_kb",
		"client.setting.copy_nbt.max_size_kb",
		2048.0,
		1.0,
		2048.0,
		1.0
	).visibleWhen(() -> limitSize.value()));

	private final BooleanSetting filterBlocks = setting(new BooleanSetting(
		"filter_blocks",
		"client.setting.copy_nbt.filter_blocks",
		false
	));

	private final BooleanSetting allowSpecial = setting(new BooleanSetting(
		"allow_special",
		"client.setting.copy_nbt.allow_special",
		true
	).visibleWhen(() -> filterBlocks.value()));

	private final BooleanSetting allowDecorative = setting(new BooleanSetting(
		"allow_decorative",
		"client.setting.copy_nbt.allow_decorative",
		true
	).visibleWhen(() -> filterBlocks.value()));

	private final BooleanSetting allowContainers = setting(new BooleanSetting(
		"allow_containers",
		"client.setting.copy_nbt.allow_containers",
		true
	).visibleWhen(() -> filterBlocks.value()));

	private final BooleanSetting allowProcessing = setting(new BooleanSetting(
		"allow_processing",
		"client.setting.copy_nbt.allow_processing",
		true
	).visibleWhen(() -> filterBlocks.value()));

	private final BooleanSetting allowRedstone = setting(new BooleanSetting(
		"allow_redstone",
		"client.setting.copy_nbt.allow_redstone",
		true
	).visibleWhen(() -> filterBlocks.value()));

	private final BooleanSetting allowAdvanced = setting(new BooleanSetting(
		"allow_advanced",
		"client.setting.copy_nbt.allow_advanced",
		true
	).visibleWhen(() -> filterBlocks.value()));

	private final BooleanSetting allowOther = setting(new BooleanSetting(
		"allow_other",
		"client.setting.copy_nbt.allow_other",
		true
	).visibleWhen(() -> filterBlocks.value()));

	private final BooleanSetting copyEntityNbt = setting(new BooleanSetting(
		"copy_entity_nbt",
		"client.setting.copy_nbt.copy_entity_nbt",
		true
	));

	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public CopyNbtModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "copy_nbt"),
			"client.module.copy_nbt.name",
			"client.module.copy_nbt.description",
			250
		));
	}

	public BooleanSetting limitSize() {
		return limitSize;
	}

	public DoubleSetting maxSizeKb() {
		return maxSizeKb;
	}

	public BooleanSetting filterBlocks() {
		return filterBlocks;
	}

	public BooleanSetting allowSpecial() {
		return allowSpecial;
	}

	public BooleanSetting allowDecorative() {
		return allowDecorative;
	}

	public BooleanSetting allowContainers() {
		return allowContainers;
	}

	public BooleanSetting allowProcessing() {
		return allowProcessing;
	}

	public BooleanSetting allowRedstone() {
		return allowRedstone;
	}

	public BooleanSetting allowAdvanced() {
		return allowAdvanced;
	}

	public BooleanSetting allowOther() {
		return allowOther;
	}

	public BooleanSetting copyEntityNbt() {
		return copyEntityNbt;
	}

	public Set<BlockNbtCategory> allowedCategories() {
		if (!filterBlocks.value()) {
			return EnumSet.allOf(BlockNbtCategory.class);
		}
		Set<BlockNbtCategory> set = EnumSet.noneOf(BlockNbtCategory.class);
		if (allowSpecial.value()) set.add(BlockNbtCategory.SPECIAL);
		if (allowDecorative.value()) set.add(BlockNbtCategory.DECORATIVE);
		if (allowContainers.value()) set.add(BlockNbtCategory.CONTAINERS);
		if (allowProcessing.value()) set.add(BlockNbtCategory.PROCESSING);
		if (allowRedstone.value()) set.add(BlockNbtCategory.REDSTONE);
		if (allowAdvanced.value()) set.add(BlockNbtCategory.ADVANCED);
		if (allowOther.value()) set.add(BlockNbtCategory.OTHER);
		return set;
	}

	@Override
	protected void onEnable() {
		CopyNbtHooks.install(this);
	}

	@Override
	protected void onDisable() {
		CopyNbtHooks.uninstall(this);
	}
}
