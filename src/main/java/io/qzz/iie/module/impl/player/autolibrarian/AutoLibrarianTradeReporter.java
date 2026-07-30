package io.qzz.iie.module.impl.player.autolibrarian;

import io.qzz.iie.api.message.MessageBoxApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 每次打开村民交易界面后，通过共享消息框汇报一次附魔书信息。
 */
public final class AutoLibrarianTradeReporter {
	private static final int OFFER_SYNC_TIMEOUT_TICKS = 40;

	private final Minecraft client;
	private final AutoLibrarianModule module;
	private final MessageBoxApi messages;
	private final ScreenOpenTracker<MerchantMenu> openTracker = new ScreenOpenTracker<>();
	private MerchantMenu pendingMenu;
	private int remainingSyncTicks;

	public AutoLibrarianTradeReporter(
		Minecraft client,
		AutoLibrarianModule module,
		MessageBoxApi messages
	) {
		this.client = Objects.requireNonNull(client, "client");
		this.module = Objects.requireNonNull(module, "module");
		this.messages = Objects.requireNonNull(messages, "messages");
	}

	public void tick() {
		MerchantMenu current = currentVisibleMenu();
		boolean opened = openTracker.observe(current);
		if (!module.reportTrades().value()) {
			pendingMenu = null;
			return;
		}
		if (opened) {
			pendingMenu = current;
			remainingSyncTicks = OFFER_SYNC_TIMEOUT_TICKS;
		}
		if (current == null || pendingMenu != current) {
			pendingMenu = null;
			return;
		}
		if (current.getOffers().isEmpty() && remainingSyncTicks-- > 0) {
			return;
		}
		report(current);
		pendingMenu = null;
	}

	private MerchantMenu currentVisibleMenu() {
		if (client.player != null
			&& client.gui.screen() instanceof MerchantScreen
			&& client.player.containerMenu instanceof MerchantMenu menu) {
			return menu;
		}
		return null;
	}

	private void report(MerchantMenu menu) {
		List<Component> bookProperties = new ArrayList<>();
		for (var offer : menu.getOffers()) {
			var result = offer.getResult();
			if (!result.is(Items.ENCHANTED_BOOK)) {
				continue;
			}
			ItemEnchantments enchantments =
				result.get(DataComponents.STORED_ENCHANTMENTS);
			if (enchantments == null || enchantments.isEmpty()) {
				bookProperties.add(Component.translatable(
					"client.message.auto_librarian.trade_report.unknown_property"
				));
				continue;
			}
			MutableComponent property = Component.empty();
			boolean first = true;
			for (var entry : enchantments.entrySet()) {
				if (!first) {
					property.append(Component.literal(" + "));
				}
				property.append(
					Enchantment.getFullname(entry.getKey(), entry.getIntValue())
				);
				first = false;
			}
			bookProperties.add(property);
		}

		if (bookProperties.isEmpty()) {
			show("client.message.auto_librarian.trade_report.none");
			return;
		}
		show(
			"client.message.auto_librarian.trade_report.found",
			bookProperties.size()
		);
		for (Component property : bookProperties) {
			messages.show(Component.translatable(
				"client.message.auto_librarian.trade_report.property",
				property
			));
		}
	}

	private void show(String translationKey, Object... arguments) {
		messages.show(Component.translatable(translationKey, arguments));
	}
}
