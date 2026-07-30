package io.qzz.iie.ui.theme;

import io.qzz.iie.ui.animation.AnimationSpec;
import io.qzz.iie.ui.animation.Easing;

public final class ClickGuiMotion {
	public static final AnimationSpec GUI_OPEN =
		new AnimationSpec(0.26, Easing.CUBIC_OUT);
	public static final AnimationSpec SELECTION =
		new AnimationSpec(0.22, Easing.CUBIC_IN_OUT);
	public static final AnimationSpec TOGGLE =
		new AnimationSpec(0.18, Easing.CUBIC_OUT);
	public static final AnimationSpec CHOICE_DRAWER =
		new AnimationSpec(0.16, Easing.CUBIC_OUT);
	public static final AnimationSpec SETTINGS_PAGE =
		new AnimationSpec(0.24, Easing.CUBIC_OUT);
	public static final AnimationSpec SCROLL =
		new AnimationSpec(0.22, Easing.CUBIC_OUT);
	public static final int GUI_OPEN_SLIDE_DISTANCE = 18;
	public static final int SETTINGS_SLIDE_DISTANCE = 24;

	private ClickGuiMotion() {
	}
}
