package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 保存单轮讲台回收所需的起点、目标和超时状态。
 */
final class LecternRecoverySession {
	int expectedLecternCount;
	Vec3 origin;
	float originYaw;
	float originPitch;
	ItemEntity target;
	int timer;
	int stalledTicks;
	Vec3 lastPosition;

	void begin(int expectedLecternCount, Vec3 origin, float yaw, float pitch) {
		this.expectedLecternCount = expectedLecternCount;
		this.origin = origin;
		originYaw = yaw;
		originPitch = pitch;
		target = null;
		timer = 0;
		stalledTicks = 0;
		lastPosition = origin;
	}

	void clear() {
		expectedLecternCount = 0;
		origin = null;
		target = null;
		timer = 0;
		stalledTicks = 0;
		lastPosition = null;
	}
}
