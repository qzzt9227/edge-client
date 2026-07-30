package io.qzz.iie.module.impl.combat.autoweb;

public final class AutoWebTypes {
	private AutoWebTypes() {
	}

	public enum TargetPriority {
		NEAREST,
		LOWEST_HEALTH,
		CROSSHAIR
	}

	public enum TargetType {
		PLAYER,
		FRIENDLY,
		HOSTILE,
		ALL,
		NEUTRAL
	}

	public enum PlacementPattern {
		FEET,
		FEET_AND_HEAD,
		SURROUND
	}

	public enum HotbarMode {
		SILENT,
		VISIBLE,
		HELD_ONLY
	}

	public enum InventoryMode {
		TEMPORARY_RESTORE,
		MOVE_TO_EMPTY,
		SILENT_SELECTED_RESTORE
	}

	public enum PlacementCadence {
		ONE_PER_ROTATION,
		ALL_AFTER_ROTATION,
		INTERVAL
	}

	public record BlockCell(int x, int y, int z) {
		public BlockCell above() {
			return new BlockCell(x, y + 1, z);
		}

		public BlockCell north() {
			return new BlockCell(x, y, z - 1);
		}

		public BlockCell south() {
			return new BlockCell(x, y, z + 1);
		}

		public BlockCell east() {
			return new BlockCell(x + 1, y, z);
		}

		public BlockCell west() {
			return new BlockCell(x - 1, y, z);
		}
	}

	public record TargetSnapshot(
		int entityId,
		double distanceSquared,
		double health,
		double aimErrorSquared
	) {
	}
}
