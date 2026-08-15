package io.qzz.iie.module.impl.player.autoignite;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;

import java.util.Objects;

/** 只记录本地玩家发起的 TNT 放置候选，不消费原版交互。 */
public final class AutoIgniteHooks {
	private static AutoIgniteModule module;
	private static boolean installed;

	private AutoIgniteHooks() {
	}

	public static void install(AutoIgniteModule installedModule) {
		module = Objects.requireNonNull(installedModule, "installedModule");
		if (!installed) {
			installed = true;
			UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
				AutoIgniteModule current = module;
				Minecraft client = Minecraft.getInstance();
				if (current == null
					|| !current.isEnabled()
					|| player != client.player
					|| level != client.level
					|| !player.getItemInHand(hand).is(Items.TNT)) {
					return InteractionResult.PASS;
				}
				BlockPlaceContext context = new BlockPlaceContext(
					player,
					hand,
					player.getItemInHand(hand),
					hit
				);
				if (context.canPlace()
					&& !level.getBlockState(context.getClickedPos()).is(Blocks.TNT)) {
					current.recordPlacementCandidate(context.getClickedPos().asLong());
				}
				return InteractionResult.PASS;
			});
		}
	}
}
