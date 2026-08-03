package io.qzz.iie.module.impl.input.invertmouse;

import net.minecraft.client.player.LocalPlayer;

import java.util.Objects;

/**
 * 将版本敏感的 {@code LocalPlayer.turn} 注入面限制在一个极小的桥接类中。
 *
 * <p>Fabric 没有鼠标视角旋转的合适钩子，因此必须注入
 * {@code MouseHandler.turnPlayer} 中的转向调用；本类负责把“是否启用”
 * 的状态判断与角度反转从 Mixin 中隔离出来。水平模块反转 yaw，
 * 垂直模块反转 pitch，两者相互独立。</p>
 */
public final class InvertMouseHooks {
	private static volatile InvertMouseModule horizontalModule;
	private static volatile InvertMousePitchModule verticalModule;

	private InvertMouseHooks() {
	}

	public static void install(InvertMouseModule installedModule) {
		horizontalModule = Objects.requireNonNull(installedModule, "installedModule");
	}

	public static void installPitch(InvertMousePitchModule installedModule) {
		verticalModule = Objects.requireNonNull(installedModule, "installedModule");
	}

	/**
	 * 判断鼠标水平视角（yaw）是否应被反转。Mixin 之外也用于纯逻辑测试。
	 */
	public static boolean shouldInvertHorizontal() {
		InvertMouseModule current = horizontalModule;
		return current != null && current.isEnabled();
	}

	/**
	 * 判断鼠标垂直视角（pitch）是否应被反转。Mixin 之外也用于纯逻辑测试。
	 */
	public static boolean shouldInvertVertical() {
		InvertMousePitchModule current = verticalModule;
		return current != null && current.isEnabled();
	}

	/**
	 * 替代 {@code player.turn(yRot, xRot)}。yRot 是水平（yaw）增量，
	 * xRot 是垂直（pitch）增量；各自在对应模块启用时取反。
	 */
	public static void turn(LocalPlayer player, double yRot, double xRot) {
		if (shouldInvertHorizontal()) {
			yRot = -yRot;
		}
		if (shouldInvertVertical()) {
			xRot = -xRot;
		}
		player.turn(yRot, xRot);
	}
}
