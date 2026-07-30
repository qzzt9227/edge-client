package io.qzz.iie.module.impl.combat.autoweb;

import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.BlockCell;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.PlacementPattern;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.TargetPriority;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.TargetSnapshot;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AutoWebPlanner {
	private AutoWebPlanner() {
	}

	public static List<BlockCell> placementCells(
		BlockCell feet,
		PlacementPattern pattern
	) {
		Objects.requireNonNull(feet, "feet");
		return switch (Objects.requireNonNull(pattern, "pattern")) {
			case FEET -> List.of(feet);
			case FEET_AND_HEAD -> List.of(feet, feet.above());
			case SURROUND -> List.of(
				feet,
				feet.above(),
				feet.north(),
				feet.south(),
				feet.east(),
				feet.west()
			);
		};
	}

	public static Optional<TargetSnapshot> selectTarget(
		List<TargetSnapshot> candidates,
		TargetPriority priority
	) {
		Objects.requireNonNull(candidates, "candidates");
		Comparator<TargetSnapshot> comparator = switch (
			Objects.requireNonNull(priority, "priority")
		) {
			case NEAREST -> Comparator.comparingDouble(TargetSnapshot::distanceSquared);
			case LOWEST_HEALTH -> Comparator.comparingDouble(TargetSnapshot::health);
			case CROSSHAIR -> Comparator.comparingDouble(TargetSnapshot::aimErrorSquared);
		};
		return candidates.stream()
			.min(comparator.thenComparingInt(TargetSnapshot::entityId));
	}
}
