package io.qzz.iie.module.impl.player.copynbt;

import java.util.Set;

/**
 * 复制方块/实体 NBT 的纯逻辑策略。
 *
 * <p>负责根据模块启用状态、游戏模式（是否为创造模式）、NBT 大小限制以及允许的方块分类，
 * 判定选取物品时是否携带 NBT 标签与属性数据。</p>
 */
public final class CopyNbtPolicy {
	private CopyNbtPolicy() {
	}

	/**
	 * 判定是否应携带 NBT 数据。
	 *
	 * @param moduleEnabled 模块是否启用
	 * @param isCreative 玩家是否处于创造模式
	 * @param originalIncludeData 原始 includeData 状态（例如玩家是否按住 Ctrl）
	 * @param limitSize 是否启用 NBT 大小限制
	 * @param maxSizeBytes 最大允许的 NBT 字节数
	 * @param actualBytes 实际检测到的 NBT 字节数（若小于等于 0 则视为未限制或未知）
	 * @param filterBlocks 是否开启方块过滤子设置
	 * @param category 当前方块的分类（若为 null 则不进行分类拦截）
	 * @param allowedCategories 允许的方块分类集合
	 * @return 最终发送给服务端的 includeData 标志
	 */
	public static boolean shouldIncludeData(
		boolean moduleEnabled,
		boolean isCreative,
		boolean originalIncludeData,
		boolean limitSize,
		long maxSizeBytes,
		long actualBytes,
		boolean filterBlocks,
		BlockNbtCategory category,
		Set<BlockNbtCategory> allowedCategories
	) {
		if (!moduleEnabled) {
			return originalIncludeData;
		}
		if (!isCreative) {
			return originalIncludeData;
		}
		if (filterBlocks && category != null && allowedCategories != null && !allowedCategories.contains(category)) {
			return false;
		}
		if (limitSize && actualBytes > 0 && actualBytes > maxSizeBytes) {
			return false;
		}
		return true;
	}
}
