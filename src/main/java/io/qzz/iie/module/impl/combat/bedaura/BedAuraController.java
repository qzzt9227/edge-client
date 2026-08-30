package io.qzz.iie.module.impl.combat.bedaura;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

final class BedAuraController {
	private final BedAuraPlanner planner = new BedAuraPlanner();
	private final BedAuraItemAccess itemAccess = new BedAuraItemAccess();

	private int placeDelayTimer = 0;
	private int breakDelayTimer = 0;

	void tick(BedAuraModule module) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		ClientLevel level = client.level;

		if (player == null || level == null || client.gameMode == null || !player.isAlive()) {
			reset();
			return;
		}

		if (module.onlyNether().value()) {
			// 在主世界等自然安全维度不触发爆炸床，仅在下界/末地等维度触发
			if (!level.dimension().equals(Level.NETHER) && !level.dimension().equals(Level.END)) {
				return;
			}
		}

		if (placeDelayTimer > 0) {
			placeDelayTimer--;
		}
		if (breakDelayTimer > 0) {
			breakDelayTimer--;
		}

		double range = module.range().value().doubleValue();
		LivingEntity target = planner.findTarget(level, player, range).orElse(null);
		if (target == null) {
			return;
		}

		// 1. 优先检查并引爆周围已放置的床
		var placedBed = planner.findPlacedBed(level, player, target, range);
		if (placedBed.isPresent()) {
			if (breakDelayTimer <= 0) {
				BlockHitResult hit = placedBed.get();
				if (itemAccess.interactWithPlacedBed(client, hit)) {
					breakDelayTimer = (int) module.breakInterval().value().doubleValue();
				}
			}
			return;
		}

		// 2. 无已放置床时，寻找合适位置放置床
		if (placeDelayTimer <= 0) {
			var spot = planner.findPlacementSpot(level, player, target, range);
			if (spot.isPresent()) {
				if (itemAccess.placeBed(client, spot.get().hitResult())) {
					placeDelayTimer = (int) module.placeInterval().value().doubleValue();
				}
			}
		}
	}

	void reset() {
		placeDelayTimer = 0;
		breakDelayTimer = 0;
	}
}
