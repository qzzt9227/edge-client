package io.qzz.iie.module.impl.input.specialflip;

import net.minecraft.client.player.LocalPlayer;

import java.util.Objects;

/**
 * 特殊翻转的注入桥接：开启后把鼠标水平/垂直位移互换并取反，
 * 即左移看上、右移看下、上移看右、下移看左。
 *
 * <p>与 {@link InvertMouseHooks} 的普通水平/垂直反转互斥；本模块启用时
 * 完全接管转向调用。</p>
 */
public final class SpecialFlipHooks {
	private static volatile SpecialFlipModule module;

	private SpecialFlipHooks() {
	}

	public static void install(SpecialFlipModule installedModule) {
		module = Objects.requireNonNull(installedModule, "installedModule");
	}

	public static boolean shouldApply() {
		SpecialFlipModule current = module;
		return current != null && current.isEnabled();
	}

	/**
	 * 替代 {@code player.turn(yRot, xRot)}。yRot 为水平（yaw）增量、
	 * xRot 为垂直（pitch）增量；特殊翻转把两个轴互换并按需求取反。
	 */
	public static void turn(LocalPlayer player, double yRot, double xRot) {
		player.turn(-xRot, yRot);
	}
}
