package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.core.BlockPos;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 回收后强制后续轮次复用同一个讲台位置。
 */
final class LecternPlacementMemory {
	private BlockPos position;

	void record(BlockPos position) {
		this.position = position == null ? null : position.immutable();
	}

	<T> T select(Function<BlockPos, T> exactPlacement, Supplier<T> initialSearch) {
		return position == null ? initialSearch.get() : exactPlacement.apply(position);
	}

	void clear() {
		position = null;
	}
}
