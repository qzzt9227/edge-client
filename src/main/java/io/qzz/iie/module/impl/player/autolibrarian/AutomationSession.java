package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 保存一次自动化运行中的可变状态与玩家快照。
 */
final class AutomationSession {
	final LecternPlacementMemory lecternPlacementMemory = new LecternPlacementMemory();
	ClientLevel startedLevel;
	int villagerId = -1;
	BlockPos lecternPos;
	BlockHitResult placementHit;
	int timer;
	int originalSlot;
	float originalYaw;
	float originalPitch;
	boolean hasPlayerSnapshot;
	int lecternCountBeforePlacement;

	void begin(ClientLevel level, LocalPlayer player) {
		lecternPlacementMemory.clear();
		startedLevel = level;
		originalSlot = player.getInventory().getSelectedSlot();
		originalYaw = player.getYRot();
		originalPitch = player.getXRot();
		hasPlayerSnapshot = true;
	}

	void restorePlayer(LocalPlayer player) {
		if (!hasPlayerSnapshot || player == null) {
			return;
		}
		player.getInventory().setSelectedSlot(originalSlot);
		player.setYRot(originalYaw);
		player.setXRot(originalPitch);
	}

	void clear() {
		lecternPlacementMemory.clear();
		startedLevel = null;
		villagerId = -1;
		lecternPos = null;
		placementHit = null;
		timer = 0;
		hasPlayerSnapshot = false;
		lecternCountBeforePlacement = 0;
	}
}
