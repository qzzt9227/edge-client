package io.qzz.iie.module.impl.combat.autoweb;

import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.HotbarMode;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.InventoryMode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;

final class AutoWebItemAccess {
	private PendingRestore pendingRestore;

	boolean restorePending(Minecraft client) {
		if (pendingRestore == null) {
			return true;
		}
		LocalPlayer player = client.player;
		if (player == null || client.gameMode == null) {
			return false;
		}
		player.getInventory().setSelectedSlot(pendingRestore.originalSelected());
		if (pendingRestore.restoreSwap()) {
			// 容器界面打开时延后交换，避免向错误的 containerId 发送点击。
			if (player.containerMenu != player.inventoryMenu) {
				return false;
			}
			swapInventoryWithHotbar(
				client,
				pendingRestore.inventorySlot(),
				pendingRestore.hotbarSlot()
			);
		}
		pendingRestore = null;
		return true;
	}

	boolean place(
		Minecraft client,
		BlockHitResult hit,
		HotbarMode hotbarMode,
		boolean checkInventory,
		InventoryMode inventoryMode
	) {
		LocalPlayer player = client.player;
		if (player == null || client.gameMode == null || pendingRestore != null) {
			return false;
		}
		PreparedWeb prepared = prepare(
			client,
			hotbarMode,
			checkInventory,
			inventoryMode
		);
		if (prepared == null) {
			return false;
		}

		Inventory inventory = player.getInventory();
		inventory.setSelectedSlot(prepared.webHotbarSlot());
		InteractionResult result = client.gameMode.useItemOn(
			player,
			InteractionHand.MAIN_HAND,
			hit
		);
		if (result.consumesAction()) {
			player.swing(InteractionHand.MAIN_HAND);
		}

		if (hotbarMode == HotbarMode.VISIBLE
			&& prepared.webHotbarSlot() != prepared.originalSelected()
			&& !prepared.restoreSwap()) {
			// 保留到下一帧再恢复，让可见模式确实显示一次槽位切换。
			pendingRestore = new PendingRestore(
				prepared.originalSelected(),
				prepared.inventorySlot(),
				prepared.webHotbarSlot(),
				prepared.restoreSwap()
			);
		} else {
			inventory.setSelectedSlot(prepared.originalSelected());
			if (prepared.restoreSwap()) {
				swapInventoryWithHotbar(
					client,
					prepared.inventorySlot(),
					prepared.webHotbarSlot()
				);
			}
		}
		return result.consumesAction();
	}

	void reset(Minecraft client) {
		restorePending(client);
	}

	private PreparedWeb prepare(
		Minecraft client,
		HotbarMode hotbarMode,
		boolean checkInventory,
		InventoryMode inventoryMode
	) {
		LocalPlayer player = client.player;
		Inventory inventory = player.getInventory();
		int originalSelected = inventory.getSelectedSlot();
		if (hotbarMode == HotbarMode.HELD_ONLY) {
			return isWeb(inventory, originalSelected)
				? new PreparedWeb(originalSelected, originalSelected, -1, false)
				: null;
		}

		int hotbarSlot = findWeb(inventory, 0, Inventory.getSelectionSize());
		if (hotbarSlot >= 0) {
			return new PreparedWeb(originalSelected, hotbarSlot, -1, false);
		}
		if (!checkInventory || player.containerMenu != player.inventoryMenu) {
			return null;
		}

		int inventorySlot = findWeb(inventory, 9, 36);
		if (inventorySlot < 0) {
			return null;
		}
		int destination = switch (inventoryMode) {
			case SILENT_SELECTED_RESTORE -> originalSelected;
			case TEMPORARY_RESTORE -> {
				int empty = findEmptyHotbar(inventory);
				yield empty >= 0 ? empty : originalSelected;
			}
			case MOVE_TO_EMPTY -> findEmptyHotbar(inventory);
		};
		if (destination < 0) {
			return null;
		}

		swapInventoryWithHotbar(client, inventorySlot, destination);
		boolean restoreSwap = inventoryMode != InventoryMode.MOVE_TO_EMPTY;
		return new PreparedWeb(
			originalSelected,
			destination,
			inventorySlot,
			restoreSwap
		);
	}

	private static void swapInventoryWithHotbar(
		Minecraft client,
		int inventorySlot,
		int hotbarSlot
	) {
		LocalPlayer player = client.player;
		client.gameMode.handleContainerInput(
			player.inventoryMenu.containerId,
			inventorySlot,
			hotbarSlot,
			ContainerInput.SWAP,
			player
		);
	}

	private static int findWeb(Inventory inventory, int start, int end) {
		for (int slot = start; slot < end; slot++) {
			if (isWeb(inventory, slot)) {
				return slot;
			}
		}
		return -1;
	}

	private static int findEmptyHotbar(Inventory inventory) {
		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			if (inventory.getItem(slot).isEmpty()) {
				return slot;
			}
		}
		return -1;
	}

	private static boolean isWeb(Inventory inventory, int slot) {
		return inventory.getItem(slot).is(Items.COBWEB);
	}

	private record PreparedWeb(
		int originalSelected,
		int webHotbarSlot,
		int inventorySlot,
		boolean restoreSwap
	) {
	}

	private record PendingRestore(
		int originalSelected,
		int inventorySlot,
		int hotbarSlot,
		boolean restoreSwap
	) {
	}
}
