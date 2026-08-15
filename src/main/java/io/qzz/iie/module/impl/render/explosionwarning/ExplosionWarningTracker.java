package io.qzz.iie.module.impl.render.explosionwarning;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Tracks per-entity state transitions so warning messages are emitted once per event. */
public final class ExplosionWarningTracker {
	private final Map<Integer, Observation> observations = new HashMap<>();

	public Set<ExplosionWarningEvent> observe(
		int entityId,
		ExplosionTargetKind kind,
		boolean inRange,
		boolean impending
	) {
		Observation previous = observations.put(
			entityId,
			new Observation(kind, inRange, impending)
		);
		EnumSet<ExplosionWarningEvent> events = EnumSet.noneOf(ExplosionWarningEvent.class);
		if (kind == ExplosionTargetKind.CREEPER && inRange
			&& (previous == null || previous.kind() != kind || !previous.inRange())) {
			events.add(ExplosionWarningEvent.ENTERED_RANGE);
		}
		if (impending && (previous == null || previous.kind() != kind || !previous.impending())) {
			events.add(ExplosionWarningEvent.IMPENDING_EXPLOSION);
		}
		return Set.copyOf(events);
	}

	public void retainOnly(Set<Integer> entityIds) {
		observations.keySet().retainAll(entityIds);
	}

	public void clear() {
		observations.clear();
	}

	private record Observation(ExplosionTargetKind kind, boolean inRange, boolean impending) {
	}
}
