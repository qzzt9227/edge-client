package io.qzz.iie.module.impl.render.betterhealth;

import io.qzz.iie.api.hud.HudElementPreview;
import io.qzz.iie.api.hud.HudElementSize;
import io.qzz.iie.api.hud.HudPositionLayout;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.UiPainter;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * 通过 Fabric HUD API 绘制血量数字，同时作为通用位置编辑器的预览。
 */
public final class BetterHealthBarHudRenderer implements HudElementPreview {
	private static final int HORIZONTAL_PADDING = 4;
	private static final int VERTICAL_PADDING = 3;

	private final BetterHealthBarModule module;
	private final BooleanSupplier editorActive;

	public BetterHealthBarHudRenderer(
		BetterHealthBarModule module,
		BooleanSupplier editorActive
	) {
		this.module = Objects.requireNonNull(module, "module");
		this.editorActive = Objects.requireNonNull(editorActive, "editorActive");
	}

	public void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (editorActive.getAsBoolean() || !shouldRender()) {
			return;
		}
		HudElementSize size = measure();
		Rect bounds = HudPositionLayout.resolve(
			module.numberPosition().value(),
			graphics.guiWidth(),
			graphics.guiHeight(),
			size.width(),
			size.height()
		);
		extract(graphics, bounds);
	}

	@Override
	public HudElementSize measure() {
		Minecraft client = Minecraft.getInstance();
		double scale = module.numberScale().value();
		int textWidth = (int) Math.round(client.font.width(currentText()) * scale);
		int textHeight = (int) Math.round(client.font.lineHeight * scale);
		return new HudElementSize(
			Math.max(1, textWidth + HORIZONTAL_PADDING * 2),
			Math.max(1, textHeight + VERTICAL_PADDING * 2)
		);
	}

	@Override
	public void extract(GuiGraphicsExtractor graphics, Rect bounds) {
		UiPainter painter = new UiPainter(
			graphics,
			Minecraft.getInstance().font,
			module.numberScale().value(),
			io.qzz.iie.font.ClientFontManager.getActiveFontDescription()
		);
		painter.text(
			currentText(),
			bounds.left() + HORIZONTAL_PADDING,
			bounds.top() + VERTICAL_PADDING,
			0xFFFFFFFF
		);
	}

	private boolean shouldRender() {
		LocalPlayer player = Minecraft.getInstance().player;
		return player != null && BetterHealthBarPolicy.shouldShowNumber(
			module.isEnabled(),
			module.thresholdRows().value(),
			player.getMaxHealth()
		);
	}

	private String currentText() {
		LocalPlayer player = Minecraft.getInstance().player;
		return BetterHealthBarPolicy.formatHealth(player == null ? 20.0F : player.getHealth());
	}
}
