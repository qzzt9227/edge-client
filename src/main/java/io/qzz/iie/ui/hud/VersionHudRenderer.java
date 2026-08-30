package io.qzz.iie.ui.hud;

import io.qzz.iie.Client;
import io.qzz.iie.api.hud.HudElementPreview;
import io.qzz.iie.api.hud.HudElementSize;
import io.qzz.iie.api.hud.HudPositionLayout;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * 客户端版本 HUD 渲染器与预览提供者：读取 fabric.mod.json 元数据展示客户端名称与版本。
 */
public final class VersionHudRenderer implements HudElementPreview {
	private static final int DEFAULT_HEIGHT = 16;
	private static final int PADDING_X = 6;

	private final BooleanSetting enabledSetting;
	private final HudPositionSetting positionSetting;
	private final BooleanSupplier editorActive;
	private final String displayText;

	public VersionHudRenderer(
		BooleanSetting enabledSetting,
		HudPositionSetting positionSetting,
		BooleanSupplier editorActive
	) {
		this(
			enabledSetting,
			positionSetting,
			editorActive,
			resolveModDisplayText()
		);
	}

	public VersionHudRenderer(
		BooleanSetting enabledSetting,
		HudPositionSetting positionSetting,
		BooleanSupplier editorActive,
		String displayText
	) {
		this.enabledSetting = Objects.requireNonNull(enabledSetting, "enabledSetting");
		this.positionSetting = Objects.requireNonNull(positionSetting, "positionSetting");
		this.editorActive = Objects.requireNonNull(editorActive, "editorActive");
		this.displayText = Objects.requireNonNull(displayText, "displayText");
	}

	public String displayText() {
		return displayText;
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
		Minecraft client = Minecraft.getInstance();
		Font font = client != null ? client.font : null;
		int textWidth = font != null ? font.width(displayText) : (displayText.length() * 6);
		return new HudElementSize(textWidth + (PADDING_X * 2), DEFAULT_HEIGHT);
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

		// 半透明深色背景
		painter.fill(x, y, w, h, 0x60000000);

		// 主题强调色侧边指示条
		painter.fill(x, y, 2, h, ClickGuiTheme.MODULE_ENABLED);

		int textY = y + (h - 8) / 2;
		painter.text(displayText, x + PADDING_X, textY, ClickGuiTheme.TEXT_PRIMARY);
	}

	public static String resolveModDisplayText() {
		return FabricLoader.getInstance()
			.getModContainer(Client.MOD_ID)
			.map(container -> {
				String name = container.getMetadata().getName();
				String version = container.getMetadata().getVersion().getFriendlyString();
				return name + " v" + version;
			})
			.orElse("Edge Client v1.0.0");
	}
}
