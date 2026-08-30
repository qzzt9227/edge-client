package io.qzz.iie.module.impl.combat.bedaura;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

final class BedAuraItemAccess {

	boolean isBed(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof BedItem;
	}

	int findBedInHotbar(Inventory inventory) {
		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			if (isBed(inventory.getItem(slot))) {
				return slot;
			}
		}
		return -1;
	}

	int findBedInMainInventory(Inventory inventory) {
		for (int slot = 9; slot < 36; slot++) {
			if (isBed(inventory.getItem(slot))) {
				return slot;
			}
		}
		return -1;
	}

	boolean placeBed(Minecraft client, BlockHitResult hitResult) {
		LocalPlayer player = client.player;
		if (player == null || client.gameMode == null) {
			return false;
		}

		Inventory inventory = player.getInventory();
		int originalSelected = inventory.getSelectedSlot();
		int bedSlot = findBedInHotbar(inventory);
		boolean needSwap = false;
		int invBedSlot = -1;

		if (bedSlot < 0) {
			if (player.containerMenu != player.inventoryMenu) {
				return false;
			}
			invBedSlot = findBedInMainInventory(inventory);
			if (invBedSlot < 0) {
				return false;
			}
			bedSlot = originalSelected;
			swapInventoryWithHotbar(client, invBedSlot, bedSlot);
			needSwap = true;
		}

		inventory.setSelectedSlot(bedSlot);
		InteractionResult result = client.gameMode.useItemOn(
			player,
			InteractionHand.MAIN_HAND,
			hitResult
		);

		if (result.consumesAction()) {
			player.swing(InteractionHand.MAIN_HAND);
		}

		if (needSwap) {
			swapInventoryWithHotbar(client, invBedSlot, bedSlot);
		}
		inventory.setSelectedSlot(originalSelected);

		return result.consumesAction();
	}

	boolean interactWithPlacedBed(Minecraft client, BlockHitResult hitResult) {
		LocalPlayer player = client.player;
		if (player == null || client.gameMode == null) {
			return false;
		}

		InteractionResult result = client.gameMode.useItemOn(
			player,
			InteractionHand.MAIN_HAND,
			hitResult
		);

		if (result.consumesAction()) {
			player.swing(InteractionHand.MAIN_HAND);
			return true;
		}
		return false;
	}

	private static void swapInventoryWithHotbar(
		Minecraft client,
		int inventorySlot,
		int hotbarSlot
	) {
		LocalPlayer player = client.player;
		if (player == null || client.gameMode == null) {
			return;
		}
		client.gameMode.handleContainerInput(
			player.inventoryMenu.containerId,
			inventorySlot,
			hotbarSlot,
			ContainerInput.SWAP,
			player
		);
	}
}
