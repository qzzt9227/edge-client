package io.qzz.iie.module.impl.player.autolibrarian;

/**
 * 对每个新打开或替换的对象只报告一次。
 */
final class ScreenOpenTracker<T> {
	private T previous;

	boolean observe(T current) {
		boolean opened = current != null && current != previous;
		previous = current;
		return opened;
	}
}
