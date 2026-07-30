package io.qzz.iie.module.impl.player.autolibrarian;

import io.qzz.iie.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * 自动图书管理员的状态机编排器。
 */
final class AutoLibrarianController {
	private static final int PLACE_CONFIRM_TIMEOUT = 40;
	private static final int TRADE_OPEN_TIMEOUT = 80;
	private static final int TRADE_DATA_TIMEOUT = 40;
	private static final int BREAK_TIMEOUT = 400;

	private final Minecraft client;
	private final AutoLibrarianModule module;
	private final AutomationSession session = new AutomationSession();
	private final RotationController rotation = new RotationController();
	private final VillagerLocator villagers = new VillagerLocator();
	private final LecternPlacementFinder placements = new LecternPlacementFinder();
	private final HotbarSelector hotbar = new HotbarSelector();
	private final AutomationInteractions interactions;
	private final LecternRecoveryController recovery;
	private AutomationState state = AutomationState.IDLE;

	AutoLibrarianController(Minecraft client, AutoLibrarianModule module) {
		this.client = client;
		this.module = module;
		interactions = new AutomationInteractions(client);
		recovery = new LecternRecoveryController(client);
	}

	boolean isActive() {
		return state != AutomationState.IDLE;
	}

	String statusTranslationKey() {
		return state.translationKey;
	}

	boolean start() {
		if (isActive()) {
			return true;
		}
		LocalPlayer player = client.player;
		if (player == null || client.level == null || client.gameMode == null) {
			show("client.message.auto_librarian.no_world");
			return false;
		}
		if (client.gui.screen() != null) {
			show("client.message.auto_librarian.close_screen");
			return false;
		}

		AutoLibrarianSettings settings = module.snapshot();
		if (settings.targets().isEmpty()) {
			show("client.message.auto_librarian.invalid_target");
			return false;
		}
		var enchantments =
			client.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		EnchantmentTargetValidator.Result validation =
			EnchantmentTargetValidator.validateAndClamp(
				settings.targets(),
				id -> {
					Identifier identifier = Identifier.tryParse(id);
					return identifier != null && enchantments.containsKey(identifier)
						? enchantments.getValue(identifier).definition().maxLevel()
						: 0;
				}
			);
		if (!validation.valid()) {
			show("client.message.auto_librarian.invalid_target");
			return false;
		}
		if (validation.changed()) {
			module.targets().set(validation.targets());
		}
		if (hotbar.findItem(player, Items.LECTERN) < 0) {
			show("client.message.auto_librarian.no_lectern");
			return false;
		}

		session.begin(client.level, player);
		state = AutomationState.WAIT_PROFESSION;
		beginRound(null, module.snapshot());
		return isActive();
	}

	void tick() {
		if (!isActive()) {
			return;
		}
		if (client.level == null
			|| client.player == null
			|| client.gameMode == null
			|| client.level != session.startedLevel) {
			stop("client.message.auto_librarian.world_changed");
			return;
		}

		AutoLibrarianSettings settings = module.snapshot();
		Villager villager = villagers.findById(client.level, session.villagerId);
		if (!validateTarget(villager, settings)) {
			return;
		}

		switch (state) {
			case ROTATING -> tickRotation();
			case WAIT_BEFORE_PLACE -> tickDelay(this::placeLectern);
			case WAIT_PLACE_CONFIRM -> tickPlaceConfirmation(settings);
			case WAIT_PROFESSION -> tickProfession(villager, settings);
			case WAIT_BEFORE_OPEN -> tickDelay(() -> openTrade(villager));
			case WAIT_TRADE_SCREEN -> tickTradeScreen();
			case WAIT_TRADE_DATA -> tickTradeData(settings);
			case WAIT_BEFORE_BREAK -> tickDelay(() -> prepareBreak(settings));
			case BREAKING -> tickBreaking(settings);
			case WAIT_BEFORE_RECYCLE, WAIT_RECYCLE_DROP,
				MOVING_TO_RECYCLE_DROP, RETURNING_FROM_RECYCLE ->
				tickRecovery(settings);
			case WAIT_AFTER_BREAK -> tickDelay(() -> {
				state = AutomationState.WAIT_UNEMPLOYED;
				session.timer = settings.professionCheckIntervalTicks();
			});
			case WAIT_UNEMPLOYED -> tickUnemployed(villager, settings);
			case IDLE -> {
			}
		}
	}

	void stop(String reasonTranslationKey) {
		if (stopInternal()) {
			show(reasonTranslationKey);
		}
	}

	void stopSilently() {
		stopInternal();
	}

	private boolean stopInternal() {
		if (!isActive()) {
			return false;
		}
		interactions.stopBreaking();
		recovery.cancel();
		if (client.player != null
			&& client.player.containerMenu instanceof MerchantMenu) {
			client.player.closeContainer();
		}
		session.restorePlayer(client.player);
		state = AutomationState.IDLE;
		clearRuntimeState();
		return true;
	}

	private boolean validateTarget(
		Villager villager,
		AutoLibrarianSettings settings
	) {
		if (villager == null || !villager.isAlive() || villager.isRemoved()) {
			stop("client.message.auto_librarian.villager_lost");
			return false;
		}
		double allowed = settings.searchRadius() + 1.0;
		if (!recovery.isActive()
			&& client.player.distanceToSqr(villager) > allowed * allowed) {
			stop("client.message.auto_librarian.villager_out_of_range");
			return false;
		}
		return true;
	}

	private void beginRound(
		Villager preferred,
		AutoLibrarianSettings settings
	) {
		LocalPlayer player = client.player;
		Villager villager = preferred != null
			? preferred
			: villagers.findNearestUnemployed(
				client.level,
				player,
				settings.searchRadius()
			);
		if (villager == null) {
			stop("client.message.auto_librarian.no_villager");
			return;
		}

		session.villagerId = villager.getId();
		int lecternSlot = hotbar.findItem(player, Items.LECTERN);
		if (lecternSlot < 0) {
			stop("client.message.auto_librarian.no_lectern");
			return;
		}
		var lecternStack = player.getInventory().getItem(lecternSlot);
		var placement = session.lecternPlacementMemory.select(
			position -> placements.findAt(
				client.level,
				player,
				villager,
				lecternStack,
				settings.placementRadius(),
				position
			),
			() -> placements.find(
				client.level,
				player,
				villager,
				lecternStack,
				settings.placementRadius()
			)
		);
		if (placement == null) {
			stop("client.message.auto_librarian.no_place_position");
			return;
		}

		session.lecternPos = placement.position();
		session.placementHit = placement.hitResult();
		session.lecternCountBeforePlacement = LecternInventoryCounter.count(player);
		player.getInventory().setSelectedSlot(lecternSlot);
		beginRotation(
			placement.hitResult().getLocation(),
			AutomationState.WAIT_BEFORE_PLACE,
			settings.beforePlaceTicks(),
			settings
		);
	}

	private void placeLectern() {
		switch (interactions.placeLectern(session.placementHit)) {
			case STARTED -> {
				state = AutomationState.WAIT_PLACE_CONFIRM;
				session.timer = PLACE_CONFIRM_TIMEOUT;
			}
			case NO_LECTERN -> stop("client.message.auto_librarian.no_lectern");
			case OUT_OF_RANGE ->
				stop("client.message.auto_librarian.place_out_of_range");
			case REJECTED -> stop("client.message.auto_librarian.place_rejected");
		}
	}

	private void tickPlaceConfirmation(AutoLibrarianSettings settings) {
		if (interactions.isLecternPresent(session.lecternPos)) {
			state = AutomationState.WAIT_PROFESSION;
			session.timer = settings.professionCheckIntervalTicks();
		} else if (--session.timer <= 0) {
			stop("client.message.auto_librarian.place_not_confirmed");
		}
	}

	private void tickProfession(
		Villager villager,
		AutoLibrarianSettings settings
	) {
		if (--session.timer > 0) {
			return;
		}
		session.timer = settings.professionCheckIntervalTicks();
		if (!interactions.isLecternPresent(session.lecternPos)) {
			stop("client.message.auto_librarian.lectern_missing");
			return;
		}
		if (villager.getVillagerData().profession().is(VillagerProfession.LIBRARIAN)) {
			Vec3 target =
				villager.position().add(0.0, villager.getBbHeight() * 0.55, 0.0);
			beginRotation(
				target,
				AutomationState.WAIT_BEFORE_OPEN,
				settings.beforeOpenTradeTicks(),
				settings
			);
		} else if (!villager.getVillagerData().profession().is(VillagerProfession.NONE)) {
			stop("client.message.auto_librarian.wrong_profession");
		}
	}

	private void openTrade(Villager villager) {
		switch (interactions.openTrade(villager)) {
			case STARTED -> {
				state = AutomationState.WAIT_TRADE_SCREEN;
				session.timer = TRADE_OPEN_TIMEOUT;
			}
			case OUT_OF_RANGE ->
				stop("client.message.auto_librarian.trade_out_of_range");
			case REJECTED -> stop("client.message.auto_librarian.trade_rejected");
		}
	}

	private void tickTradeScreen() {
		if (client.player.containerMenu instanceof MerchantMenu) {
			state = AutomationState.WAIT_TRADE_DATA;
			session.timer = TRADE_DATA_TIMEOUT;
		} else if (--session.timer <= 0) {
			stop("client.message.auto_librarian.trade_not_opened");
		}
	}

	private void tickTradeData(AutoLibrarianSettings settings) {
		if (!(client.player.containerMenu instanceof MerchantMenu menu)) {
			stop("client.message.auto_librarian.trade_closed");
			return;
		}
		if (menu.getOffers().isEmpty() && --session.timer > 0) {
			return;
		}

		var match = TradeMatcher.findMatch(menu.getOffers(), settings.targets());
		client.player.closeContainer();
		if (match.isPresent()) {
			finishSuccess(match.get());
			return;
		}
		state = AutomationState.WAIT_BEFORE_BREAK;
		session.timer = settings.beforeBreakTicks();
	}

	private void prepareBreak(AutoLibrarianSettings settings) {
		if (!interactions.isLecternPresent(session.lecternPos)) {
			stop("client.message.auto_librarian.lectern_missing");
			return;
		}
		int toolSlot = hotbar.findBestMiningSlot(
			client.player,
			client.level.getBlockState(session.lecternPos),
			settings.allowHandMining(),
			session.originalSlot
		);
		if (toolSlot < 0) {
			stop("client.message.auto_librarian.no_mining_slot");
			return;
		}
		client.player.getInventory().setSelectedSlot(toolSlot);
		beginRotation(
			Vec3.atCenterOf(session.lecternPos),
			AutomationState.BREAKING,
			BREAK_TIMEOUT,
			settings
		);
	}

	private void tickBreaking(AutoLibrarianSettings settings) {
		boolean firstTick = session.timer == BREAK_TIMEOUT;
		switch (interactions.continueBreaking(session.lecternPos, firstTick)) {
			case COMPLETE -> beginRecycleOrAfterBreak(settings);
			case PROGRESS -> {
				if (--session.timer <= 0) {
					interactions.stopBreaking();
					stop("client.message.auto_librarian.break_timeout");
				}
			}
			case OUT_OF_RANGE ->
				stop("client.message.auto_librarian.break_out_of_range");
			case REJECTED -> stop("client.message.auto_librarian.break_rejected");
		}
	}

	private void beginRecycleOrAfterBreak(AutoLibrarianSettings settings) {
		if (!settings.autoRecycle()) {
			enterAfterBreak(settings);
			return;
		}
		session.lecternPlacementMemory.record(session.lecternPos);
		recovery.begin(
			session.lecternCountBeforePlacement,
			session.lecternPos,
			settings
		);
		state = recovery.state();
	}

	private void tickRecovery(AutoLibrarianSettings settings) {
		LecternRecoveryController.Outcome outcome = recovery.tick(settings);
		if (outcome == LecternRecoveryController.Outcome.AFTER_BREAK) {
			enterAfterBreak(settings);
		} else if (outcome == LecternRecoveryController.Outcome.MANUAL_OVERRIDE) {
			stop("client.message.auto_librarian.recycling_manual_override");
		} else if (outcome == LecternRecoveryController.Outcome.PICKUP_FAILED) {
			stop("client.message.auto_librarian.recycle_pickup_failed");
		} else if (outcome == LecternRecoveryController.Outcome.RETURN_FAILED) {
			stop("client.message.auto_librarian.recycle_return_failed");
		} else {
			state = recovery.state();
		}
	}

	private void enterAfterBreak(AutoLibrarianSettings settings) {
		state = AutomationState.WAIT_AFTER_BREAK;
		session.timer = settings.afterBreakTicks();
	}

	private void tickUnemployed(
		Villager villager,
		AutoLibrarianSettings settings
	) {
		if (--session.timer > 0) {
			return;
		}
		session.timer = settings.professionCheckIntervalTicks();
		if (villager.getVillagerData().profession().is(VillagerProfession.NONE)) {
			beginRound(villager, settings);
		} else if (!villager.getVillagerData().profession().is(
			VillagerProfession.LIBRARIAN
		)) {
			stop("client.message.auto_librarian.wrong_profession");
		}
	}

	private void beginRotation(
		Vec3 target,
		AutomationState nextState,
		int nextTimer,
		AutoLibrarianSettings settings
	) {
		rotation.begin(
			client.player,
			target,
			settings.rotationTicks(),
			nextState,
			nextTimer
		);
		state = AutomationState.ROTATING;
	}

	private void tickRotation() {
		RotationController.Completion completion = rotation.tick(client.player);
		if (completion != null) {
			state = completion.nextState();
			session.timer = completion.nextTimer();
		}
	}

	private void tickDelay(Runnable action) {
		if (--session.timer <= 0) {
			action.run();
		}
	}

	private void finishSuccess(TradeMatcher.Match match) {
		recovery.cancel();
		session.restorePlayer(client.player);
		state = AutomationState.IDLE;
		show(
			"client.message.auto_librarian.success",
			match.enchantmentId(),
			match.level(),
			match.emeraldPrice()
		);
		Client.LOGGER.info(
			"Auto Librarian found {} level {} for {} emerald(s)",
			match.enchantmentId(),
			match.level(),
			match.emeraldPrice()
		);
		clearRuntimeState();
	}

	private void clearRuntimeState() {
		rotation.clear();
		recovery.cancel();
		session.clear();
	}

	private void show(String translationKey, Object... arguments) {
		module.messages().show(Component.translatable(translationKey, arguments));
	}
}
