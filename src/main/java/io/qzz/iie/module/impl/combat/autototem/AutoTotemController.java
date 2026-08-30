package io.qzz.iie.module.impl.combat.autototem;

import io.qzz.iie.api.message.MessageBoxApi;
import io.qzz.iie.module.impl.combat.autototem.AutoTotemTypes.OffhandMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Objects;
import java.util.Random;

final class AutoTotemController {
	private static final int OFFHAND_SLOT_INDEX = 45;
	private final MessageBoxApi messages;
	private final Random random = new Random();

	private int delayTimer = 0;
	private int lastEquippedTotemCount = -1;

	AutoTotemController(MessageBoxApi messages) {
		this.messages = Objects.requireNonNull(messages, "messages");
	}

	void tick(AutoTotemModule module) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || client.gameMode == null || !player.isAlive()) {
			reset();
			return;
		}

		if (delayTimer > 0) {
			delayTimer--;
			return;
		}

		// 检查副手当前物品
		ItemStack offhand = player.getOffhandItem();
		if (offhand.is(Items.TOTEM_OF_UNDYING)) {
			return;
		}

		// 评估是否需要装备图腾
		boolean needTotem = evaluateNeedTotem(player, module);
		if (!needTotem) {
			return;
		}

		// 查找图腾（或备选盾牌）
		Inventory inventory = player.getInventory();
		int targetSlot = findItemSlot(inventory, Items.TOTEM_OF_UNDYING);
		boolean isTotem = true;

		if (targetSlot < 0 && module.fallbackShield().value()) {
			if (!offhand.is(Items.SHIELD)) {
				targetSlot = findItemSlot(inventory, Items.SHIELD);
				isTotem = false;
			}
		}

		if (targetSlot < 0) {
			return;
		}

		// 计算延迟
		int baseDelay = (int) module.delay().value().doubleValue();
		int randomOffset = (int) module.randomDelay().value().doubleValue();
		if (randomOffset > 0) {
			baseDelay += random.nextInt(randomOffset + 1);
		}

		if (baseDelay > 0 && delayTimer <= 0) {
			delayTimer = baseDelay;
			return;
		}

		// 执行副手装备
		equipToOffhand(client, player, targetSlot, module.offhandMode().value());

		// 提示信息
		if (module.alerts().value()) {
			if (isTotem) {
				int remaining = countItem(player.getInventory(), Items.TOTEM_OF_UNDYING);
				messages.show(Component.translatable(
					"client.message.auto_totem.equipped",
					remaining
				));
			} else {
				messages.show(Component.translatable("client.message.auto_totem.shield_equipped"));
			}
		}
	}

	void reset() {
		delayTimer = 0;
		lastEquippedTotemCount = -1;
	}

	private boolean evaluateNeedTotem(LocalPlayer player, AutoTotemModule module) {
		float health = player.getHealth();
		float absorption = player.getAbsorptionAmount();
		double fallDist = player.fallDistance;

		boolean slowFalling = false;
		float jumpBoost = 0.0F;
		int resistanceLevel = 0;

		if (module.checkEffects().value()) {
			slowFalling = player.hasEffect(MobEffects.SLOW_FALLING);
			MobEffectInstance jumpEffect = player.getEffect(MobEffects.JUMP_BOOST);
			if (jumpEffect != null) {
				jumpBoost = jumpEffect.getAmplifier() + 1.0F;
			}
			MobEffectInstance resEffect = player.getEffect(MobEffects.RESISTANCE);
			if (resEffect != null) {
				resistanceLevel = resEffect.getAmplifier() + 1;
			}
		}

		double predictedFall = AutoTotemPolicy.calculateFallDamage(fallDist, jumpBoost, slowFalling);
		boolean isInvulnerable = module.checkEffects().value() && AutoTotemPolicy.isCompletelyInvulnerable(resistanceLevel);

		return AutoTotemPolicy.shouldEquipTotem(
			health,
			absorption,
			predictedFall,
			module.healthThreshold().value().doubleValue(),
			module.fallDamageThreshold().value().doubleValue(),
			module.onlyOnLowHealth().value(),
			isInvulnerable
		);
	}

	private void equipToOffhand(
		Minecraft client,
		LocalPlayer player,
		int invSlotIndex,
		OffhandMode offhandMode
	) {
		if (player.containerMenu != player.inventoryMenu) {
			return;
		}

		int containerSlot = convertToContainerSlot(invSlotIndex);
		if (containerSlot < 0) {
			return;
		}

		int containerId = player.inventoryMenu.containerId;

		// 1. 拿起目标物品
		client.gameMode.handleContainerInput(
			containerId,
			containerSlot,
			0,
			ContainerInput.PICKUP,
			player
		);

		// 2. 放入副手槽（此时原副手物品被拿起在鼠标光标上）
		client.gameMode.handleContainerInput(
			containerId,
			OFFHAND_SLOT_INDEX,
			0,
			ContainerInput.PICKUP,
			player
		);

		// 3. 处理原副手物品
		if (offhandMode == OffhandMode.DROP) {
			// 丢弃到界面外
			client.gameMode.handleContainerInput(
				containerId,
				-999,
				0,
				ContainerInput.PICKUP,
				player
			);
		} else {
			// 放入原物品槽位 (SWAP / RESTORE)
			client.gameMode.handleContainerInput(
				containerId,
				containerSlot,
				0,
				ContainerInput.PICKUP,
				player
			);
		}
	}

	private static int convertToContainerSlot(int invSlotIndex) {
		if (invSlotIndex >= 0 && invSlotIndex < 9) {
			// 快捷栏 (0-8) -> 容器槽位 36-44
			return 36 + invSlotIndex;
		}
		if (invSlotIndex >= 9 && invSlotIndex < 36) {
			// 背包主区 (9-35) -> 容器槽位 9-35
			return invSlotIndex;
		}
		return -1;
	}

	private static int findItemSlot(Inventory inventory, net.minecraft.world.item.Item item) {
		// 优先快捷栏
		for (int i = 0; i < Inventory.getSelectionSize(); i++) {
			if (inventory.getItem(i).is(item)) {
				return i;
			}
		}
		// 其次主背包
		for (int i = 9; i < 36; i++) {
			if (inventory.getItem(i).is(item)) {
				return i;
			}
		}
		return -1;
	}

	private static int countItem(Inventory inventory, net.minecraft.world.item.Item item) {
		int count = 0;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.is(item)) {
				count += stack.getCount();
			}
		}
		return count;
	}
}
