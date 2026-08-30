package io.qzz.iie.module.impl.player.copynbt;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;
import java.util.Set;

/**
 * 复制 NBT 模块的 Minecraft 运行时桥接与拦截钩子。
 */
public final class CopyNbtHooks {
	private static volatile CopyNbtModule installedModule;

	private CopyNbtHooks() {
	}

	public static void install(CopyNbtModule module) {
		installedModule = Objects.requireNonNull(module, "module");
	}

	public static void uninstall(CopyNbtModule module) {
		if (installedModule == module) {
			installedModule = null;
		}
	}

	public static boolean isEnabled() {
		CopyNbtModule module = installedModule;
		return module != null && module.isEnabled();
	}

	/**
	 * 判断方块拾取（Pick Block）时是否携带 NBT 数据。
	 */
	public static boolean shouldIncludeBlockData(
		Minecraft client,
		BlockPos pos,
		boolean originalIncludeData
	) {
		CopyNbtModule module = installedModule;
		if (module == null || !module.isEnabled()) {
			return originalIncludeData;
		}
		boolean isCreative = isCreativeMode(client);
		if (!isCreative) {
			return originalIncludeData;
		}

		BlockEntity blockEntity = getBlockEntity(client, pos);
		BlockNbtCategory category = BlockNbtClassifier.classify(blockEntity);
		long actualBytes = getBlockEntityNbtBytes(client, blockEntity);
		boolean limitSize = module.limitSize().value();
		long maxSizeBytes = (long) (module.maxSizeKb().value() * 1024.0);
		boolean filterBlocks = module.filterBlocks().value();
		Set<BlockNbtCategory> allowedCategories = module.allowedCategories();

		return CopyNbtPolicy.shouldIncludeData(
			true,
			true,
			originalIncludeData,
			limitSize,
			maxSizeBytes,
			actualBytes,
			filterBlocks,
			category,
			allowedCategories
		);
	}

	/**
	 * 判断实体拾取（Pick Entity）时是否携带 NBT 数据。
	 */
	public static boolean shouldIncludeEntityData(
		Minecraft client,
		Entity entity,
		boolean originalIncludeData
	) {
		CopyNbtModule module = installedModule;
		if (module == null || !module.isEnabled()) {
			return originalIncludeData;
		}
		if (!module.copyEntityNbt().value()) {
			return originalIncludeData;
		}
		boolean isCreative = isCreativeMode(client);
		if (!isCreative) {
			return originalIncludeData;
		}

		return CopyNbtPolicy.shouldIncludeData(
			true,
			true,
			originalIncludeData,
			false,
			0,
			0,
			false,
			null,
			Set.of()
		);
	}

	/**
	 * 检查当前玩家是否处于创造模式。
	 */
	public static boolean isCreativeMode(Minecraft client) {
		if (client == null || client.player == null) {
			return false;
		}
		Player player = client.player;
		return player.isCreative() || player.hasInfiniteMaterials();
	}

	public static BlockEntity getBlockEntity(Minecraft client, BlockPos pos) {
		if (client == null || client.level == null || pos == null) {
			return null;
		}
		try {
			return client.level.getBlockEntity(pos);
		} catch (Throwable ignored) {
			return null;
		}
	}

	public static long getBlockEntityNbtBytes(Minecraft client, BlockEntity blockEntity) {
		if (client == null || client.level == null || blockEntity == null) {
			return 0L;
		}
		try {
			CompoundTag tag = blockEntity.saveWithFullMetadata(client.level.registryAccess());
			return tag != null ? tag.sizeInBytes() : 0L;
		} catch (Throwable ignored) {
			return 0L;
		}
	}
}
