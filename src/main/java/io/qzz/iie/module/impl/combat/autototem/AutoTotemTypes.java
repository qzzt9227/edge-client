package io.qzz.iie.module.impl.combat.autototem;

public final class AutoTotemTypes {
	private AutoTotemTypes() {
	}

	public enum OffhandMode {
		SWAP("swap", "client.option.auto_totem.swap"),
		DROP("drop", "client.option.auto_totem.drop"),
		RESTORE("restore", "client.option.auto_totem.restore");

		private final String id;
		private final String translationKey;

		OffhandMode(String id, String translationKey) {
			this.id = id;
			this.translationKey = translationKey;
		}

		public String id() {
			return id;
		}

		public String translationKey() {
			return translationKey;
		}
	}
}
