package io.qzz.iie.module.impl.player.packetmine;

import net.minecraft.core.BlockPos;

/**
 * 跨线程与渲染帧的包挖掘视觉快照，用于第三人称转向与世界渲染。
 */
public final class PacketMineVisualState {
	private static volatile Snapshot snapshot = Snapshot.INERT;

	private PacketMineVisualState() {
	}

	public static void update(Snapshot newSnapshot) {
		snapshot = newSnapshot == null ? Snapshot.INERT : newSnapshot;
	}

	public static Snapshot snapshot() {
		return snapshot;
	}

	public record Snapshot(
		boolean rotationActive,
		float yaw,
		float pitch,
		boolean miningActive,
		BlockPos targetPos,
		float progress,
		PacketMineRenderStyle renderStyle,
		int fillColor,
		int lineColor
	) {
		public static final Snapshot INERT = new Snapshot(
			false, 0.0f, 0.0f, false, null, 0.0f,
			PacketMineRenderStyle.EXPAND, 0, 0
		);
	}
}
