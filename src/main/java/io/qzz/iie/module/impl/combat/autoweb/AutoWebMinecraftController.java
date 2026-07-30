package io.qzz.iie.module.impl.combat.autoweb;

import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.BlockCell;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.PlacementCadence;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Optional;

final class AutoWebMinecraftController {
	private static final long NANOS_PER_TICK = 50_000_000L;

	private final AutoWebTargetFinder targetFinder = new AutoWebTargetFinder();
	private final AutoWebItemAccess itemAccess = new AutoWebItemAccess();
	private final ArrayDeque<BlockPos> placements = new ArrayDeque<>();

	private Entity target;
	private BlockPos plannedFeet;
	private RotationState rotation;
	private boolean silentRotationActive;
	private float serverYaw;
	private float serverPitch;
	private long nextPlacementNanos;

	void tick(AutoWebModule module) {
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
		if (!itemAccess.restorePending(client)) {
			return;
		}

		if (!planStillValid(player, module.range().value())) {
			clearPlan(player);
		}
		if (target == null) {
			acquirePlan(module, level, player);
		}
		if (target == null || placements.isEmpty()) {
			return;
		}

		long now = System.nanoTime();
		if (rotation == null && now < nextPlacementNanos) {
			return;
		}
		if (rotation == null && !beginRotation(level, player)) {
			removeInvalidFront(level);
			return;
		}
		if (!advanceRotation(player, module.rotationTicks().value())) {
			return;
		}

		if (module.placementCadence().value() == PlacementCadence.ALL_AFTER_ROTATION) {
			placeAll(module, client, level, player);
		} else {
			placeFront(module, client, level);
			if (module.placementCadence().value() == PlacementCadence.INTERVAL) {
				nextPlacementNanos = now + intervalNanos(module.placementInterval().value());
			}
		}
		rotation = null;
		if (placements.isEmpty()) {
			finishSilentRotation(player);
			target = null;
			plannedFeet = null;
		}
	}

	void reset(Minecraft client) {
		itemAccess.reset(client);
		if (client.player != null) {
			finishSilentRotation(client.player);
		}
		target = null;
		plannedFeet = null;
		placements.clear();
		rotation = null;
		nextPlacementNanos = 0L;
	}

	private void acquirePlan(
		AutoWebModule module,
		ClientLevel level,
		LocalPlayer player
	) {
		target = targetFinder.find(
			level,
			player,
			module.targetType().value(),
			module.targetPriority().value(),
			module.range().value()
		).orElse(null);
		if (target == null) {
			return;
		}

		plannedFeet = target.blockPosition();
		BlockCell feet = new BlockCell(
			plannedFeet.getX(),
			plannedFeet.getY(),
			plannedFeet.getZ()
		);
		for (BlockCell cell : AutoWebPlanner.placementCells(
			feet,
			module.placementPattern().value()
		)) {
			BlockPos pos = new BlockPos(cell.x(), cell.y(), cell.z());
			if (isPlaceable(level, pos)) {
				placements.add(pos);
			}
		}
		if (placements.isEmpty()) {
			target = null;
			plannedFeet = null;
		}
	}

	private boolean planStillValid(LocalPlayer player, double range) {
		return target != null
			&& target.isAlive()
			&& player.distanceToSqr(target) <= range * range
			&& target.blockPosition().equals(plannedFeet);
	}

	private boolean beginRotation(ClientLevel level, LocalPlayer player) {
		BlockPos pos = placements.peek();
		Optional<BlockHitResult> hit = findHit(level, pos);
		if (hit.isEmpty()) {
			return false;
		}
		float startYaw = silentRotationActive ? serverYaw : player.getYRot();
		float startPitch = silentRotationActive ? serverPitch : player.getXRot();
		RotationAngles goal = anglesTo(player.getEyePosition(), hit.get().getLocation());
		rotation = new RotationState(
			startYaw,
			startPitch,
			goal.yaw(),
			goal.pitch(),
			0.0
		);
		return true;
	}

	private boolean advanceRotation(LocalPlayer player, double rotationTicks) {
		double progress = Math.min(1.0, rotation.progress() + 1.0 / rotationTicks);
		serverYaw = AutoWebRotation.interpolateAngle(
			rotation.startYaw(),
			rotation.targetYaw(),
			progress
		);
		serverPitch = (float) (
			rotation.startPitch()
				+ (rotation.targetPitch() - rotation.startPitch()) * progress
		);
		rotation = rotation.withProgress(progress);
		sendSilentRotation(player, serverYaw, serverPitch);
		return progress >= 1.0;
	}

	private void placeFront(
		AutoWebModule module,
		Minecraft client,
		ClientLevel level
	) {
		BlockPos pos = placements.peek();
		PlacementResult result = place(module, client, level, pos);
		if (result != PlacementResult.RETRY) {
			placements.remove();
		}
	}

	private void placeAll(
		AutoWebModule module,
		Minecraft client,
		ClientLevel level,
		LocalPlayer player
	) {
		while (!placements.isEmpty()) {
			BlockPos pos = placements.peek();
			Optional<BlockHitResult> hit = findHit(level, pos);
			if (hit.isPresent()) {
				RotationAngles angles = anglesTo(
					player.getEyePosition(),
					hit.get().getLocation()
				);
				serverYaw = angles.yaw();
				serverPitch = angles.pitch();
				sendSilentRotation(player, serverYaw, serverPitch);
			}
			PlacementResult result = place(module, client, level, pos);
			if (result == PlacementResult.RETRY) {
				return;
			}
			placements.remove();
		}
	}

	private PlacementResult place(
		AutoWebModule module,
		Minecraft client,
		ClientLevel level,
		BlockPos pos
	) {
		BlockState state = level.getBlockState(pos);
		if (state.is(Blocks.COBWEB)) {
			return PlacementResult.DONE;
		}
		Optional<BlockHitResult> hit = findHit(level, pos);
		if (hit.isEmpty()) {
			return PlacementResult.INVALID;
		}
		return itemAccess.place(
			client,
			hit.get(),
			module.hotbarMode().value(),
			module.checkInventory().value(),
			module.inventoryMode().value()
		)
			? PlacementResult.DONE
			: PlacementResult.RETRY;
	}

	private void removeInvalidFront(ClientLevel level) {
		if (!placements.isEmpty() && !isPlaceable(level, placements.peek())) {
			placements.remove();
		}
		if (placements.isEmpty()) {
			target = null;
			plannedFeet = null;
		}
	}

	private void clearPlan(LocalPlayer player) {
		finishSilentRotation(player);
		target = null;
		plannedFeet = null;
		placements.clear();
		rotation = null;
		nextPlacementNanos = 0L;
	}

	private void finishSilentRotation(LocalPlayer player) {
		if (!silentRotationActive) {
			return;
		}
		sendRotationPacket(player, player.getYRot(), player.getXRot());
		if (player instanceof LivingEntity living) {
			living.setYHeadRot(player.getYRot());
			living.setYBodyRot(player.getYRot());
		}
		silentRotationActive = false;
	}

	private void sendSilentRotation(LocalPlayer player, float yaw, float pitch) {
		sendRotationPacket(player, yaw, pitch);
		// 只改模型的头和身体朝向，第一人称相机仍读取玩家自身 yaw/pitch。
		player.setYHeadRot(yaw);
		player.setYBodyRot(yaw);
		silentRotationActive = true;
	}

	private static void sendRotationPacket(LocalPlayer player, float yaw, float pitch) {
		player.connection.send(new ServerboundMovePlayerPacket.Rot(
			yaw,
			pitch,
			player.onGround(),
			player.horizontalCollision
		));
	}

	private static boolean isPlaceable(ClientLevel level, BlockPos pos) {
		return level.getWorldBorder().isWithinBounds(pos)
			&& level.getBlockState(pos).canBeReplaced()
			&& findHit(level, pos).isPresent();
	}

	private static Optional<BlockHitResult> findHit(ClientLevel level, BlockPos target) {
		if (!level.getBlockState(target).canBeReplaced()) {
			return Optional.empty();
		}
		for (Direction offset : Direction.values()) {
			BlockPos support = target.relative(offset);
			Direction face = offset.getOpposite();
			if (!level.getBlockState(support).isFaceSturdy(level, support, face)) {
				continue;
			}
			Vec3 location = Vec3.atCenterOf(support).add(
				face.getStepX() * 0.5,
				face.getStepY() * 0.5,
				face.getStepZ() * 0.5
			);
			return Optional.of(new BlockHitResult(location, face, support, false));
		}
		return Optional.empty();
	}

	private static RotationAngles anglesTo(Vec3 origin, Vec3 target) {
		Vec3 delta = target.subtract(origin);
		double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		float yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
		float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontal));
		return new RotationAngles(yaw, Math.clamp(pitch, -90.0F, 90.0F));
	}

	private static long intervalNanos(double ticks) {
		return Math.max(1L, Math.round(ticks * NANOS_PER_TICK));
	}

	private enum PlacementResult {
		DONE,
		INVALID,
		RETRY
	}

	private record RotationAngles(float yaw, float pitch) {
	}

	private record RotationState(
		float startYaw,
		float startPitch,
		float targetYaw,
		float targetPitch,
		double progress
	) {
		private RotationState withProgress(double requestedProgress) {
			return new RotationState(
				startYaw,
				startPitch,
				targetYaw,
				targetPitch,
				requestedProgress
			);
		}
	}
}
