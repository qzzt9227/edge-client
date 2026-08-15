package io.qzz.iie.ui.screen;

import io.qzz.iie.api.hud.HudElementPreview;
import io.qzz.iie.api.hud.HudElementSize;
import io.qzz.iie.api.hud.HudPositionDrag;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.i18n.ClientI18n;
import io.qzz.iie.ui.hud.HudPositionEditorManager;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 统一全屏 HUD 可视化编辑屏幕：背景完全透明，支持多元素自由拖拽与 4 边角/屏幕边缘自动磁吸。
 */
public final class HudEditorScreen extends Screen {
	private final HudPositionEditorManager manager;
	private final Screen parent;
	private final List<HudEntry> entries = new ArrayList<>();
	private HudEntry activeDragging;

	private record HudEntry(
		HudPositionSetting setting,
		HudElementPreview preview,
		HudPositionDrag drag
	) {
	}

	public HudEditorScreen(
		HudPositionEditorManager manager,
		Screen parent
	) {
		super(Component.translatable("client.gui.hud_editor.title"));
		this.manager = Objects.requireNonNull(manager, "manager");
		this.parent = parent;

		for (Map.Entry<HudPositionSetting, HudElementPreview> e : manager.registeredPreviews().entrySet()) {
			entries.add(new HudEntry(e.getKey(), e.getValue(), new HudPositionDrag(e.getKey())));
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// 保持完全透明，不绘制暗色或模糊背景
	}

	@Override
	public void extractTransparentBackground(GuiGraphicsExtractor graphics) {
		// 保持完全透明
	}

	@Override
	public void extractBlurredBackground(GuiGraphicsExtractor graphics) {
		// 保持完全透明
	}

	@Override
	public void extractRenderState(
		GuiGraphicsExtractor graphics,
		int mouseX,
		int mouseY,
		float delta
	) {
		UiPainter painter = new UiPainter(
			graphics,
			font,
			1.0,
			io.qzz.iie.font.ClientFontManager.getActiveFontDescription()
		);

		// 1. 磁吸辅助指示线
		if (activeDragging != null && activeDragging.drag.isDragging()) {
			Rect b = activeDragging.drag.bounds();
			if (b.left() <= 1) {
				painter.fill(0, 0, 2, height, ClickGuiTheme.MODULE_ENABLED);
			}
			if (b.right() >= width - 1) {
				painter.fill(width - 2, 0, 2, height, ClickGuiTheme.MODULE_ENABLED);
			}
			if (b.top() <= 1) {
				painter.fill(0, 0, width, 2, ClickGuiTheme.MODULE_ENABLED);
			}
			if (b.bottom() >= height - 1) {
				painter.fill(0, height - 2, width, 2, ClickGuiTheme.MODULE_ENABLED);
			}
		}

		// 2. 渲染所有 HUD 元素预览与轻量边框指示
		for (HudEntry entry : entries) {
			HudElementSize size = entry.preview.measure();
			entry.drag.layout(width, height, size.width(), size.height());
			Rect bounds = entry.drag.bounds();

			boolean isTarget = (activeDragging == entry && entry.drag.isDragging());
			int borderColor = isTarget ? 0xFFEBD23D : 0x605FA8FF;

			// 绘制 HUD 元素实际内容（自带背景）
			entry.preview.extract(graphics, bounds);

			// 绘制轻量边界提示线
			painter.outline(
				bounds.left(),
				bounds.top(),
				(int) bounds.width(),
				(int) bounds.height(),
				borderColor
			);
		}

		// 3. 顶部提示条
		String tip = ClientI18n.translate("client.gui.hud_editor.hint");
		int tipW = painter.textWidth(tip);
		int tipX = (width - tipW) / 2;
		painter.fill(tipX - 8, 8, tipW + 16, 16, 0xCC000000);
		painter.text(tip, tipX, 12, ClickGuiTheme.TEXT_PRIMARY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			// 从后向前寻找被点击的 HUD 元素
			for (int i = entries.size() - 1; i >= 0; i--) {
				HudEntry entry = entries.get(i);
				HudElementSize size = entry.preview.measure();
				entry.drag.layout(width, height, size.width(), size.height());

				if (entry.drag.begin(event.x(), event.y())) {
					activeDragging = entry;
					return true;
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		if (event.button() == 0 && activeDragging != null && activeDragging.drag.isDragging()) {
			HudElementSize size = activeDragging.preview.measure();
			activeDragging.drag.layout(width, height, size.width(), size.height());
			activeDragging.drag.move(event.x(), event.y());
			return true;
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0 && activeDragging != null) {
			boolean ended = activeDragging.drag.end();
			activeDragging = null;
			if (ended) {
				return true;
			}
		}
		return super.mouseReleased(event);
	}

	@Override
	public void onClose() {
		for (HudEntry entry : entries) {
			if (entry.drag.isDragging()) {
				entry.drag.end();
			}
		}
		activeDragging = null;
		minecraft.setScreenAndShow(parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}
}
