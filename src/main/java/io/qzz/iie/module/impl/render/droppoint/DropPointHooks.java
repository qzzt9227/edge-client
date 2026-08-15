package io.qzz.iie.module.impl.render.droppoint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

/** Minecraft adapter that calculates one landing snapshot while the module is enabled. */
public final class DropPointHooks {
	private static DropPointModule module;
	private static DropPointFootprint.BlockCell previousCell;
	private static int hintsShown;

	private DropPointHooks() {
	}

	public static void install(DropPointModule installedModule) {
		module = Objects.requireNonNull(installedModule, "installedModule");
	}

	public static void tick(Minecraft client) {
		DropPointModule current = module;
		if (current == null || !current.isEnabled() || client.level == null || client.player == null) {
			clear();
			return;
		}

		LandingSurface landing = findLandingSurface(client, client.player);
		if (landing == null) {
			clear();
			return;
		}
		DropPointBlockKind kind = classify(landing.state());
		double distance = client.player.getBoundingBox().minY - landing.topY();
		double totalFallDistance = Math.max(0.0, client.player.fallDistance) + Math.max(0.0, distance);
		double predictedDamage = DropPointFallDamage.calculate(
			totalFallDistance,
			client.player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE),
			client.player.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER),
			kind == DropPointBlockKind.HAY_BALE ? 0.2 : 1.0
		);
		DropPointDecision decision = DropPointPolicy.decide(
			kind,
			distance,
			predictedDamage,
			client.player.getHealth(),
			client.player.isShiftKeyDown()
		);
		if (!decision.visible()) {
			clear();
			return;
		}

		DropPointFootprint.BlockCell cell = landing.cell();
		if (!cell.equals(previousCell)) {
			previousCell = cell;
			hintsShown = 0;
		}
		if (decision.role() == DropPointRole.SCAFFOLD_NEEDS_SNEAKING
			&& hintsShown < (int) Math.round(current.scaffoldHintCount().value())) {
			current.messages().show(net.minecraft.network.chat.Component.translatable(
				"client.message.drop_point.hold_sneak"
			));
			hintsShown++;
		}
		current.setRenderState(new DropPointRenderState(
			cell.x(),
			cell.y(),
			cell.z(),
			colorFor(current, decision.role())
		));
	}

	private static void clear() {
		if (module != null) {
			module.clearRenderState();
		}
		previousCell = null;
		hintsShown = 0;
	}

	private static DropPointColor colorFor(DropPointModule current, DropPointRole role) {
		return switch (role) {
			case SAFE -> current.safeColor();
			case DANGER -> current.dangerColor();
			case SCAFFOLD_SNEAKING -> current.scaffoldSneakingColor();
			case SCAFFOLD_NEEDS_SNEAKING -> current.scaffoldNeedsSneakingColor();
			case DEFAULT -> current.defaultColor();
			case NONE -> throw new IllegalArgumentException("A hidden decision has no render color");
		};
	}

	private static DropPointBlockKind classify(BlockState state) {
		if (state.is(Blocks.SCAFFOLDING)) {
			return DropPointBlockKind.SCAFFOLD;
		}
		if (state.is(Blocks.HAY_BLOCK)) {
			return DropPointBlockKind.HAY_BALE;
		}
		if (state.is(Blocks.LAVA) || state.getFluidState().getType() == net.minecraft.world.level.material.Fluids.LAVA) {
			return DropPointBlockKind.LAVA;
		}
		if (state.is(Blocks.WATER)
			|| state.getFluidState().getType() == net.minecraft.world.level.material.Fluids.WATER
			|| state.is(Blocks.SLIME_BLOCK)
			|| state.is(Blocks.SWEET_BERRY_BUSH)
			|| state.is(Blocks.POWDER_SNOW)) {
			return DropPointBlockKind.SAFE;
		}
		return DropPointBlockKind.NORMAL;
	}

	private static LandingSurface findLandingSurface(Minecraft client, LocalPlayer player) {
		AABB box = player.getBoundingBox();
		int firstX = (int) Math.floor(box.minX);
		int lastX = (int) Math.ceil(box.maxX) - 1;
		int firstZ = (int) Math.floor(box.minZ);
		int lastZ = (int) Math.ceil(box.maxZ) - 1;
		int highestLoadedSurface = client.level.getMinY();
		for (int x = firstX; x <= lastX; x++) {
			for (int z = firstZ; z <= lastZ; z++) {
				highestLoadedSurface = Math.max(
					highestLoadedSurface,
					client.level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z)
				);
			}
		}
		int startY = Math.min(
			(int) Math.floor(box.minY - 1.0E-7),
			highestLoadedSurface
		);
		for (int y = startY; y >= client.level.getMinY(); y--) {
			LandingSurface best = null;
			for (int x = firstX; x <= lastX; x++) {
				for (int z = firstZ; z <= lastZ; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					if (!client.level.isLoaded(pos)) {
						continue;
					}
					BlockState state = client.level.getBlockState(pos);
					double top = topSurface(client, player, pos, state, box);
					if (!Double.isFinite(top) || top >= box.minY) {
						continue;
					}
					double area = overlapArea(box, x, z);
					LandingSurface candidate = new LandingSurface(
						new DropPointFootprint.BlockCell(x, y, z, area),
						state,
						top
					);
					if (area <= 0.0 || best == null || candidate.betterThan(best)) {
						best = candidate;
					}
				}
			}
			if (best != null) {
				return best;
			}
		}
		return null;
	}

	private static double topSurface(
		Minecraft client,
		LocalPlayer player,
		BlockPos pos,
		BlockState state,
		AABB playerBox
	) {
		if (state.getFluidState().getType() == net.minecraft.world.level.material.Fluids.WATER
			|| state.getFluidState().getType() == net.minecraft.world.level.material.Fluids.LAVA) {
			return pos.getY() + state.getFluidState().getHeight(client.level, pos);
		}
		VoxelShape shape = state.getCollisionShape(client.level, pos, CollisionContext.of(player));
		double best = Double.NEGATIVE_INFINITY;
		for (var bounds : shape.toAabbs()) {
			if (overlapArea(playerBox, pos.getX() + bounds.minX, pos.getZ() + bounds.minZ,
				pos.getX() + bounds.maxX, pos.getZ() + bounds.maxZ) > 0.0) {
				best = Math.max(best, pos.getY() + bounds.maxY);
			}
		}
		return best;
	}

	private static double overlapArea(AABB box, int x, int z) {
		return overlapArea(box, x, z, x + 1.0, z + 1.0);
	}

	private static double overlapArea(AABB box, double minX, double minZ, double maxX, double maxZ) {
		return Math.max(0.0, Math.min(box.maxX, maxX) - Math.max(box.minX, minX))
			* Math.max(0.0, Math.min(box.maxZ, maxZ) - Math.max(box.minZ, minZ));
	}

	private record LandingSurface(
		DropPointFootprint.BlockCell cell,
		BlockState state,
		double topY
	) {
		private boolean betterThan(LandingSurface other) {
			return topY > other.topY
				|| topY == other.topY && cell.area() > other.cell.area()
				|| topY == other.topY && cell.area() == other.cell.area()
					&& (cell.x() < other.cell.x() || cell.x() == other.cell.x() && cell.z() < other.cell.z());
		}
	}
}
