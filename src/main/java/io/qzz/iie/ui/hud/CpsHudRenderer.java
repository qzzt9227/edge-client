package io.qzz.iie.ui.hud;

import io.qzz.iie.api.hud.HudElementPreview;
import io.qzz.iie.api.hud.HudElementSize;
import io.qzz.iie.api.hud.HudPositionLayout;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * 实时显示左键与右键 CPS 的 HUD 渲染器与预览提供者。
 */
public final class CpsHudRenderer implements HudElementPreview {
	private static final int DEFAULT_WIDTH = 76;
	private static final int DEFAULT_HEIGHT = 16;

	private final BooleanSetting enabledSetting;
	private final HudPositionSetting positionSetting;
	private final BooleanSupplier editorActive;

	public CpsHudRenderer(
		BooleanSetting enabledSetting,
		HudPositionSetting positionSetting,
		BooleanSupplier editorActive
	) {
		this.enabledSetting = Objects.requireNonNull(enabledSetting, "enabledSetting");
		this.positionSetting = Objects.requireNonNull(positionSetting, "positionSetting");
		this.editorActive = Objects.requireNonNull(editorActive, "editorActive");
	}

	public void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (editorActive.getAsBoolean() || !enabledSetting.value()) {
			return;
		}
		HudElementSize size = measure();
		Rect bounds = HudPositionLayout.resolve(
			positionSetting.value(),
			graphics.guiWidth(),
			graphics.guiHeight(),
			size.width(),
			size.height()
		);
		extract(graphics, bounds);
	}

	@Override
	public HudElementSize measure() {
		return new HudElementSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	@Override
	public void extract(GuiGraphicsExtractor graphics, Rect bounds) {
		Minecraft client = Minecraft.getInstance();
		UiPainter painter = new UiPainter(
			graphics,
			client.font,
			1.0,
			io.qzz.iie.font.ClientFontManager.getActiveFontDescription()
		);

		int x = bounds.left();
		int y = bounds.top();
		int w = (int) bounds.width();
		int h = (int) bounds.height();

		// 半透明背景条
		painter.fill(x, y, w, h, 0x60000000);

		// 主题色侧边指示条
		painter.fill(x, y, 2, h, ClickGuiTheme.MODULE_ENABLED);

		int left = CpsTracker.getLeftCps();
		int right = CpsTracker.getRightCps();
		String text = String.format(Locale.ROOT, "%d | %d CPS", left, right);

		int textW = painter.textWidth(text);
		int textX = x + (w - textW) / 2 + 1;
		int textY = y + (h - 8) / 2;

		painter.text(text, textX, textY, ClickGuiTheme.TEXT_PRIMARY);
	}
}
