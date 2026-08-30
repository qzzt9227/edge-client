package io.qzz.iie.module.impl.movement.flight;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 飞行模块。
 *
 * <p>允许玩家在空中三维自由飞行，并可独立调节平移速度与升降速度。
 * 支持地面状态伪装，在未启用飞行的服务端上防止被反作弊踢出与摔落伤害。</p>
 */
public final class FlightModule extends Module {
	private final DoubleSetting horizontalSpeed = setting(
		new DoubleSetting(
			"horizontal_speed",
			"client.setting.flight.horizontal_speed",
			1.0,
			0.1,
			10.0,
			0.1
		)
	);

	private final DoubleSetting verticalSpeed = setting(
		new DoubleSetting(
			"vertical_speed",
			"client.setting.flight.vertical_speed",
			0.8,
			0.1,
			10.0,
			0.1
		)
	);

	private final BooleanSetting spoofGround = setting(
		new BooleanSetting(
			"spoof_ground",
			"client.setting.flight.spoof_ground",
			true
		)
	);

	private final KeybindSetting shortcut = keybind(
		new KeybindSetting(
			"keybind",
			"client.setting.module_keybind"
		)
	);

	public FlightModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "flight"),
			"client.module.flight.name",
			"client.module.flight.description",
			120
		));
	}

	public DoubleSetting horizontalSpeed() {
		return horizontalSpeed;
	}

	public DoubleSetting verticalSpeed() {
		return verticalSpeed;
	}

	public BooleanSetting spoofGround() {
		return spoofGround;
	}

	@Override
	protected void onEnable() {
		FlightHooks.install(this);
	}

	@Override
	protected void onDisable() {
		FlightHooks.uninstall(this);
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.player != null) {
			client.player.setDeltaMovement(Vec3.ZERO);
		}
	}

	@Override
	protected void onClientTick() {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null || client.level == null) {
			return;
		}

		LocalPlayer player = client.player;
		if (player.isPassenger() || player.isSleeping()) {
			return;
		}

		float forward = 0.0f;
		float strafe = 0.0f;
		boolean jump = false;
		boolean sneak = false;

		if (client.options != null) {
			if (client.options.keyUp != null && client.options.keyUp.isDown()) {
				forward += 1.0f;
			}
			if (client.options.keyDown != null && client.options.keyDown.isDown()) {
				forward -= 1.0f;
			}
			if (client.options.keyLeft != null && client.options.keyLeft.isDown()) {
				strafe += 1.0f;
			}
			if (client.options.keyRight != null && client.options.keyRight.isDown()) {
				strafe -= 1.0f;
			}
			if (client.options.keyJump != null && client.options.keyJump.isDown()) {
				jump = true;
			}
			if (client.options.keyShift != null && client.options.keyShift.isDown()) {
				sneak = true;
			}
		}

		FlightPolicy.Velocity3d velocity = FlightPolicy.calculateVelocity(
			player.getYRot(),
			forward,
			strafe,
			jump,
			sneak,
			horizontalSpeed.value(),
			verticalSpeed.value()
		);

		player.setDeltaMovement(new Vec3(velocity.x(), velocity.y(), velocity.z()));
		player.fallDistance = 0.0f;
	}
}
