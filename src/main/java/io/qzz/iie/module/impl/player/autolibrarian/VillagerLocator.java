package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import java.util.Comparator;

/**
 * 定位并校验目标村民。
 */
final class VillagerLocator {
	Villager findNearestUnemployed(ClientLevel level, LocalPlayer player, int radius) {
		return level.getEntitiesOfClass(
			Villager.class,
			player.getBoundingBox().inflate(radius),
			VillagerLocator::isUsableUnemployed
		).stream()
			.filter(villager -> player.distanceToSqr(villager) <= radius * radius)
			.min(Comparator.comparingDouble(player::distanceToSqr))
			.orElse(null);
	}

	Villager findById(ClientLevel level, int entityId) {
		Entity entity = level.getEntity(entityId);
		return entity instanceof Villager villager ? villager : null;
	}

	static boolean isUsableUnemployed(Villager villager) {
		return villager.isAlive()
			&& !villager.isBaby()
			&& villager.getVillagerData().profession().is(VillagerProfession.NONE);
	}
}
