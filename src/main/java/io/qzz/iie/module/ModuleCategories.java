package io.qzz.iie.module;

import java.util.List;

public final class ModuleCategories {
	public static final ModuleCategory COMBAT = new ModuleCategory(
		"combat",
		"client.category.combat",
		100
	);
	public static final ModuleCategory PLAYER = new ModuleCategory(
		"player",
		"client.category.player",
		200
	);
	public static final ModuleCategory MOVEMENT = new ModuleCategory(
		"movement",
		"client.category.movement",
		300
	);
	public static final ModuleCategory RENDER = new ModuleCategory(
		"render",
		"client.category.render",
		400
	);
	public static final ModuleCategory GUI = new ModuleCategory(
		"gui",
		"client.category.gui",
		500
	);

	private ModuleCategories() {
	}

	public static List<ModuleCategory> builtIns() {
		return List.of(COMBAT, PLAYER, MOVEMENT, RENDER, GUI);
	}
}
