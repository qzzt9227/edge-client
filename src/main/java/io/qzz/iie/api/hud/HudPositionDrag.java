package io.qzz.iie.api.hud;

import io.qzz.iie.ui.layout.Rect;

import java.util.Objects;

/**
 * HUD 定位编辑器共享的拖动状态机，支持 4 边角与屏幕边缘自动磁吸（Magnetic Snap）。
 *
 * <p>移动阶段只更新草稿；{@link #end()} 才写入设置，避免拖动过程中反复保存配置。</p>
 */
public final class HudPositionDrag {
	public static final double SNAP_THRESHOLD = 16.0;

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

	public HudPositionSetting setting() {
		return setting;
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
		double rawLeft = pointerX - grabOffsetX;
		double rawTop = pointerY - grabOffsetY;

		double left = rawLeft;
		double top = rawTop;

		// 4 边角与边缘自动吸附算法
		if (availableWidth > 0) {
			if (Math.abs(left) < SNAP_THRESHOLD) {
				left = 0.0;
			} else if (Math.abs(left - availableWidth) < SNAP_THRESHOLD) {
				left = availableWidth;
			}
		}

		if (availableHeight > 0) {
			if (Math.abs(top) < SNAP_THRESHOLD) {
				top = 0.0;
			} else if (Math.abs(top - availableHeight) < SNAP_THRESHOLD) {
				top = availableHeight;
			}
		}

		left = Math.clamp(left, 0.0, availableWidth);
		top = Math.clamp(top, 0.0, availableHeight);

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
