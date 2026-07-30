package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 查找讲台及最快的快捷栏挖掘工具。
 */
final class HotbarSelector {
	int findItem(LocalPlayer player, Item item) {
		for (int slot = 0; slot < 9; slot++) {
			if (player.getInventory().getItem(slot).is(item)) {
				return slot;
			}
		}
		return -1;
	}

	int findBestMiningSlot(
		LocalPlayer player,
		BlockState state,
		boolean allowHandMining,
		int fallbackSlot
	) {
		int bestSlot = -1;
		int emptySlot = -1;
		float bestSpeed = 1.0F;
		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.isEmpty()) {
				emptySlot = slot;
				continue;
			}
			float speed = stack.getDestroySpeed(state);
			if (speed > bestSpeed) {
				bestSpeed = speed;
				bestSlot = slot;
			}
		}
		if (bestSlot >= 0) {
			return bestSlot;
		}
		if (!allowHandMining) {
			return -1;
		}
		return emptySlot >= 0 ? emptySlot : fallbackSlot;
	}
}
