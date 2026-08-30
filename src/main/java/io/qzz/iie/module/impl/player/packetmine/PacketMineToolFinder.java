package io.qzz.iie.module.impl.player.packetmine;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 鬼手挖掘工具检索策略：自动在快捷栏/背包中寻找破坏方块速度最高的最优工具。
 */
public final class PacketMineToolFinder {
	private PacketMineToolFinder() {
	}

	public record BestToolResult(int slot, ItemStack stack, float destroySpeed) {
	}

	/**
	 * 在快捷栏 (0..8) 中检索最适工具。
	 */
	public static BestToolResult findBestHotbarTool(Inventory inventory, BlockState blockState) {
		if (inventory == null || blockState == null) {
			return null;
		}
		int bestSlot = -1;
		ItemStack bestStack = ItemStack.EMPTY;
		float bestSpeed = -1.0f;

		for (int i = 0; i < 9; i++) {
			ItemStack stack = inventory.getItem(i);
			float speed = stack.getDestroySpeed(blockState);
			if (speed > bestSpeed) {
				bestSpeed = speed;
				bestSlot = i;
				bestStack = stack;
			}
		}
		if (bestSlot >= 0 && bestSpeed > 1.0f) {
			return new BestToolResult(bestSlot, bestStack, bestSpeed);
		}
		return null;
	}
}
