package io.qzz.iie.ui.hud;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 实时统计 1 秒滑动时间窗口内的左键与右键点击频次（CPS）。
 */
public final class CpsTracker {
	private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
	private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();

	private CpsTracker() {
	}

	public static synchronized void recordLeftClick() {
		LEFT_CLICKS.addLast(System.currentTimeMillis());
	}

	public static synchronized void recordRightClick() {
		RIGHT_CLICKS.addLast(System.currentTimeMillis());
	}

	public static synchronized int getLeftCps() {
		return calculateCps(LEFT_CLICKS, System.currentTimeMillis());
	}

	public static synchronized int getRightCps() {
		return calculateCps(RIGHT_CLICKS, System.currentTimeMillis());
	}

	public static synchronized void reset() {
		LEFT_CLICKS.clear();
		RIGHT_CLICKS.clear();
	}

	public static int calculateCps(Deque<Long> clicks, long now) {
		long threshold = now - 1000L;
		while (!clicks.isEmpty() && clicks.peekFirst() <= threshold) {
			clicks.pollFirst();
		}
		return clicks.size();
	}
}
