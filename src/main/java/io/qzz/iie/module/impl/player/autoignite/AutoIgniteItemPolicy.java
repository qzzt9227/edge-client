package io.qzz.iie.module.impl.player.autoignite;

import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.IgnitionItem;
import io.qzz.iie.module.impl.player.autoignite.AutoIgniteTypes.ItemSource;

public final class AutoIgniteItemPolicy {
	private AutoIgniteItemPolicy() {
	}

	public static boolean shouldRestoreSelection(
		ItemSource source,
		IgnitionItem item,
		boolean restoreAfterFlint
	) {
		return source == ItemSource.SILENT_INVENTORY
			|| item == IgnitionItem.FIRE_CHARGE
			|| restoreAfterFlint;
	}
}
