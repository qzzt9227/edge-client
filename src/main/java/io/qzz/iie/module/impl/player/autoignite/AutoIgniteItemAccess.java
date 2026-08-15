package io.qzz.iie.module.impl.player.autoignite;

import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.IgnitionItem;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.ItemPriority;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.ItemSource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;

final class AutoIgniteItemAccess {
	boolean hasItem(Minecraft client, ItemSource source, ItemPriority priority) {
		LocalPlayer player = client.player;
		if (player == null) {
			return false;
		}
		Inventory inventory = player.getInventory();
		int start = source == ItemSource.HOTBAR ? 0 : Inventory.getSelectionSize();
		int end = source == ItemSource.HOTBAR ? Inventory.getSelectionSize() : 36;
		return findPreferred(inventory, start, end, priority) != null;
	}

	boolean ignite(
		Minecraft client,
		BlockHitResult hit,
		ItemSource source,
		ItemPriority priority,
		boolean restoreAfterFlint
	) {
		LocalPlayer player = client.player;
		if (player == null || client.gameMode == null) {
			return false;
		}
		if (source == ItemSource.SILENT_INVENTORY
			&& player.containerMenu != player.inventoryMenu) {
			return false;
		}

		Inventory inventory = player.getInventory();
		int originalSelected = inventory.getSelectedSlot();
		int start = source == ItemSource.HOTBAR ? 0 : Inventory.getSelectionSize();
		int end = source == ItemSource.HOTBAR ? Inventory.getSelectionSize() : 36;
		FoundItem found = findPreferred(inventory, start, end, priority);
		if (found == null) {
			return false;
		}

		boolean swappedInventory = source == ItemSource.SILENT_INVENTORY;
		if (swappedInventory) {
			swapInventoryWithHotbar(client, found.slot(), originalSelected);
		} else {
			inventory.setSelectedSlot(found.slot());
		}

		InteractionResult result;
		try {
			result = client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
			if (result.consumesAction()) {
				player.swing(InteractionHand.MAIN_HAND);
			}
		} finally {
			if (swappedInventory) {
				swapInventoryWithHotbar(client, found.slot(), originalSelected);
				inventory.setSelectedSlot(originalSelected);
			} else if (AutoIgniteItemPolicy.shouldRestoreSelection(
				source,
				found.item(),
				restoreAfterFlint
			)) {
				inventory.setSelectedSlot(originalSelected);
			}
		}
		return result.consumesAction();
	}

	private static FoundItem findPreferred(
		Inventory inventory,
		int start,
		int end,
		ItemPriority priority
	) {
		for (IgnitionItem preferred : priority.order()) {
			Item expected = item(preferred);
			for (int slot = start; slot < end; slot++) {
				if (inventory.getItem(slot).is(expected)) {
					return new FoundItem(slot, preferred);
				}
			}
		}
		return null;
	}

	private static Item item(IgnitionItem item) {
		return switch (item) {
			case FLINT_AND_STEEL -> Items.FLINT_AND_STEEL;
			case FIRE_CHARGE -> Items.FIRE_CHARGE;
		};
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

	private record FoundItem(int slot, IgnitionItem item) {
	}
}
