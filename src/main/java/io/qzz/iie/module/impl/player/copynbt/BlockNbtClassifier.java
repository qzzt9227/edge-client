package io.qzz.iie.module.impl.player.copynbt;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

/**
 * 方块实体分类器，用于将方块映射到对应的 NBT 复制子选项分类。
 */
public final class BlockNbtClassifier {
	private BlockNbtClassifier() {
	}

	public static BlockNbtCategory classify(BlockEntity blockEntity) {
		if (blockEntity == null) {
			return BlockNbtCategory.OTHER;
		}

		// 1. 信标、刷怪笼、蜂窝/蜂巢、重生锚、磁石、潮涌核心、附魔台等
		if (blockEntity instanceof BeaconBlockEntity
			|| blockEntity instanceof SpawnerBlockEntity
			|| blockEntity instanceof BeehiveBlockEntity
			|| blockEntity instanceof ConduitBlockEntity
			|| blockEntity instanceof EnchantingTableBlockEntity) {
			return BlockNbtCategory.SPECIAL;
		}

		// 2. 告示牌类、玩家头颅、唱片机、音符盒、旗帜等
		if (blockEntity instanceof SignBlockEntity
			|| blockEntity instanceof HangingSignBlockEntity
			|| blockEntity instanceof SkullBlockEntity
			|| blockEntity instanceof JukeboxBlockEntity
			|| blockEntity instanceof BannerBlockEntity
			|| blockEntity instanceof BellBlockEntity) {
			return BlockNbtCategory.DECORATIVE;
		}

		// 3. 箱子、潜影盒、末影箱、木桶、漏斗、饰罐、雕纹书架、讲台等
		if (blockEntity instanceof ChestBlockEntity
			|| blockEntity instanceof EnderChestBlockEntity
			|| blockEntity instanceof ShulkerBoxBlockEntity
			|| blockEntity instanceof HopperBlockEntity
			|| blockEntity instanceof DecoratedPotBlockEntity
			|| blockEntity instanceof ChiseledBookShelfBlockEntity
			|| blockEntity instanceof LecternBlockEntity) {
			return BlockNbtCategory.CONTAINERS;
		}

		// 4. 熔炉、高炉、烟熏炉、酿造台、营火等
		if (blockEntity instanceof AbstractFurnaceBlockEntity
			|| blockEntity instanceof BrewingStandBlockEntity
			|| blockEntity instanceof CampfireBlockEntity) {
			return BlockNbtCategory.PROCESSING;
		}

		// 5. 投掷器、发射器、活塞、自动合成器等
		if (blockEntity instanceof DispenserBlockEntity
			|| blockEntity instanceof DropperBlockEntity
			|| blockEntity instanceof CrafterBlockEntity
			|| blockEntity instanceof PistonMovingBlockEntity) {
			return BlockNbtCategory.REDSTONE;
		}

		// 6. 命令方块、结构方块、拼图方块
		if (blockEntity instanceof CommandBlockEntity
			|| blockEntity instanceof StructureBlockEntity
			|| blockEntity instanceof JigsawBlockEntity) {
			return BlockNbtCategory.ADVANCED;
		}

		// 7. 类名及通用名称兜底检测
		String name = blockEntity.getClass().getSimpleName().toLowerCase();
		if (name.contains("beacon") || name.contains("spawner") || name.contains("bee") || name.contains("lodestone") || name.contains("anchor") || name.contains("conduit") || name.contains("portal") || name.contains("gateway") || name.contains("vault") || name.contains("trial")) {
			return BlockNbtCategory.SPECIAL;
		}
		if (name.contains("sign") || name.contains("skull") || name.contains("head") || name.contains("jukebox") || name.contains("note") || name.contains("banner") || name.contains("bell")) {
			return BlockNbtCategory.DECORATIVE;
		}
		if (name.contains("chest") || name.contains("shulker") || name.contains("barrel") || name.contains("hopper") || name.contains("pot") || name.contains("shelf") || name.contains("container")) {
			return BlockNbtCategory.CONTAINERS;
		}
		if (name.contains("furnace") || name.contains("smoker") || name.contains("brew") || name.contains("campfire")) {
			return BlockNbtCategory.PROCESSING;
		}
		if (name.contains("dispenser") || name.contains("dropper") || name.contains("piston") || name.contains("crafter") || name.contains("sensor") || name.contains("comparator") || name.contains("detector")) {
			return BlockNbtCategory.REDSTONE;
		}
		if (name.contains("command") || name.contains("structure") || name.contains("jigsaw") || name.contains("test")) {
			return BlockNbtCategory.ADVANCED;
		}

		return BlockNbtCategory.OTHER;
	}
}
