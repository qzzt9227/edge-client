package io.qzz.iie.module.impl.player.autoignite;

import java.util.List;

public final class AutoIgniteTypes {
	private AutoIgniteTypes() {
	}

	public enum ItemSource {
		HOTBAR,
		SILENT_INVENTORY
	}

	public enum IgnitionItem {
		FLINT_AND_STEEL,
		FIRE_CHARGE
	}

	public enum ItemPriority {
		FLINT_FIRST(List.of(IgnitionItem.FLINT_AND_STEEL, IgnitionItem.FIRE_CHARGE)),
		FIRE_CHARGE_FIRST(List.of(IgnitionItem.FIRE_CHARGE, IgnitionItem.FLINT_AND_STEEL)),
		FLINT_ONLY(List.of(IgnitionItem.FLINT_AND_STEEL)),
		FIRE_CHARGE_ONLY(List.of(IgnitionItem.FIRE_CHARGE));

		private final List<IgnitionItem> order;

		ItemPriority(List<IgnitionItem> order) {
			this.order = order;
		}

		public List<IgnitionItem> order() {
			return order;
		}
	}

	public enum TargetHandling {
		LATEST_ONLY,
		QUEUE
	}
}
