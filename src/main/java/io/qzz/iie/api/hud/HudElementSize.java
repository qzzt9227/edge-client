package io.qzz.iie.api.hud;

/**
 * HUD 元素在当前 GUI 缩放下占用的像素尺寸。
 */
public record HudElementSize(int width, int height) {
	public HudElementSize {
		if (width < 0 || height < 0) {
			throw new IllegalArgumentException("HUD element size must be non-negative");
		}
	}
}
