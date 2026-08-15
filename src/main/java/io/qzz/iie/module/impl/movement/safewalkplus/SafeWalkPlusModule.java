package io.qzz.iie.module.impl.movement.safewalkplus;

import com.mojang.blaze3d.platform.InputConstants;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import org.lwjgl.glfw.GLFW;

/**
 * 安全行走+ 模块：
 * 启用时，如果玩家站在方块边缘导致脚底支撑覆盖率低于设定阈值，强制进行潜行以防止跌落。
 */
public final class SafeWalkPlusModule extends Module {
	private final DoubleSetting coverageThreshold = setting(
		new DoubleSetting(
			"coverage_threshold",
			"client.setting.safe_walk_plus.coverage_threshold",
			60.0,
			1.0,
			100.0,
			1.0
		)
	);

	private final KeybindSetting shortcut = keybind(
		new KeybindSetting("keybind", "client.setting.module_keybind")
	);

	private boolean forcedSneaking;

	public SafeWalkPlusModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "safe_walk_plus"),
			"client.module.safe_walk_plus.name",
			"client.module.safe_walk_plus.description",
			110
		));
	}

	public DoubleSetting coverageThreshold() {
		return coverageThreshold;
	}

	@Override
	protected void onEnable() {
		forcedSneaking = false;
	}

	@Override
	protected void onDisable() {
		if (forcedSneaking) {
			forcedSneaking = false;
			restoreSneakKey();
		}
	}

	@Override
	protected void onClientTick() {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null || client.level == null) {
			return;
		}

		LocalPlayer player = client.player;
		AABB bb = player.getBoundingBox();
		int floorY = (int) Math.floor(bb.minY - 0.05);

		double coverage = SafeWalkPlusPolicy.calculateSupportCoverage(
			bb.minX,
			bb.maxX,
			bb.minZ,
			bb.maxZ,
			(bx, bz) -> SafeWalkPlusPolicy.isSolidAt(client.level, bx, floorY, bz)
		);

		boolean shouldSneak = SafeWalkPlusPolicy.shouldForceSneak(
			coverage,
			coverageThreshold.value(),
			player.onGround()
		);

		if (shouldSneak) {
			if (client.options != null && client.options.keyShift != null) {
				client.options.keyShift.setDown(true);
				forcedSneaking = true;
			}
		} else if (forcedSneaking) {
			forcedSneaking = false;
			restoreSneakKey();
		}
	}

	private void restoreSneakKey() {
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.options != null && client.options.keyShift != null) {
			client.options.keyShift.setDown(isPhysicalDown(client, client.options.keyShift));
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
