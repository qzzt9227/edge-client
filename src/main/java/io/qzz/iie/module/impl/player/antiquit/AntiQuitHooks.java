package io.qzz.iie.module.impl.player.antiquit;

import io.qzz.iie.ui.screen.AntiQuitConfirmScreen;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

/**
 * 防误退桥接与拦截控制。
 *
 * <p>负责在游戏菜单断开连接与 Windows 窗口关闭（X / Alt+F4）时进行拦截，
 * 重置 GLFW 退出状态，并弹出统一的二次确认界面。</p>
 */
public final class AntiQuitHooks {
	private static volatile AntiQuitModule installedModule;
	private static volatile boolean forceQuit;

	private AntiQuitHooks() {
	}

	public static void install(AntiQuitModule module) {
		installedModule = Objects.requireNonNull(module, "module");
		forceQuit = false;
	}

	public static void uninstall(AntiQuitModule module) {
		if (installedModule == module) {
			installedModule = null;
		}
		forceQuit = false;
	}

	public static boolean isEnabled() {
		AntiQuitModule module = installedModule;
		return module != null && module.isEnabled();
	}

	public static boolean shouldConfirmDisconnect() {
		AntiQuitModule module = installedModule;
		return module != null && module.isEnabled() && module.confirmDisconnect().value();
	}

	public static boolean shouldConfirmWindowClose() {
		AntiQuitModule module = installedModule;
		return module != null && module.isEnabled() && module.confirmWindowClose().value();
	}

	public static boolean isForceQuitting() {
		return forceQuit;
	}

	public static void setForceQuitting(boolean quitting) {
		forceQuit = quitting;
	}

	/**
	 * 处理 GLFW 窗口 shouldClose 状态。
	 *
	 * @return 是否允许 Minecraft 继续执行正常退出（{@code minecraft.stop()}）
	 */
	public static boolean handleWindowShouldClose(Minecraft minecraft, Window window) {
		if (!shouldConfirmWindowClose()) {
			return true;
		}
		if (forceQuit) {
			return true;
		}

		// 重置 GLFW 窗口关闭标记，防止窗口被系统或引擎直接关闭
		if (window != null) {
			GLFW.glfwSetWindowShouldClose(window.handle(), false);
		}

		if (minecraft == null) {
			return false;
		}

		if (minecraft.gui != null && minecraft.gui.screen() instanceof AntiQuitConfirmScreen) {
			// 在已打开确认提示时再次点击窗口 X：静默关闭提示窗口并继续游戏
			closeConfirmationScreen(minecraft);
			return false;
		}

		promptWindowClose(minecraft);
		return false;
	}

	/**
	 * 弹出窗口关闭确认提示。
	 */
	public static void promptWindowClose(Minecraft minecraft) {
		if (minecraft == null) {
			return;
		}
		Screen currentScreen = minecraft.gui != null ? minecraft.gui.screen() : null;
		AntiQuitConfirmScreen confirmScreen = new AntiQuitConfirmScreen(
			currentScreen,
			Component.translatable("client.gui.anti_quit.title"),
			Component.translatable("client.gui.anti_quit.message_close"),
			() -> forceClose(minecraft)
		);
		minecraft.setScreenAndShow(confirmScreen);
	}

	/**
	 * 弹出断开连接确认提示。
	 */
	public static void promptDisconnect(Minecraft minecraft, Screen parentScreen, Runnable onConfirm) {
		if (minecraft == null) {
			if (onConfirm != null) {
				onConfirm.run();
			}
			return;
		}
		AntiQuitConfirmScreen confirmScreen = new AntiQuitConfirmScreen(
			parentScreen,
			Component.translatable("client.gui.anti_quit.title"),
			Component.translatable("client.gui.anti_quit.message_disconnect"),
			onConfirm != null ? onConfirm : () -> {}
		);
		minecraft.setScreenAndShow(confirmScreen);
	}

	/**
	 * 用户确认后强制关闭客户端。
	 */
	public static void forceClose(Minecraft minecraft) {
		forceQuit = true;
		if (minecraft != null) {
			minecraft.stop();
		}
	}

	/**
	 * 静默关闭确认窗口。
	 */
	public static void closeConfirmationScreen(Minecraft minecraft) {
		if (minecraft != null && minecraft.gui != null && minecraft.gui.screen() instanceof AntiQuitConfirmScreen confirmScreen) {
			confirmScreen.cancel();
		}
	}
}
