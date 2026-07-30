package io.qzz.iie.api.hud;

import io.qzz.iie.ui.layout.Rect;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 可被通用 HUD 定位编辑器测量和绘制的元素。
 */
public interface HudElementPreview {
	HudElementSize measure();

	void extract(GuiGraphicsExtractor graphics, Rect bounds);
}
