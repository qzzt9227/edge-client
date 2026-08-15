package io.qzz.iie.module.impl.render.explosionwarning;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Minecraft adapter for entity detection, event notifications, and immutable render snapshots. */
public final class ExplosionWarningHooks {
	private static ExplosionWarningModule module;
	private static final ExplosionWarningTracker TRACKER = new ExplosionWarningTracker();

	private ExplosionWarningHooks() {
	}

	public static void install(ExplosionWarningModule installedModule) {
		module = installedModule;
		TRACKER.clear();
	}

	public static void tick(Minecraft client) {
		ExplosionWarningModule current = module;
		if (current == null || !current.isEnabled() || client.level == null || client.player == null) {
			if (current != null) {
				current.setRenderStates(List.of());
			}
			TRACKER.clear();
			return;
		}

		double centerX = client.player.getX();
		double centerZ = client.player.getZ();
		double radius = current.radius().value();
		double radiusSquared = radius * radius;
		List<ExplosionWarningRenderState> states = new ArrayList<>();
		Set<Integer> seenIds = new HashSet<>();
		AABB searchBox = new AABB(
			centerX - radius,
			client.player.getY() - radius,
			centerZ - radius,
			centerX + radius,
			client.player.getY() + radius,
			centerZ + radius
		);
		for (Entity entity : client.level.getEntities(
			(Entity) null,
			searchBox,
			candidate -> candidate instanceof PrimedTnt || candidate instanceof Creeper
		)) {
			ExplosionTargetKind kind;
			boolean impending;
			if (entity instanceof PrimedTnt tnt) {
				kind = ExplosionTargetKind.TNT;
				impending = true;
			} else if (entity instanceof Creeper creeper) {
				kind = ExplosionTargetKind.CREEPER;
				impending = creeper.isIgnited() || creeper.getSwellDir() > 0 || creeper.getSwelling(0.0F) > 0.0F;
			} else {
				continue;
			}

			int entityId = entity.getId();
			seenIds.add(entityId);
			double dx = entity.getX() - centerX;
			double dz = entity.getZ() - centerZ;
			boolean inRange = dx * dx + dz * dz <= radiusSquared;
			Set<ExplosionWarningEvent> events = TRACKER.observe(entityId, kind, inRange, inRange && impending);
			if (inRange && kind == ExplosionTargetKind.CREEPER && current.creeperRangeMessage().value()
				&& events.contains(ExplosionWarningEvent.ENTERED_RANGE)) {
				show(current, "client.message.explosion_warning.creeper_range");
			}
			if (inRange && current.impendingMessage().value()
				&& events.contains(ExplosionWarningEvent.IMPENDING_EXPLOSION)) {
				show(current, "client.message.explosion_warning.impending");
			}
			if (!inRange || !impending) {
				continue;
			}

			AABB bounds = entity.getBoundingBox();
			double targetY = kind == ExplosionTargetKind.TNT
				? bounds.maxY + 0.15
				: bounds.minY + bounds.getYsize() * 0.5;
			states.add(new ExplosionWarningRenderState(
				entityId,
				kind,
				entity.getX(),
				entity.getY(),
				entity.getZ(),
				targetY,
				bounds.getXsize() * 0.5,
				bounds.getZsize() * 0.5,
				entity instanceof PrimedTnt tnt ? tnt.getFuse() : -1,
				entity instanceof Creeper creeper ? creeper.getSwelling(0.0F) : 0.0F
			));
		}
		TRACKER.retainOnly(seenIds);
		current.setRenderStates(states);
	}

	private static void show(ExplosionWarningModule current, String translationKey) {
		current.messages().show(
			Component.translatable(translationKey)
				.withStyle(style -> style.withColor(current.messageColor()))
		);
	}
}
