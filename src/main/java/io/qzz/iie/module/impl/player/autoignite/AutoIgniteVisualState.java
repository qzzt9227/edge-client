package io.qzz.iie.module.impl.player.autoignite;

/** 在客户端 Tick、鼠标输入和实体状态提取之间传递当前模拟角度。 */
public final class AutoIgniteVisualState {
	private static final Snapshot INACTIVE = new Snapshot(false, 0.0F, 0.0F, false);
	private static Snapshot snapshot = INACTIVE;

	private AutoIgniteVisualState() {
	}

	public static void publish(float yaw, float pitch, boolean suppressMouseTurn) {
		snapshot = new Snapshot(true, yaw, pitch, suppressMouseTurn);
	}

	public static Snapshot snapshot() {
		return snapshot;
	}

	public static boolean shouldSuppressMouseTurn() {
		return snapshot.suppressMouseTurn();
	}

	public static void clear() {
		snapshot = INACTIVE;
	}

	public record Snapshot(
		boolean active,
		float yaw,
		float pitch,
		boolean suppressMouseTurn
	) {
	}
}
