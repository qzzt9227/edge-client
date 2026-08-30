package io.qzz.iie.module.impl.player.packetmine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * 包挖掘控制器：管理延迟计时、发包、严格视线判定、动态转头追踪与鬼手工具切换。
 */
public final class PacketMineController {
	private enum Phase {
		IDLE,
		DELAYING,
		MINING,
		RESETTING
	}

	private final PacketMineModule module;
	private Phase phase = Phase.IDLE;
	private BlockPos targetPos;
	private Direction targetDirection = Direction.UP;
	private int delayRemainingTicks;
	private float currentProgress;
	private boolean startPacketSent;

	private float startRotationYaw;
	private float startRotationPitch;
	private float serverYaw;
	private float serverPitch;
	private int rotationTicksRemaining;
	private int totalRotationTicks;

	private int originalCarriedSlot = -1;
	private int ghostToolSlot = -1;

	public PacketMineController(PacketMineModule module) {
		this.module = Objects.requireNonNull(module, "module");
	}

	public synchronized void start(BlockPos pos, Direction direction) {
		if (pos == null || !module.isEnabled()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) {
			return;
		}

		// 检查当前方块是否可被破坏
		BlockState state = mc.level.getBlockState(pos);
		if (state.isAir() || state.getDestroySpeed(mc.level, pos) < 0) {
			return;
		}

		// 严格检测 1：距离
		Vec3 eyePos = mc.player.getEyePosition();
		if (!PacketMinePolicy.isWithinReach(eyePos, pos, module.range().value())) {
			return;
		}

		// 严格检测 2：视线遮挡 (Raycast)
		if (!hasLineOfSight(mc.level, mc.player, eyePos, pos)) {
			return;
		}

		// 如果已经在挖同一个方块，不重复初始化
		if (phase == Phase.MINING && pos.equals(targetPos)) {
			return;
		}

		// 中止前一个任务（如果有）
		if (phase != Phase.IDLE) {
			abort();
		}

		this.targetPos = pos;
		this.targetDirection = direction != null ? direction : Direction.UP;
		this.delayRemainingTicks = (int) (double) module.delayTicks().value();
		this.currentProgress = 0.0f;
		this.startPacketSent = false;
		this.phase = delayRemainingTicks > 0 ? Phase.DELAYING : Phase.MINING;

		this.startRotationYaw = mc.player.getYRot();
		this.startRotationPitch = mc.player.getXRot();
		this.serverYaw = this.startRotationYaw;
		this.serverPitch = this.startRotationPitch;
		this.totalRotationTicks = Math.max(1, (int) (double) module.rotationTicks().value());
		this.rotationTicksRemaining = this.totalRotationTicks;
		this.originalCarriedSlot = -1;
		this.ghostToolSlot = -1;
	}

	public synchronized void abort() {
		if (phase == Phase.IDLE) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (startPacketSent && targetPos != null && mc.getConnection() != null) {
			mc.getConnection().send(new ServerboundPlayerActionPacket(
				ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
				targetPos,
				targetDirection
			));
		}
		restoreGhostHand(mc);
		startResetRotation(mc);
	}

	public synchronized void tick(Minecraft client) {
		if (client == null || client.player == null || client.level == null) {
			reset();
			return;
		}
		if (!module.isEnabled()) {
			if (phase != Phase.IDLE) {
				abort();
			}
			return;
		}

		LocalPlayer player = client.player;
		ClientLevel level = client.level;

		if (phase == Phase.IDLE) {
			PacketMineVisualState.update(PacketMineVisualState.Snapshot.INERT);
			return;
		}

		if (phase == Phase.RESETTING) {
			handleResettingTick(player);
			return;
		}

		if (targetPos == null) {
			abort();
			return;
		}

		BlockState blockState = level.getBlockState(targetPos);
		if (blockState.isAir()) {
			// 方块已被破坏或消失，完成回正
			completeBreak(client);
			return;
		}

		// 严格检测 1：距离
		Vec3 eyePos = player.getEyePosition();
		if (!PacketMinePolicy.isWithinReach(eyePos, targetPos, module.range().value())) {
			abort();
			return;
		}

		// 严格检测 2：视线遮挡 (Raycast)
		if (!hasLineOfSight(level, player, eyePos, targetPos)) {
			abort();
			return;
		}

		// 动态目标瞄准：即使玩家移动，准心始终锁定方块中心
		Vec3 blockCenter = Vec3.atCenterOf(targetPos);
		PacketMinePolicy.AimAngles targetAim = PacketMinePolicy.calculateAim(eyePos, blockCenter);
		if (rotationTicksRemaining > 0) {
			rotationTicksRemaining--;
			float factor = 1.0f - ((float) rotationTicksRemaining / (float) totalRotationTicks);
			serverYaw = PacketMinePolicy.interpolateAngle(startRotationYaw, targetAim.yaw(), factor);
			serverPitch = PacketMinePolicy.interpolateAngle(startRotationPitch, targetAim.pitch(), factor);
		} else {
			serverYaw = targetAim.yaw();
			serverPitch = targetAim.pitch();
		}
		sendRotation(player, serverYaw, serverPitch);

		// 处于延迟阶段
		if (phase == Phase.DELAYING) {
			delayRemainingTicks--;
			if (delayRemainingTicks <= 0) {
				phase = Phase.MINING;
			}
			updateVisualSnapshot();
			return;
		}

		// 处于挖掘阶段
		if (phase == Phase.MINING) {
			if (!startPacketSent) {
				startPacketSent = true;
				applyGhostHand(client, player, blockState);
				if (client.getConnection() != null) {
					client.getConnection().send(new ServerboundPlayerActionPacket(
						ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
						targetPos,
						targetDirection
					));
					client.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
				}
			}

			// 计算挖掘进度
			float delta = blockState.getDestroyProgress(player, level, targetPos);
			currentProgress += delta;

			// 持续挥手模拟挖掘
			if (client.getConnection() != null && client.player.tickCount % 4 == 0) {
				client.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
			}

			if (currentProgress >= 1.0f) {
				completeBreak(client);
			} else {
				updateVisualSnapshot();
			}
		}
	}

	private void completeBreak(Minecraft client) {
		if (client.getConnection() != null && targetPos != null) {
			client.getConnection().send(new ServerboundPlayerActionPacket(
				ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
				targetPos,
				targetDirection
			));
			client.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
		}
		restoreGhostHand(client);
		startResetRotation(client);
	}

	private static boolean hasLineOfSight(ClientLevel level, LocalPlayer player, Vec3 eyePos, BlockPos pos) {
		Vec3 blockCenter = Vec3.atCenterOf(pos);
		BlockHitResult hit = level.clip(new ClipContext(
			eyePos,
			blockCenter,
			ClipContext.Block.COLLIDER,
			ClipContext.Fluid.NONE,
			player
		));
		return hit.getType() != HitResult.Type.BLOCK || hit.getBlockPos().equals(pos);
	}

	private void applyGhostHand(Minecraft client, LocalPlayer player, BlockState state) {
		if (!module.ghostHand().value() || client.getConnection() == null) {
			return;
		}
		PacketMineToolFinder.BestToolResult best = PacketMineToolFinder.findBestHotbarTool(
			player.getInventory(),
			state
		);
		if (best != null && best.slot() != player.getInventory().getSelectedSlot()) {
			this.originalCarriedSlot = player.getInventory().getSelectedSlot();
			this.ghostToolSlot = best.slot();
			client.getConnection().send(new ServerboundSetCarriedItemPacket(best.slot()));
		}
	}

	private void restoreGhostHand(Minecraft client) {
		if (originalCarriedSlot >= 0 && client.getConnection() != null) {
			client.getConnection().send(new ServerboundSetCarriedItemPacket(originalCarriedSlot));
			originalCarriedSlot = -1;
			ghostToolSlot = -1;
		}
	}

	private void startResetRotation(Minecraft client) {
		phase = Phase.RESETTING;
		startRotationYaw = serverYaw;
		startRotationPitch = serverPitch;
		totalRotationTicks = Math.max(1, (int) (double) module.resetRotationTicks().value());
		rotationTicksRemaining = totalRotationTicks;
	}

	private void handleResettingTick(LocalPlayer player) {
		if (rotationTicksRemaining > 0) {
			rotationTicksRemaining--;
			float factor = 1.0f - ((float) rotationTicksRemaining / (float) totalRotationTicks);
			serverYaw = PacketMinePolicy.interpolateAngle(startRotationYaw, player.getYRot(), factor);
			serverPitch = PacketMinePolicy.interpolateAngle(startRotationPitch, player.getXRot(), factor);
			sendRotation(player, serverYaw, serverPitch);
			PacketMineVisualState.update(new PacketMineVisualState.Snapshot(
				true, serverYaw, serverPitch,
				false, targetPos, 1.0f,
				module.renderStyle().value(),
				module.fillColor(),
				module.lineColor()
			));
		} else {
			reset();
		}
	}

	private static void sendRotation(LocalPlayer player, float yaw, float pitch) {
		if (player != null && player.connection != null) {
			player.connection.send(new ServerboundMovePlayerPacket.Rot(
				yaw,
				pitch,
				player.onGround(),
				player.horizontalCollision
			));
		}
	}

	private void reset() {
		phase = Phase.IDLE;
		targetPos = null;
		currentProgress = 0.0f;
		startPacketSent = false;
		originalCarriedSlot = -1;
		ghostToolSlot = -1;
		PacketMineVisualState.update(PacketMineVisualState.Snapshot.INERT);
	}

	private void updateVisualSnapshot() {
		PacketMineVisualState.update(new PacketMineVisualState.Snapshot(
			true, serverYaw, serverPitch,
			true, targetPos, currentProgress,
			module.renderStyle().value(),
			module.fillColor(),
			module.lineColor()
		));
	}
}
