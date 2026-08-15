package io.qzz.iie.module.impl.render.droppoint;

public record DropPointDecision(DropPointRole role) {
	public boolean visible() {
		return role != DropPointRole.NONE;
	}
}
