package io.qzz.iie.module.impl.player.packetmine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * 包挖掘核心几何、距离判定与角度平滑插值纯逻辑策略。
 */
public final class PacketMinePolicy {
	private PacketMinePolicy() {
	}

	/**
	 * 计算从方块中心向外等比膨胀的包围盒（进度 0.0 ~ 1.0）。
	 */
	public static PacketMineBox calculateExpandBox(BlockPos pos, float progress) {
		float p = Math.clamp(progress, 0.0f, 1.0f);
		double half = p / 2.0;
		double centerX = pos.getX() + 0.5;
		double centerY = pos.getY() + 0.5;
		double centerZ = pos.getZ() + 0.5;
		return new PacketMineBox(
			centerX - half, centerY - half, centerZ - half,
			centerX + half, centerY + half, centerZ + half
		);
	}

	/**
	 * 计算从方块底部升起的包围盒（进度 0.0 ~ 1.0）。
	 */
	public static PacketMineBox calculateRiseBox(BlockPos pos, float progress) {
		float p = Math.clamp(progress, 0.0f, 1.0f);
		return new PacketMineBox(
			pos.getX(), pos.getY(), pos.getZ(),
			pos.getX() + 1.0, pos.getY() + p, pos.getZ() + 1.0
		);
	}

	/**
	 * 计算完整方块包围盒。
	 */
	public static PacketMineBox calculateFullBox(BlockPos pos) {
		return new PacketMineBox(
			pos.getX(), pos.getY(), pos.getZ(),
			pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0
		);
	}

	/**
	 * 判定玩家眼部位置到目标方块的距离是否在允许范围内。
	 */
	public static boolean isWithinReach(Vec3 eyePos, BlockPos pos, double maxRange) {
		if (eyePos == null || pos == null || maxRange <= 0.0) {
			return false;
		}
		double closestX = Math.clamp(eyePos.x, pos.getX(), pos.getX() + 1.0);
		double closestY = Math.clamp(eyePos.y, pos.getY(), pos.getY() + 1.0);
		double closestZ = Math.clamp(eyePos.z, pos.getZ(), pos.getZ() + 1.0);
		double dx = closestX - eyePos.x;
		double dy = closestY - eyePos.y;
		double dz = closestZ - eyePos.z;
		double distSq = dx * dx + dy * dy + dz * dz;
		return distSq <= (maxRange * maxRange);
	}

	/**
	 * 计算眼部朝向目标点的瞄准角度 (Yaw, Pitch)。
	 */
	public static AimAngles calculateAim(Vec3 eyePos, Vec3 targetPos) {
		double dx = targetPos.x - eyePos.x;
		double dy = targetPos.y - eyePos.y;
		double dz = targetPos.z - eyePos.z;
		double horizontalDist = Math.sqrt(dx * dx + dz * dz);
		float yaw = (float) (Math.toDegrees(Math.atan2(-dx, dz)));
		float pitch = (float) (Math.toDegrees(-Math.atan2(dy, horizontalDist)));
		return new AimAngles(yaw, pitch);
	}

	public record AimAngles(float yaw, float pitch) {
	}

	/**
	 * 沿最短角度路径进行平滑插值。
	 */
	public static float interpolateAngle(float start, float target, float factor) {
		float f = Math.clamp(factor, 0.0f, 1.0f);
		float delta = ((target - start) % 360.0f + 540.0f) % 360.0f - 180.0f;
		return start + delta * f;
	}

	/**
	 * 计算科技感脉冲呼吸颜色（ARGB）。
	 */
	public static int calculatePulseColor(int baseArgb, float progress, long timeMs) {
		int alpha = (baseArgb >> 24) & 0xFF;
		int r = (baseArgb >> 16) & 0xFF;
		int g = (baseArgb >> 8) & 0xFF;
		int b = baseArgb & 0xFF;

		// 呼吸波: 0.75 ~ 1.0
		double wave = 0.875 + 0.125 * Math.sin(timeMs / 120.0);
		int pulseAlpha = (int) Math.clamp(alpha * wave, 0, 255);
		return (pulseAlpha << 24) | (r << 16) | (g << 8) | b;
	}
}
