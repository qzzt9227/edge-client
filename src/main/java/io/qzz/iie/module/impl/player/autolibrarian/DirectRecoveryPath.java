package io.qzz.iie.module.impl.player.autolibrarian;

/**
 * 把下一小段直线路径快照转换为移动动作。
 */
public final class DirectRecoveryPath {
	private DirectRecoveryPath() {
	}

	public static Action decide(Cell cell) {
		if (!cell.hasFloor() || cell.hazard() || Math.abs(cell.floorDelta()) > 1) {
			return Action.BLOCKED;
		}
		if (!cell.crouchingClearance()) {
			return Action.BLOCKED;
		}
		if (cell.floorDelta() > 0) {
			return cell.standingClearance() ? Action.JUMP : Action.BLOCKED;
		}
		if (cell.magmaFloor() || !cell.standingClearance()) {
			return Action.CROUCH;
		}
		return Action.WALK;
	}

	public enum Action {
		WALK,
		CROUCH,
		JUMP,
		BLOCKED
	}

	public record Cell(
		int floorDelta,
		boolean hasFloor,
		boolean standingClearance,
		boolean crouchingClearance,
		boolean magmaFloor,
		boolean hazard
	) {
	}
}
