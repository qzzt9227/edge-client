package io.qzz.iie.module.impl.movement.flight;

/**
 * 飞行运动学与伪装策略算法库。
 *
 * <p>提供纯数学的视角平移矢量转换、垂直速度计算与地面伪装判定，
 * 完全不依赖 Minecraft 渲染管线，便于完整单元测试。</p>
 */
public final class FlightPolicy {
	private FlightPolicy() {
	}

	public record Velocity2d(double x, double z) {
		public static final Velocity2d ZERO = new Velocity2d(0.0, 0.0);
	}

	public record Velocity3d(double x, double y, double z) {
		public static final Velocity3d ZERO = new Velocity3d(0.0, 0.0, 0.0);
	}

	/**
	 * 根据视角偏航角（Yaw）、前进/后退、左/右输入以及设定平移速度，计算水平速度矢量。
	 *
	 * @param yaw     玩家视角的偏航角（度）
	 * @param forward 前进输入（正数为前进，负数为后退，0 为无输入）
	 * @param strafe  平移输入（正数为向左，负数为向右，0 为无输入）
	 * @param speed   设定的水平速度（> 0）
	 * @return 计算出的水平 X 和 Z 速度矢量
	 */
	public static Velocity2d calculateHorizontalVelocity(
		float yaw,
		float forward,
		float strafe,
		double speed
	) {
		if (forward == 0.0f && strafe == 0.0f) {
			return Velocity2d.ZERO;
		}

		double rad = Math.toRadians(yaw);
		double sin = Math.sin(rad);
		double cos = Math.cos(rad);

		// Minecraft 中前向运动对应 (-sin(yaw), cos(yaw))，左平移对应 (cos(yaw), sin(yaw))
		double dx = forward * -sin + strafe * cos;
		double dz = forward * cos + strafe * sin;

		double length = Math.sqrt(dx * dx + dz * dz);
		if (length > 1e-6) {
			dx = (dx / length) * speed;
			dz = (dz / length) * speed;
			return new Velocity2d(dx, dz);
		}

		return Velocity2d.ZERO;
	}

	/**
	 * 根据跳跃（上升）与潜行（下降）按键状态计算垂直速度。
	 */
	public static double calculateVerticalVelocity(
		boolean jump,
		boolean sneak,
		double verticalSpeed
	) {
		if (jump && !sneak) {
			return verticalSpeed;
		}
		if (sneak && !jump) {
			return -verticalSpeed;
		}
		return 0.0;
	}

	/**
	 * 综合计算 3D 飞行速度。
	 */
	public static Velocity3d calculateVelocity(
		float yaw,
		float forward,
		float strafe,
		boolean jump,
		boolean sneak,
		double horizontalSpeed,
		double verticalSpeed
	) {
		Velocity2d horizontal = calculateHorizontalVelocity(yaw, forward, strafe, horizontalSpeed);
		double vertical = calculateVerticalVelocity(jump, sneak, verticalSpeed);
		return new Velocity3d(horizontal.x(), vertical, horizontal.z());
	}
}
