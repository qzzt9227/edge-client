package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 只通过原版按键状态驱动回收移动，并在退出时恢复真实输入。
 */
final class RecoveryMovementController {
	private final Minecraft client;
	private final RecoveryInputState state = new RecoveryInputState();

	RecoveryMovementController(Minecraft client) {
		this.client = client;
	}

	boolean physicalInputActive() {
		return PhysicalMovementInput.anyMovementDown(client);
	}

	MoveResult moveToward(Vec3 target, DirectRecoveryPath.Action action) {
		LocalPlayer player = client.player;
		if (player == null || action == DirectRecoveryPath.Action.BLOCKED) {
			release();
			return MoveResult.BLOCKED;
		}
		if (physicalInputActive()) {
			release();
			return MoveResult.MANUAL_OVERRIDE;
		}
		Vec3 delta = target.subtract(player.position());
		player.setYRot(Mth.wrapDegrees(
			(float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0)
		));
		state.apply(
			true,
			action == DirectRecoveryPath.Action.JUMP,
			action == DirectRecoveryPath.Action.CROUCH
		);
		apply(client.options.keyUp, state.forward());
		apply(client.options.keyJump, state.jump());
		apply(client.options.keyShift, state.shift());
		return MoveResult.MOVING;
	}

	void release() {
		state.release();
		restore(client.options.keyUp);
		restore(client.options.keyJump);
		restore(client.options.keyShift);
	}

	private static void apply(KeyMapping mapping, boolean down) {
		mapping.setDown(down);
	}

	private void restore(KeyMapping mapping) {
		mapping.setDown(PhysicalMovementInput.isDown(client, mapping));
	}

	enum MoveResult {
		MOVING,
		BLOCKED,
		MANUAL_OVERRIDE
	}
}
