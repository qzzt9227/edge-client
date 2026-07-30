package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/**
 * 扫描本轮讲台位置附近的讲台掉落物。
 */
final class LecternDropScanner {
	ItemEntity findNearest(
		ClientLevel level,
		Vec3 playerPosition,
		BlockPos lecternPosition,
		int radius
	) {
		AABB bounds = new AABB(lecternPosition).inflate(radius);
		ArrayList<LecternDropSelection.Candidate<ItemEntity>> candidates = new ArrayList<>();
		for (var entity : level.getEntities(
			(net.minecraft.world.entity.Entity) null,
			bounds,
			entity -> entity instanceof ItemEntity
		)) {
			ItemEntity item = (ItemEntity) entity;
			candidates.add(new LecternDropSelection.Candidate<>(
				item,
				item.position(),
				item.isAlive() && !item.isRemoved(),
				item.getItem().is(Items.LECTERN)
			));
		}
		return LecternDropSelection.nearest(
			candidates,
			playerPosition,
			Vec3.atCenterOf(lecternPosition),
			radius
		);
	}
}
