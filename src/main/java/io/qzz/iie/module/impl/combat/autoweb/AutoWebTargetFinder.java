package io.qzz.iie.module.impl.combat.autoweb;

import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.TargetPriority;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.TargetSnapshot;
import io.qzz.iie.module.impl.combat.autoweb.AutoWebTypes.TargetType;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class AutoWebTargetFinder {
	Optional<Entity> find(
		ClientLevel level,
		LocalPlayer player,
		TargetType targetType,
		TargetPriority priority,
		double range
	) {
		double maximumDistanceSquared = range * range;
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		List<Candidate> candidates = new ArrayList<>();

		for (Entity entity : level.entitiesForRendering()) {
			if (!isEligible(entity, player, targetType)) {
				continue;
			}
			double distanceSquared = player.distanceToSqr(entity);
			if (distanceSquared > maximumDistanceSquared) {
				continue;
			}

			Vec3 direction = entity.getBoundingBox().getCenter().subtract(eye);
			double aimError = direction.lengthSqr() < 1.0E-9
				? 0.0
				: 1.0 - Math.clamp(look.dot(direction.normalize()), -1.0, 1.0);
			double health = entity instanceof LivingEntity living
				? living.getHealth()
				: Double.POSITIVE_INFINITY;
			candidates.add(new Candidate(
				entity,
				new TargetSnapshot(entity.getId(), distanceSquared, health, aimError)
			));
		}

		List<TargetSnapshot> snapshots = candidates.stream()
			.map(Candidate::snapshot)
			.toList();
		return AutoWebPlanner.selectTarget(snapshots, priority)
			.flatMap(selected -> candidates.stream()
				.filter(candidate -> candidate.snapshot().entityId() == selected.entityId())
				.map(Candidate::entity)
				.findFirst());
	}

	private static boolean isEligible(
		Entity entity,
		LocalPlayer player,
		TargetType targetType
	) {
		if (entity == player || !entity.isAlive() || entity.isSpectator()) {
			return false;
		}
		MobCategory category = entity.getType().getCategory();
		return switch (targetType) {
			case PLAYER -> entity instanceof Player;
			case HOSTILE -> category == MobCategory.MONSTER
				&& !(entity instanceof NeutralMob);
			case FRIENDLY -> isFriendly(category)
				&& !(entity instanceof NeutralMob);
			case NEUTRAL -> entity instanceof NeutralMob;
			case ALL -> true;
		};
	}

	private static boolean isFriendly(MobCategory category) {
		return switch (category) {
			case CREATURE, AMBIENT, AXOLOTLS, UNDERGROUND_WATER_CREATURE,
				WATER_CREATURE, WATER_AMBIENT -> true;
			default -> false;
		};
	}

	private record Candidate(Entity entity, TargetSnapshot snapshot) {
	}
}
