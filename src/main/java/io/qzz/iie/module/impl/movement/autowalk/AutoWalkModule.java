package io.qzz.iie.module.impl.movement.autowalk;

import com.mojang.blaze3d.platform.InputConstants;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.KeybindSetting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * 自动行走模块：启用后，模拟玩家按住前进键（W）不放。
 */
public final class AutoWalkModule extends Module {
	private final KeybindSetting shortcut = keybind(
		new KeybindSetting("keybind", "client.setting.module_keybind")
	);

	public AutoWalkModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "auto_walk"),
			"client.module.auto_walk.name",
			"client.module.auto_walk.description",
			100
		));
	}

	@Override
	protected void onEnable() {
		pressForwardKey();
	}

	@Override
	protected void onDisable() {
		restoreForwardKey();
	}

	@Override
	protected void onClientTick() {
		pressForwardKey();
	}

	private void pressForwardKey() {
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.player != null && client.options != null && client.options.keyUp != null) {
			client.options.keyUp.setDown(true);
		}
	}

	private void restoreForwardKey() {
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.options != null && client.options.keyUp != null) {
			client.options.keyUp.setDown(isPhysicalDown(client, client.options.keyUp));
		}
	}

	private static boolean isPhysicalDown(Minecraft client, KeyMapping mapping) {
		if (client == null || client.getWindow() == null || mapping == null) {
			return false;
		}
		try {
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
		} catch (Throwable ignored) {
		}
		return false;
	}
}
