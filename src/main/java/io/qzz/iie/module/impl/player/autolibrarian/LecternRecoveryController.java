package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * 独立编排讲台掉落扫描、直线拾取和返回。
 */
final class LecternRecoveryController {
	private static final int MAX_STALLED_TICKS = 30;
	private static final int MIN_PICKUP_TIMEOUT_TICKS = 100;
	private static final int PICKUP_TIMEOUT_TICKS_PER_BLOCK = 30;

	private final Minecraft client;
	private final LecternDropScanner drops = new LecternDropScanner();
	private final RecoveryPathProbe path = new RecoveryPathProbe();
	private final LecternRecoverySession session = new LecternRecoverySession();
	private final RecoveryMovementController movement;
	private BlockPos lecternPosition;
	private AutomationState state;

	LecternRecoveryController(Minecraft client) {
		this.client = client;
		movement = new RecoveryMovementController(client);
	}

	void begin(
		int expectedLecternCount,
		BlockPos lecternPosition,
		AutoLibrarianSettings settings
	) {
		this.lecternPosition = lecternPosition.immutable();
		session.begin(
			expectedLecternCount,
			client.player.position(),
			client.player.getYRot(),
			client.player.getXRot()
		);
		session.timer = settings.beforeRecycleTicks();
		state = AutomationState.WAIT_BEFORE_RECYCLE;
	}

	Outcome tick(AutoLibrarianSettings settings) {
		return switch (state) {
			case WAIT_BEFORE_RECYCLE -> tickBeforeRecycle(settings);
			case WAIT_RECYCLE_DROP -> tickDropSearch(settings);
			case MOVING_TO_RECYCLE_DROP -> tickMovingToDrop(settings);
			case RETURNING_FROM_RECYCLE -> tickReturning(settings);
			default -> Outcome.RUNNING;
		};
	}

	AutomationState state() {
		return state;
	}

	boolean isActive() {
		return state == AutomationState.WAIT_BEFORE_RECYCLE
			|| state == AutomationState.WAIT_RECYCLE_DROP
			|| state == AutomationState.MOVING_TO_RECYCLE_DROP
			|| state == AutomationState.RETURNING_FROM_RECYCLE;
	}

	void cancel() {
		movement.release();
		state = null;
		lecternPosition = null;
		session.clear();
	}

	private Outcome tickBeforeRecycle(AutoLibrarianSettings settings) {
		RecoveryFlow.Phase next = RecoveryFlow.next(
			RecoveryFlow.Phase.WAIT_BEFORE_RECYCLE,
			inventoryRestored(),
			--session.timer <= 0,
			false,
			false,
			true,
			false
		);
		if (next == RecoveryFlow.Phase.AFTER_BREAK) {
			return complete();
		}
		if (next == RecoveryFlow.Phase.WAIT_DROP) {
			state = AutomationState.WAIT_RECYCLE_DROP;
			session.timer = settings.recycleSearchTimeoutTicks();
		}
		return Outcome.RUNNING;
	}

	private Outcome tickDropSearch(AutoLibrarianSettings settings) {
		session.target = drops.findNearest(
			client.level,
			client.player.position(),
			lecternPosition,
			settings.recycleRadius()
		);
		DirectRecoveryPath.Action action = session.target == null
			? DirectRecoveryPath.Action.BLOCKED
			: path.probe(
				client.level,
				client.player,
				session.target.position(),
				lecternPosition,
				settings.recycleRadius()
			);
		boolean pickupConfirmed = inventoryRestored();
		boolean timerExpired = !pickupConfirmed && --session.timer <= 0;
		RecoveryFlow.Phase next = RecoveryFlow.next(
			RecoveryFlow.Phase.WAIT_DROP,
			pickupConfirmed,
			timerExpired,
			session.target != null,
			action != DirectRecoveryPath.Action.BLOCKED,
			false,
			false
		);
		if (next == RecoveryFlow.Phase.PICKUP_FAILED) {
			return failPickup();
		}
		if (next == RecoveryFlow.Phase.RETURN_TO_ORIGIN) {
			beginReturn(settings);
		} else if (next == RecoveryFlow.Phase.MOVE_TO_DROP) {
			beginMoveToDrop(settings);
		}
		return Outcome.RUNNING;
	}

	private Outcome tickMovingToDrop(AutoLibrarianSettings settings) {
		boolean manualOverride = movement.physicalInputActive();
		boolean pickupConfirmed = inventoryRestored();
		boolean validTarget = validTarget(settings);
		DirectRecoveryPath.Action action = validTarget
			? path.probe(
				client.level,
				client.player,
				session.target.position(),
				lecternPosition,
				settings.recycleRadius()
			)
			: DirectRecoveryPath.Action.BLOCKED;
		boolean timerExpired = !pickupConfirmed && --session.timer <= 0;
		RecoveryFlow.Phase next = RecoveryFlow.next(
			RecoveryFlow.Phase.MOVE_TO_DROP,
			pickupConfirmed,
			timerExpired,
			validTarget,
			action != DirectRecoveryPath.Action.BLOCKED,
			false,
			manualOverride
		);
		if (next == RecoveryFlow.Phase.STOP) {
			return Outcome.MANUAL_OVERRIDE;
		}
		if (next == RecoveryFlow.Phase.PICKUP_FAILED) {
			return failPickup();
		}
		if (next == RecoveryFlow.Phase.RETURN_TO_ORIGIN) {
			beginReturn(settings);
			return Outcome.RUNNING;
		}
		if (next == RecoveryFlow.Phase.WAIT_DROP) {
			resumeDropSearch(settings);
			return Outcome.RUNNING;
		}
		if (RecoveryDecision.reachedPickupArea(
			session.target.position(),
			client.player.position()
		)) {
			movement.release();
			session.stalledTicks = 0;
			session.lastPosition = client.player.position();
			return Outcome.RUNNING;
		}
		RecoveryMovementController.MoveResult result =
			movement.moveToward(session.target.position(), action);
		if (result == RecoveryMovementController.MoveResult.MANUAL_OVERRIDE) {
			return Outcome.MANUAL_OVERRIDE;
		}
		if (result == RecoveryMovementController.MoveResult.BLOCKED || updateStall()) {
			return failPickup();
		}
		return Outcome.RUNNING;
	}

	private Outcome tickReturning(AutoLibrarianSettings settings) {
		boolean manualOverride = movement.physicalInputActive();
		RecoveryFlow.Phase next = RecoveryFlow.next(
			RecoveryFlow.Phase.RETURN_TO_ORIGIN,
			false,
			false,
			false,
			false,
			RecoveryDecision.reached(session.origin, client.player.position()),
			manualOverride
		);
		if (next == RecoveryFlow.Phase.STOP) {
			return Outcome.MANUAL_OVERRIDE;
		}
		if (next == RecoveryFlow.Phase.AFTER_BREAK) {
			return complete();
		}
		DirectRecoveryPath.Action action = path.probe(
			client.level,
			client.player,
			session.origin,
			lecternPosition,
			settings.recycleRadius()
		);
		RecoveryMovementController.MoveResult result =
			movement.moveToward(session.origin, action);
		if (result == RecoveryMovementController.MoveResult.MANUAL_OVERRIDE) {
			return Outcome.MANUAL_OVERRIDE;
		}
		if (result == RecoveryMovementController.MoveResult.BLOCKED
			|| updateStall()
			|| --session.timer <= 0) {
			return Outcome.RETURN_FAILED;
		}
		return Outcome.RUNNING;
	}

	private void beginReturn(AutoLibrarianSettings settings) {
		movement.release();
		session.target = null;
		session.stalledTicks = 0;
		session.lastPosition = client.player.position();
		session.timer = Math.max(100, settings.recycleRadius() * 30);
		state = AutomationState.RETURNING_FROM_RECYCLE;
	}

	private void beginMoveToDrop(AutoLibrarianSettings settings) {
		session.stalledTicks = 0;
		session.lastPosition = client.player.position();
		session.timer = Math.max(
			MIN_PICKUP_TIMEOUT_TICKS,
			Math.max(
				settings.recycleSearchTimeoutTicks(),
				settings.recycleRadius() * PICKUP_TIMEOUT_TICKS_PER_BLOCK
			)
		);
		state = AutomationState.MOVING_TO_RECYCLE_DROP;
	}

	private void resumeDropSearch(AutoLibrarianSettings settings) {
		movement.release();
		session.target = null;
		session.stalledTicks = 0;
		session.lastPosition = client.player.position();
		session.timer = settings.recycleSearchTimeoutTicks();
		state = AutomationState.WAIT_RECYCLE_DROP;
	}

	private Outcome failPickup() {
		movement.release();
		return Outcome.PICKUP_FAILED;
	}

	private Outcome complete() {
		movement.release();
		client.player.setYRot(session.originYaw);
		client.player.setXRot(session.originPitch);
		state = null;
		lecternPosition = null;
		session.clear();
		return Outcome.AFTER_BREAK;
	}

	private boolean inventoryRestored() {
		return RecoveryDecision.inventoryRestored(
			session.expectedLecternCount,
			LecternInventoryCounter.count(client.player)
		);
	}

	private boolean validTarget(AutoLibrarianSettings settings) {
		if (session.target == null
			|| !session.target.isAlive()
			|| session.target.isRemoved()
			|| !session.target.getItem().is(Items.LECTERN)) {
			return false;
		}
		double radius = settings.recycleRadius();
		return session.target.position().distanceToSqr(Vec3.atCenterOf(lecternPosition))
			<= radius * radius;
	}

	private boolean updateStall() {
		Vec3 current = client.player.position();
		if (session.lastPosition != null
			&& current.distanceToSqr(session.lastPosition) < 0.0004) {
			session.stalledTicks++;
		} else {
			session.stalledTicks = 0;
		}
		session.lastPosition = current;
		return session.stalledTicks >= MAX_STALLED_TICKS;
	}

	enum Outcome {
		RUNNING,
		AFTER_BREAK,
		MANUAL_OVERRIDE,
		PICKUP_FAILED,
		RETURN_FAILED
	}
}
