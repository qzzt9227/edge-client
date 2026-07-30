package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;

/**
 * 统计主背包和快捷栏中的讲台。
 */
final class LecternInventoryCounter {
	private LecternInventoryCounter() {
	}

	static int count(LocalPlayer player) {
		int count = 0;
		for (var stack : player.getInventory().getNonEquipmentItems()) {
			if (stack.is(Items.LECTERN)) {
				count += stack.getCount();
			}
		}
		return count;
	}
}
