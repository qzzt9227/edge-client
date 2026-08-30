package io.qzz.iie.module.impl.player.packetmine;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * 包挖掘客户端生命周期与方块点击钩子。
 */
public final class PacketMineHooks {
	private static PacketMineModule module;
	private static PacketMineController controller;
	private static boolean installed;

	private PacketMineHooks() {
	}

	public static void install(PacketMineModule installedModule) {
		module = Objects.requireNonNull(installedModule, "installedModule");
		controller = new PacketMineController(module);
		if (!installed) {
			installed = true;
			AttackBlockCallback.EVENT.register(PacketMineHooks::onAttackBlock);
			ClientTickEvents.END_CLIENT_TICK.register(PacketMineHooks::onClientTick);
		}
	}

	private static InteractionResult onAttackBlock(
		Player player,
		Level level,
		InteractionHand hand,
		BlockPos pos,
		Direction direction
	) {
		if (module == null || !module.isEnabled() || controller == null) {
			return InteractionResult.PASS;
		}
		Minecraft client = Minecraft.getInstance();
		if (player != client.player || hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		controller.start(pos, direction);
		return InteractionResult.PASS;
	}

	private static void onClientTick(Minecraft client) {
		if (controller != null) {
			controller.tick(client);
		}
	}

	public static void abort() {
		if (controller != null) {
			controller.abort();
		}
	}
}
