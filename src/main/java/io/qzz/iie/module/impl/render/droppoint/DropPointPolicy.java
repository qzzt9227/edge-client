package io.qzz.iie.module.impl.render.droppoint;

/** Pure decision rules for the landing preview; no Minecraft state is required here. */
public final class DropPointPolicy {
	private static final double MINIMUM_VISIBLE_DISTANCE = 4.0;

	private DropPointPolicy() {
	}

	public static DropPointDecision decide(
		DropPointBlockKind blockKind,
		double verticalDistance,
		double predictedFallDamage,
		double currentHealth,
		boolean sneaking
	) {
		if (blockKind == null || !Double.isFinite(verticalDistance)
			|| verticalDistance <= MINIMUM_VISIBLE_DISTANCE) {
			return new DropPointDecision(DropPointRole.NONE);
		}
		return switch (blockKind) {
			case SAFE -> new DropPointDecision(DropPointRole.SAFE);
			case SCAFFOLD -> new DropPointDecision(
				sneaking ? DropPointRole.SCAFFOLD_SNEAKING : DropPointRole.SCAFFOLD_NEEDS_SNEAKING
			);
			case NORMAL, HAY_BALE, LAVA -> new DropPointDecision(
				predictedFallDamage > currentHealth
					? DropPointRole.DANGER
					: DropPointRole.DEFAULT
			);
		};
	}
}
