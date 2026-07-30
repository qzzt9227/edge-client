package io.qzz.iie.module.impl.player.autolibrarian;

/**
 * 讲台回收阶段的纯状态迁移表。
 */
public final class RecoveryFlow {
	private RecoveryFlow() {
	}

	public static Phase next(
		Phase phase,
		boolean inventoryRestored,
		boolean timerExpired,
		boolean targetAvailable,
		boolean targetReachable,
		boolean atOrigin,
		boolean manualOverride
	) {
		if (manualOverride) {
			return Phase.STOP;
		}
		return switch (phase) {
			case WAIT_BEFORE_RECYCLE -> inventoryRestored
				? Phase.AFTER_BREAK
				: timerExpired ? Phase.WAIT_DROP : phase;
			case WAIT_DROP -> inventoryRestored
				? Phase.RETURN_TO_ORIGIN
				: targetAvailable && targetReachable
					? Phase.MOVE_TO_DROP
					: timerExpired ? Phase.PICKUP_FAILED : phase;
			case MOVE_TO_DROP -> inventoryRestored
				? Phase.RETURN_TO_ORIGIN
				: !targetAvailable || !targetReachable
					? Phase.WAIT_DROP
					: timerExpired ? Phase.PICKUP_FAILED : phase;
			case RETURN_TO_ORIGIN -> atOrigin ? Phase.AFTER_BREAK : phase;
			case AFTER_BREAK, PICKUP_FAILED, STOP -> phase;
		};
	}

	public enum Phase {
		WAIT_BEFORE_RECYCLE,
		WAIT_DROP,
		MOVE_TO_DROP,
		RETURN_TO_ORIGIN,
		AFTER_BREAK,
		PICKUP_FAILED,
		STOP
	}
}
