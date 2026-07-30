package io.qzz.iie.module.impl.player.autolibrarian;

import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 从原模块回归测试中保留的纯决策契约。
 */
public final class AutoLibrarianLogicContract {
	private AutoLibrarianLogicContract() {
	}

	public static void verify() {
		tradeRulesPreserveExactAnyLevelAndPriceSemantics();
		targetLevelSelectionCyclesThroughAnyLevel();
		recoveryFlowPreservesStopAndReturnDecisions();
		directRecoveryPathRejectsHazardsAndLargeSteps();
		recommendationsPreserveHistoricalOrder();
		catalogSearchAndRegistryValidationPreserveRules();
		recoveryHelpersPreserveSelectionInputAndScreenEdges();
	}

	private static void tradeRulesPreserveExactAnyLevelAndPriceSemantics() {
		EnchantmentTarget exact =
			new EnchantmentTarget("minecraft:mending", 1, false, 10, 20);
		EnchantmentTarget any =
			new EnchantmentTarget("minecraft:efficiency", 5, true, 30, 40);

		check(
			TradeMatcher.matches("minecraft:mending", 1, 10, List.of(exact, any)),
			"exact target must include the lower price boundary"
		);
		check(
			!TradeMatcher.matches("minecraft:mending", 2, 15, List.of(exact, any)),
			"exact target must reject another level"
		);
		check(
			TradeMatcher.matches("minecraft:efficiency", 1, 40, List.of(exact, any)),
			"any-level target must include the upper price boundary"
		);
	}

	private static void targetLevelSelectionCyclesThroughAnyLevel() {
		TargetLevelSelection.State any = TargetLevelSelection.adjust(
			new TargetLevelSelection.State(5, false),
			5,
			1
		);
		check(any.equals(new TargetLevelSelection.State(5, true)), "maximum + must become any");
		check(
			TargetLevelSelection.adjust(any, 5, 1)
				.equals(new TargetLevelSelection.State(1, false)),
			"any + must wrap to minimum"
		);
	}

	private static void recoveryFlowPreservesStopAndReturnDecisions() {
		check(
			RecoveryFlow.next(
				RecoveryFlow.Phase.WAIT_DROP,
				false,
				false,
				true,
				false,
				false,
				false
			) == RecoveryFlow.Phase.WAIT_DROP,
			"an unreachable drop must not return before pickup is confirmed"
		);
		check(
			RecoveryFlow.next(
				RecoveryFlow.Phase.MOVE_TO_DROP,
				false,
				false,
				false,
				false,
				false,
				false
			) == RecoveryFlow.Phase.WAIT_DROP,
			"a vanished target must be rescanned when inventory is not restored"
		);
		check(
			RecoveryFlow.next(
				RecoveryFlow.Phase.MOVE_TO_DROP,
				false,
				true,
				false,
				false,
				false,
				false
			) == RecoveryFlow.Phase.PICKUP_FAILED,
			"pickup timeout must fail instead of returning empty-handed"
		);
		check(
			RecoveryFlow.next(
				RecoveryFlow.Phase.MOVE_TO_DROP,
				true,
				false,
				false,
				false,
				false,
				false
			) == RecoveryFlow.Phase.RETURN_TO_ORIGIN,
			"only confirmed inventory restoration may start the return"
		);
		check(
			RecoveryFlow.next(
				RecoveryFlow.Phase.MOVE_TO_DROP,
				false,
				false,
				true,
				true,
				false,
				true
			) == RecoveryFlow.Phase.STOP,
			"physical movement input must stop recovery"
		);
		check(
			RecoveryDecision.reached(
				new Vec3(10.0, 64.0, 10.0),
				new Vec3(10.15, 64.08, 10.1)
			),
			"original recovery tolerance must be preserved"
		);
		check(
			RecoveryDecision.reachedPickupArea(
				new Vec3(10.0, 64.0, 10.0),
				new Vec3(10.35, 65.0, 10.2)
			),
			"standing at the dropped lectern entity must enter pickup confirmation"
		);
		check(
			!RecoveryDecision.reachedPickupArea(
				new Vec3(10.0, 64.0, 10.0),
				new Vec3(10.8, 64.0, 10.0)
			),
			"pickup confirmation must not begin before reaching the entity"
		);
	}

	private static void directRecoveryPathRejectsHazardsAndLargeSteps() {
		check(
			DirectRecoveryPath.decide(
				new DirectRecoveryPath.Cell(1, true, true, true, false, false)
			) == DirectRecoveryPath.Action.JUMP,
			"safe one-block rise must jump"
		);
		check(
			DirectRecoveryPath.decide(
				new DirectRecoveryPath.Cell(0, true, true, true, false, true)
			) == DirectRecoveryPath.Action.BLOCKED,
			"hazardous cells must be blocked"
		);
	}

	private static void recommendationsPreserveHistoricalOrder() {
		List<EnchantmentCatalog.Entry> catalog = List.of(
			entry("minecraft:sharpness"),
			entry("minecraft:mending"),
			entry("minecraft:efficiency"),
			entry("minecraft:unbreaking")
		);
		check(
			RecommendedEnchantments.select(catalog, 5).stream()
				.map(EnchantmentCatalog.Entry::id)
				.toList()
				.equals(List.of(
					"minecraft:mending",
					"minecraft:unbreaking",
					"minecraft:efficiency",
					"minecraft:sharpness"
				)),
			"recommendations must preserve original priority"
		);
	}

	private static void catalogSearchAndRegistryValidationPreserveRules() {
		List<EnchantmentCatalog.Entry> catalog = List.of(
			new EnchantmentCatalog.Entry(
				"minecraft:mending",
				"经验修补",
				"Mending",
				"经验修补 修补",
				1
			),
			new EnchantmentCatalog.Entry(
				"minecraft:unbreaking",
				"耐久",
				"Unbreaking",
				"耐久",
				3
			)
		);
		check(
			EnchantmentCatalog.search(catalog, "修补", 5).getFirst().id()
				.equals("minecraft:mending"),
			"Chinese aliases must remain searchable"
		);
		check(
			EnchantmentCatalog.search(catalog, "Unbreaking", 5).getFirst().id()
				.equals("minecraft:unbreaking"),
			"English names must remain searchable in another locale"
		);
		check(
			EnchantmentCatalog.displayName(catalog, "minecraft:unbreaking")
				.equals(catalog.get(1).displayName()),
			"configured targets must use the localized enchantment name"
		);
		check(
			!EnchantmentCatalog.displayName(catalog, "minecraft:unbreaking")
				.equals("minecraft:unbreaking"),
			"a known configured target must not be rendered as only its ID"
		);
		check(
			EnchantmentCatalog.displayName(catalog, "example:missing")
				.equals("example:missing"),
			"unknown configured targets must fall back to their exact ID"
		);

		EnchantmentTargetValidator.Result validation =
			EnchantmentTargetValidator.validateAndClamp(
				List.of(
					new EnchantmentTarget(
						"minecraft:unbreaking",
						99,
						true,
						1,
						64
					)
				),
				id -> id.equals("minecraft:unbreaking") ? 3 : 0
			);
		check(validation.valid(), "registered enchantments must validate");
		check(validation.changed(), "out-of-range registered levels must clamp");
		check(validation.targets().getFirst().level() == 3, "real maximum level must win");
		check(
			validation.targets().getFirst().anyLevel(),
			"clamping must not disable any-level matching"
		);
	}

	private static void recoveryHelpersPreserveSelectionInputAndScreenEdges() {
		String nearest = LecternDropSelection.nearest(
			List.of(
				new LecternDropSelection.Candidate<>(
					"far",
					new Vec3(3.0, 0.0, 0.0),
					true,
					true
				),
				new LecternDropSelection.Candidate<>(
					"invalid",
					new Vec3(1.0, 0.0, 0.0),
					true,
					false
				),
				new LecternDropSelection.Candidate<>(
					"near",
					new Vec3(2.0, 0.0, 0.0),
					true,
					true
				)
			),
			Vec3.ZERO,
			Vec3.ZERO,
			3.0
		);
		check(nearest.equals("near"), "nearest valid lectern drop must be selected");

		RecoveryInputState input = new RecoveryInputState();
		input.apply(true, true, true);
		input.release();
		check(
			!input.forward() && !input.jump() && !input.shift(),
			"recovery cancellation must release every controlled input"
		);

		ScreenOpenTracker<Object> tracker = new ScreenOpenTracker<>();
		Object menu = new Object();
		check(tracker.observe(menu), "a newly opened trade menu must report");
		check(!tracker.observe(menu), "the same open menu must only report once");
		check(!tracker.observe(null), "closing a menu must not report");
		check(tracker.observe(menu), "reopening after close must report again");
	}

	private static EnchantmentCatalog.Entry entry(String id) {
		return new EnchantmentCatalog.Entry(id, id, id, "", 5);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
