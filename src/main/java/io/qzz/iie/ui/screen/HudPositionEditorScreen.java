package io.qzz.iie.ui.screen;

import io.qzz.iie.api.hud.HudElementPreview;
import io.qzz.iie.api.hud.HudElementSize;
import io.qzz.iie.api.hud.HudPositionDrag;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.UiPainter;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * 所有 HUD 元素共用的位置编辑屏幕；拖动结束时才写入设置。
 */
public final class HudPositionEditorScreen extends Screen {
	private final HudElementPreview preview;
	private final HudPositionDrag drag;
	private final Screen parent;

	public HudPositionEditorScreen(
		HudPositionSetting setting,
		HudElementPreview preview,
		Screen parent
	) {
		super(Component.translatable("client.gui.hud_position.title"));
		this.preview = Objects.requireNonNull(preview, "preview");
		this.drag = new HudPositionDrag(Objects.requireNonNull(setting, "setting"));
		this.parent = parent;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
	}

	@Override
	public void extractTransparentBackground(GuiGraphicsExtractor graphics) {
	}

	@Override
	public void extractBlurredBackground(GuiGraphicsExtractor graphics) {
	}

	@Override
	public void extractRenderState(
		GuiGraphicsExtractor graphics,
		int mouseX,
		int mouseY,
		float delta
	) {
		Rect bounds = updateLayout();
		UiPainter painter = new UiPainter(
			graphics,
			font,
			1.0,
			io.qzz.iie.font.ClientFontManager.getActiveFontDescription()
		);
		painter.roundedRectWithBorder(
			bounds.left(),
			bounds.top(),
			(int) bounds.width(),
			(int) bounds.height(),
			4,
			1,
			0x66121824,
			drag.isDragging() ? 0xFFEBD23D : 0xCCFFFFFF
		);
		preview.extract(graphics, bounds);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			updateLayout();
			return drag.begin(event.x(), event.y())
				|| super.mouseClicked(event, doubleClick);
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		if (event.button() == 0 && drag.isDragging()) {
			updateLayout();
			drag.move(event.x(), event.y());
			return true;
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0 && drag.end()) {
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public void onClose() {
		drag.cancel();
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

	private Rect updateLayout() {
		HudElementSize size = preview.measure();
		drag.layout(width, height, size.width(), size.height());
		return drag.bounds();
	}
}
