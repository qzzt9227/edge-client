package io.qzz.iie.module.impl.player.autolibrarian;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * 直接读取真实绑定，避免把模块临时按下的按键视为玩家输入。
 */
final class PhysicalMovementInput {
	private PhysicalMovementInput() {
	}

	static boolean isDown(Minecraft client, KeyMapping mapping) {
		InputConstants.Key key = InputConstants.getKey(mapping.saveString());
		if (key.getType() == InputConstants.Type.MOUSE) {
			return GLFW.glfwGetMouseButton(
				client.getWindow().handle(),
				key.getValue()
			) == GLFW.GLFW_PRESS;
		}
		if (key.getType() == InputConstants.Type.KEYSYM) {
			return GLFW.glfwGetKey(
				client.getWindow().handle(),
				key.getValue()
			) == GLFW.GLFW_PRESS;
		}
		return false;
	}

	static boolean anyMovementDown(Minecraft client) {
		return isDown(client, client.options.keyUp)
			|| isDown(client, client.options.keyDown)
			|| isDown(client, client.options.keyLeft)
			|| isDown(client, client.options.keyRight)
			|| isDown(client, client.options.keyJump)
			|| isDown(client, client.options.keyShift);
	}
}
