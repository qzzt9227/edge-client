package io.qzz.iie.ui.screen;

import io.qzz.iie.module.ModuleCategory;
import io.qzz.iie.ui.animation.AnimatedDouble;
import io.qzz.iie.ui.animation.AnimatedRect;
import io.qzz.iie.ui.animation.AnimationFrameClock;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.theme.ClickGuiMotion;

import java.util.List;

final class ClickGuiAnimations {
	private final AnimationFrameClock frameClock = new AnimationFrameClock();
	private final AnimatedDouble guiOpen =
		new AnimatedDouble(0.0, ClickGuiMotion.GUI_OPEN);
	private final AnimatedDouble settingsPage =
		new AnimatedDouble(1.0, ClickGuiMotion.SETTINGS_PAGE);

	private AnimatedRect sidebarSelection;
	private double frameDeltaSeconds;

	ClickGuiAnimations() {
		guiOpen.animateTo(1.0);
	}

	void resetLayout() {
		frameClock.reset();
		sidebarSelection = null;
	}

	void beginFrame() {
		frameDeltaSeconds = frameClock.nextDeltaSeconds();
		guiOpen.advance(frameDeltaSeconds);
		settingsPage.advance(frameDeltaSeconds);
	}

	double frameDeltaSeconds() {
		return frameDeltaSeconds;
	}

	double guiOpenProgress() {
		return guiOpen.value();
	}

	int guiOpenOffset() {
		return (int) Math.round(
			(1.0 - guiOpen.value()) * ClickGuiMotion.GUI_OPEN_SLIDE_DISTANCE
		);
	}

	Rect advanceSidebarSelection(
		ClickGuiLayout layout,
		List<ModuleCategory> categories,
		ModuleCategory selectedCategory,
		boolean configSelected,
		boolean settingsSelected
	) {
		Rect target;
		if (configSelected) {
			target = layout.configButton();
		} else if (settingsSelected) {
			target = layout.settingsButton();
		} else {
			int categoryIndex = Math.max(0, categories.indexOf(selectedCategory));
			target = layout.categoryButton(categoryIndex);
		}

		if (sidebarSelection == null) {
			sidebarSelection = new AnimatedRect(target, ClickGuiMotion.SELECTION);
		} else {
			sidebarSelection.animateTo(target);
		}
		return sidebarSelection.advance(frameDeltaSeconds);
	}

	void beginSettingsPage() {
		settingsPage.snapTo(0.0);
		settingsPage.animateTo(1.0);
	}

	void finishSettingsPage() {
		settingsPage.snapTo(1.0);
	}

	int settingsPageOffset() {
		return (int) Math.round(
			(1.0 - settingsPage.value()) * ClickGuiMotion.SETTINGS_SLIDE_DISTANCE
		);
	}
}
