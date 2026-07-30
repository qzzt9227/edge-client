package io.qzz.iie.module.impl.player.autolibrarian;

import java.util.List;

/**
 * 单个客户端 tick 使用的自动图书管理员设置快照。
 */
record AutoLibrarianSettings(
	List<EnchantmentTarget> targets,
	int searchRadius,
	int placementRadius,
	boolean allowHandMining,
	boolean reportTrades,
	boolean autoRecycle,
	int recycleRadius,
	int beforeRecycleTicks,
	int recycleSearchTimeoutTicks,
	int rotationTicks,
	int beforePlaceTicks,
	int professionCheckIntervalTicks,
	int beforeOpenTradeTicks,
	int beforeBreakTicks,
	int afterBreakTicks
) {
	static final int PROFESSION_CHECK_INTERVAL_TICKS = 10;

	AutoLibrarianSettings {
		targets = List.copyOf(targets);
	}
}
