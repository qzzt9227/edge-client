package io.qzz.iie.ui.panel;

import io.qzz.iie.setting.Setting;
import io.qzz.iie.ui.render.UiPainter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内联设置项接口。
 */
public interface InlineSettingItem {
	Setting<?> setting();

	int height();

	default int preferredWidth(UiPainter painter) {
		return 100;
	}

	default boolean isVisible() {
		Setting<?> s = setting();
		return s == null || s.isVisible();
	}

	void render(
		UiPainter painter,
		int x,
		int y,
		int width,
		int mouseX,
		int mouseY,
		long time,
		AtomicInteger colorIndex
	);

	boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width);

	default void mouseReleased(double mouseX, double mouseY, int button) {
	}

	default void mouseDragged(
		double mouseX,
		double mouseY,
		double deltaX,
		double deltaY,
		int button,
		int x,
		int y,
		int width
	) {
	}

	default boolean keyPressed(int keyCode, int scancode, int modifiers) {
		return false;
	}

	default boolean charTyped(char chr) {
		return false;
	}
}
