package io.qzz.iie.module.impl.render.freelook;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * 自由视角在 Minecraft 渲染、相机与输入管线中的静态适配器。
 */
public final class FreeLookHooks {
	private static FreeLookModule installedModule;
	private static CameraType previousCameraType;

	private FreeLookHooks() {
	}

	public static void install(FreeLookModule module) {
		installedModule = module;
	}

	public static boolean isActive() {
		return installedModule != null && installedModule.isEnabled() && installedModule.policy().isActive();
	}

	public static boolean shouldInterceptMouseTurn() {
		return isActive();
	}

	public static void turn(double deltaYaw, double deltaPitch) {
		if (installedModule != null && installedModule.isEnabled()) {
			installedModule.policy().turn(
				deltaYaw,
				deltaPitch,
				installedModule.invertYaw().value(),
				installedModule.invertPitch().value()
			);
		}
	}

	public static float getYaw(float partialTicks) {
		if (installedModule != null && installedModule.isEnabled()) {
			return installedModule.policy().renderYaw(partialTicks);
		}
		Minecraft mc = Minecraft.getInstance();
		return mc.player != null ? mc.player.getViewYRot(partialTicks) : 0.0f;
	}

	public static float getPitch(float partialTicks) {
		if (installedModule != null && installedModule.isEnabled()) {
			return installedModule.policy().renderPitch(partialTicks);
		}
		Minecraft mc = Minecraft.getInstance();
		return mc.player != null ? mc.player.getViewXRot(partialTicks) : 0.0f;
	}

	static void onModuleEnabled(FreeLookModule module) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player != null) {
			previousCameraType = mc.options.getCameraType();
			if (module.autoThirdPerson().value() && previousCameraType.isFirstPerson()) {
				mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			}
			module.policy().activate(player.getYRot(), player.getXRot());
		}
	}

	static void onModuleDisabled(FreeLookModule module) {
		Minecraft mc = Minecraft.getInstance();
		if (module.autoThirdPerson().value() && previousCameraType != null) {
			mc.options.setCameraType(previousCameraType);
			previousCameraType = null;
		}
		module.policy().deactivate();
	}

	static void onClientTick(FreeLookModule module) {
		module.policy().tick();

		if (module.holdMode().value()) {
			module.keybind().ifPresent(keybind -> {
				int code = keybind.value().keyCode();
				if (code > 0) {
					boolean pressed = InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), code);
					if (!pressed) {
						module.disableModule();
					}
				}
			});
		}
	}
}
