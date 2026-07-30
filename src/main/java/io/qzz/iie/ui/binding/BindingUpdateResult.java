package io.qzz.iie.ui.binding;

public sealed interface BindingUpdateResult {
	boolean accepted();

	record Accepted() implements BindingUpdateResult {
		@Override
		public boolean accepted() {
			return true;
		}
	}

	record Rejected(Throwable cause) implements BindingUpdateResult {
		@Override
		public boolean accepted() {
			return false;
		}
	}
}
