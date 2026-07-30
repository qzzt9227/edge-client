package io.qzz.iie.api.hud;

import io.qzz.iie.ui.layout.Rect;

import java.util.Objects;

/**
 * HUD 定位编辑器共享的拖动状态机。
 *
 * <p>移动阶段只更新草稿；{@link #end()} 才写入设置，避免拖动过程中反复保存配置。</p>
 */
public final class HudPositionDrag {
	private final HudPositionSetting setting;
	private HudPosition draft;
	private int viewportWidth;
	private int viewportHeight;
	private int elementWidth;
	private int elementHeight;
	private double grabOffsetX;
	private double grabOffsetY;
	private boolean dragging;

	public HudPositionDrag(HudPositionSetting setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
		draft = setting.value();
	}

	public void layout(
		int viewportWidth,
		int viewportHeight,
		int elementWidth,
		int elementHeight
	) {
		if (viewportWidth < 0 || viewportHeight < 0
			|| elementWidth < 0 || elementHeight < 0) {
			throw new IllegalArgumentException("HUD drag sizes must be non-negative");
		}
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		this.elementWidth = elementWidth;
		this.elementHeight = elementHeight;
	}

	public HudPosition draft() {
		return draft;
	}

	public Rect bounds() {
		return HudPositionLayout.resolve(
			draft,
			viewportWidth,
			viewportHeight,
			elementWidth,
			elementHeight
		);
	}

	public boolean isDragging() {
		return dragging;
	}

	public boolean begin(double pointerX, double pointerY) {
		Rect bounds = bounds();
		if (!bounds.contains(pointerX, pointerY)) {
			return false;
		}
		grabOffsetX = pointerX - bounds.x();
		grabOffsetY = pointerY - bounds.y();
		dragging = true;
		return true;
	}

	public void move(double pointerX, double pointerY) {
		if (!dragging) {
			return;
		}
		double availableWidth = Math.max(0, viewportWidth - elementWidth);
		double availableHeight = Math.max(0, viewportHeight - elementHeight);
		double left = Math.clamp(pointerX - grabOffsetX, 0.0, availableWidth);
		double top = Math.clamp(pointerY - grabOffsetY, 0.0, availableHeight);
		draft = new HudPosition(
			availableWidth == 0.0 ? 0.5 : left / availableWidth,
			availableHeight == 0.0 ? 0.5 : top / availableHeight
		);
	}

	public boolean end() {
		if (!dragging) {
			return false;
		}
		dragging = false;
		setting.set(draft);
		return true;
	}

	public void cancel() {
		dragging = false;
		draft = setting.value();
	}
}
