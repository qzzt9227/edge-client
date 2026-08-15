package io.qzz.iie.module.impl.player.autoignite;

import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.TargetHandling;

import java.util.ArrayDeque;
import java.util.OptionalLong;

/** 保存已经由本地放置确认的 TNT 位置。 */
public final class AutoIgniteTargetQueue {
	private final ArrayDeque<Target> targets = new ArrayDeque<>();
	private long nextTargetId;

	public void offer(long packedBlockPos, TargetHandling handling) {
		if (handling == TargetHandling.LATEST_ONLY) {
			targets.clear();
		}
		targets.addLast(new Target(++nextTargetId, packedBlockPos));
	}

	public OptionalLong peek() {
		Target target = targets.peekFirst();
		return target == null ? OptionalLong.empty() : OptionalLong.of(target.packedBlockPos());
	}

	public OptionalLong peekId() {
		Target target = targets.peekFirst();
		return target == null ? OptionalLong.empty() : OptionalLong.of(target.id());
	}

	public void removeCurrent() {
		targets.pollFirst();
	}

	public void removeById(long targetId) {
		targets.removeIf(target -> target.id() == targetId);
	}

	public int size() {
		return targets.size();
	}

	public void clear() {
		targets.clear();
	}

	private record Target(long id, long packedBlockPos) {
	}
}
