package io.qzz.iie.module.impl.player.autolibrarian;

/**
 * 处理精准等级与任意等级之间的加减循环。
 */
public final class TargetLevelSelection {
	private TargetLevelSelection() {
	}

	public static State adjust(State current, int maximumLevel, int direction) {
		int maximum = Math.max(1, maximumLevel);
		if (current.anyLevel()) {
			return direction >= 0 ? new State(1, false) : new State(maximum, false);
		}
		int level = Math.clamp(current.level(), 1, maximum);
		if (direction >= 0) {
			return level >= maximum
				? new State(maximum, true)
				: new State(level + 1, false);
		}
		return level <= 1
			? new State(1, true)
			: new State(level - 1, false);
	}

	public record State(int level, boolean anyLevel) {
	}
}
