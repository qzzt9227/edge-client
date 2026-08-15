package io.qzz.iie.module.impl.player.autoignite;

import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.TargetHandling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

final class AutoIgniteController {
	private static final int PLACEMENT_CONFIRM_TICKS = 8;

	private final AutoIgniteItemAccess itemAccess = new AutoIgniteItemAccess();
	private final AutoIgniteTargetQueue targets = new AutoIgniteTargetQueue();
	private final Map<Long, Integer> placementCandidates = new LinkedHashMap<>();

	private ClientLevel trackedLevel;
	private LocalPlayer trackedPlayer;
	private Phase phase = Phase.IDLE;
	private long activeTargetId;
	private long activeTarget;
	private BlockHitResult activeHit;
	private float originalYaw;
	private float originalPitch;
	private float phaseStartYaw;
	private float phaseStartPitch;
	private float phaseTargetYaw;
	private float phaseTargetPitch;
	private float serverYaw;
	private float serverPitch;
	private int phaseTick;
	private int phaseDuration;
	private boolean cameraFollows;

	void recordPlacementCandidate(long packedBlockPos) {
		Minecraft client = Minecraft.getInstance();
		if (trackedLevel != client.level || trackedPlayer != client.player) {
			clearState();
			trackedLevel = client.level;
			trackedPlayer = client.player;
		}
		placementCandidates.put(packedBlockPos, PLACEMENT_CONFIRM_TICKS);
	}

	void tick(AutoIgniteModule module) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		ClientLevel level = client.level;
		if (player == null
			|| level == null
			|| client.gameMode == null
			|| client.gui.screen() != null
			|| player.isSpectator()
			|| !player.isAlive()) {
			reset(client);
			return;
		}
		if (trackedLevel != level || trackedPlayer != player) {
			clearState();
			trackedLevel = level;
			trackedPlayer = player;
		}

		confirmPlacements(level, module.targetHandling().value());
		if (phase != Phase.IDLE && !isCurrentQueueTarget()) {
			beginRestore(player);
		}

		switch (phase) {
			case IDLE -> beginTarget(module, client, level, player);
			case TURNING -> advanceTurning(module, client, level, player);
			case RESTORING -> advanceRestore(player);
		}
	}

	void reset(Minecraft client) {
		LocalPlayer player = client.player;
		if (player != null && phase != Phase.IDLE) {
			sendRotation(player, cameraFollows ? originalYaw : player.getYRot(),
				cameraFollows ? originalPitch : player.getXRot());
			if (cameraFollows) {
				player.setYRot(originalYaw);
				player.setXRot(originalPitch);
			}
		}
		clearState();
		trackedLevel = client.level;
		trackedPlayer = client.player;
	}

	private void clearState() {
		phase = Phase.IDLE;
		activeHit = null;
		targets.clear();
		placementCandidates.clear();
		AutoIgniteVisualState.clear();
	}

	private void confirmPlacements(ClientLevel level, TargetHandling handling) {
		Iterator<Map.Entry<Long, Integer>> iterator = placementCandidates.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Long, Integer> candidate = iterator.next();
			BlockPos pos = BlockPos.of(candidate.getKey());
			if (level.getBlockState(pos).is(Blocks.TNT)) {
				targets.offer(candidate.getKey(), handling);
				iterator.remove();
				continue;
			}
			int remaining = candidate.getValue() - 1;
			if (remaining <= 0) {
				iterator.remove();
			} else {
				candidate.setValue(remaining);
			}
		}
	}

	private boolean isCurrentQueueTarget() {
		OptionalLong current = targets.peekId();
		return current.isPresent() && current.getAsLong() == activeTargetId;
	}

	private void beginTarget(
		AutoIgniteModule module,
		Minecraft client,
		ClientLevel level,
		LocalPlayer player
	) {
		OptionalLong next = targets.peek();
		if (next.isEmpty()) {
			return;
		}
		long packed = next.getAsLong();
		BlockPos pos = BlockPos.of(packed);
		if (!level.getBlockState(pos).is(Blocks.TNT)
			|| !itemAccess.hasItem(client, module.itemSource().value(), module.itemPriority().value())) {
			targets.removeCurrent();
			return;
		}

		Optional<BlockHitResult> requestedHit = interactionHit(
			level,
			player,
			pos,
			module.strictInteraction().value()
		);
		if (requestedHit.isEmpty()) {
			targets.removeCurrent();
			return;
		}

		activeTargetId = targets.peekId().orElseThrow();
		activeTarget = packed;
		activeHit = requestedHit.get();
		if (client.hitResult instanceof BlockHitResult crosshairHit
			&& crosshairHit.getBlockPos().equals(pos)) {
			ignite(module, client, crosshairHit);
			targets.removeById(activeTargetId);
			activeHit = null;
			return;
		}

		RotationAngles target = anglesTo(player.getEyePosition(), activeHit.getLocation());
		originalYaw = player.getYRot();
		originalPitch = player.getXRot();
		phaseStartYaw = originalYaw;
		phaseStartPitch = originalPitch;
		phaseTargetYaw = target.yaw();
		phaseTargetPitch = target.pitch();
		serverYaw = originalYaw;
		serverPitch = originalPitch;
		phaseTick = 0;
		phaseDuration = (int) Math.round(module.rotationTicks().value());
		cameraFollows = module.cameraFollows().value();
		phase = Phase.TURNING;
		AutoIgniteVisualState.publish(serverYaw, serverPitch, cameraFollows);
	}

	private void advanceTurning(
		AutoIgniteModule module,
		Minecraft client,
		ClientLevel level,
		LocalPlayer player
	) {
		if (!level.getBlockState(BlockPos.of(activeTarget)).is(Blocks.TNT)) {
			beginRestore(player);
			return;
		}
		phaseTick++;
		double progress = (double) phaseTick / phaseDuration;
		serverYaw = AutoIgniteRotation.interpolateAngle(phaseStartYaw, phaseTargetYaw, progress);
		serverPitch = AutoIgniteRotation.interpolateLinear(phaseStartPitch, phaseTargetPitch, progress);
		applyRotation(player, serverYaw, serverPitch);
		if (phaseTick < phaseDuration) {
			return;
		}

		ignite(module, client, activeHit);
		beginRestore(player);
	}

	private void beginRestore(LocalPlayer player) {
		if (phase == Phase.RESTORING || phase == Phase.IDLE) {
			return;
		}
		phaseStartYaw = serverYaw;
		phaseStartPitch = serverPitch;
		phaseTargetYaw = cameraFollows ? originalYaw : player.getYRot();
		phaseTargetPitch = cameraFollows ? originalPitch : player.getXRot();
		phaseTick = 0;
		phase = Phase.RESTORING;
	}

	private void advanceRestore(LocalPlayer player) {
		phaseTick++;
		double progress = (double) phaseTick / phaseDuration;
		serverYaw = AutoIgniteRotation.interpolateAngle(phaseStartYaw, phaseTargetYaw, progress);
		serverPitch = AutoIgniteRotation.interpolateLinear(phaseStartPitch, phaseTargetPitch, progress);
		applyRotation(player, serverYaw, serverPitch);
		if (phaseTick < phaseDuration) {
			return;
		}
		targets.removeById(activeTargetId);
		phase = Phase.IDLE;
		activeHit = null;
		AutoIgniteVisualState.clear();
	}

	private void applyRotation(LocalPlayer player, float yaw, float pitch) {
		sendRotation(player, yaw, pitch);
		AutoIgniteVisualState.publish(yaw, pitch, cameraFollows);
		if (cameraFollows) {
			player.setYRot(yaw);
			player.setXRot(pitch);
		}
	}

	private void ignite(AutoIgniteModule module, Minecraft client, BlockHitResult hit) {
		itemAccess.ignite(
			client,
			hit,
			module.itemSource().value(),
			module.itemPriority().value(),
			module.restoreAfterFlint().value()
		);
	}

	private static Optional<BlockHitResult> interactionHit(
		ClientLevel level,
		LocalPlayer player,
		BlockPos pos,
		boolean strict
	) {
		Optional<BlockHitResult> visible = visibleHit(level, player, pos);
		if (strict) {
			return player.isWithinBlockInteractionRange(pos, 0.0) ? visible : Optional.empty();
		}
		if (visible.isPresent()) {
			return visible;
		}
		Vec3 location = Vec3.atBottomCenterOf(pos).add(0.0, 1.0, 0.0);
		return Optional.of(new BlockHitResult(location, Direction.UP, pos, false));
	}

	private static Optional<BlockHitResult> visibleHit(
		ClientLevel level,
		LocalPlayer player,
		BlockPos pos
	) {
		Vec3 eye = player.getEyePosition();
		BlockHitResult hit = level.clip(new ClipContext(
			eye,
			Vec3.atCenterOf(pos),
			ClipContext.Block.OUTLINE,
			ClipContext.Fluid.NONE,
			player
		));
		return hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(pos)
			? Optional.of(hit)
			: Optional.empty();
	}

	private static RotationAngles anglesTo(Vec3 origin, Vec3 target) {
		Vec3 delta = target.subtract(origin);
		double horizontal = Math.hypot(delta.x, delta.z);
		float yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
		float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontal));
		return new RotationAngles(yaw, Math.clamp(pitch, -90.0F, 90.0F));
	}

	private static void sendRotation(LocalPlayer player, float yaw, float pitch) {
		player.connection.send(new ServerboundMovePlayerPacket.Rot(
			yaw,
			pitch,
			player.onGround(),
			player.horizontalCollision
		));
	}

	private enum Phase {
		IDLE,
		TURNING,
		RESTORING
	}

	private record RotationAngles(float yaw, float pitch) {
	}
}
