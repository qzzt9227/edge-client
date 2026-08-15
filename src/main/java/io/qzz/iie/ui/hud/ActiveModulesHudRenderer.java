package io.qzz.iie.ui.hud;

import io.qzz.iie.api.hud.HudElementPreview;
import io.qzz.iie.api.hud.HudElementSize;
import io.qzz.iie.api.hud.HudPositionLayout;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.i18n.ClientI18n;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * 启用功能列表（ArrayList）HUD 渲染器与预览提供者。
 */
public final class ActiveModulesHudRenderer implements HudElementPreview {
	private static final int ROW_HEIGHT = 11;

	private final ModuleManager moduleManager;
	private final BooleanSetting enabledSetting;
	private final HudPositionSetting positionSetting;
	private final BooleanSupplier editorActive;

	public ActiveModulesHudRenderer(
		ModuleManager moduleManager,
		BooleanSetting enabledSetting,
		HudPositionSetting positionSetting,
		BooleanSupplier editorActive
	) {
		this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager");
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
		Minecraft client = Minecraft.getInstance();
		List<String> names = getActiveModuleNames();
		int maxW = 60;
		for (String name : names) {
			int w = client.font.width(name);
			maxW = Math.max(maxW, w + 10);
		}
		int count = Math.max(1, names.size());
		return new HudElementSize(maxW, count * ROW_HEIGHT + 2);
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

		List<String> names = getActiveModuleNames();
		boolean alignRight = positionSetting.value().x() > 0.5;

		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			int rowY = y + i * ROW_HEIGHT;
			int textW = painter.textWidth(name);

			int rowX = alignRight ? x + w - textW - 8 : x;
			int rowWidth = textW + 8;

			// 半透明背景条
			painter.fill(rowX, rowY, rowWidth, ROW_HEIGHT, 0x60000000);

			// 主题色侧边修饰条
			if (alignRight) {
				painter.fill(rowX + rowWidth - 2, rowY, 2, ROW_HEIGHT, ClickGuiTheme.MODULE_ENABLED);
				painter.text(name, rowX + 3, rowY + 1, ClickGuiTheme.MODULE_ENABLED);
			} else {
				painter.fill(rowX, rowY, 2, ROW_HEIGHT, ClickGuiTheme.MODULE_ENABLED);
				painter.text(name, rowX + 5, rowY + 1, ClickGuiTheme.MODULE_ENABLED);
			}
		}
	}

	private List<String> getActiveModuleNames() {
		List<String> list = new ArrayList<>();
		for (Module module : moduleManager.modules()) {
			if (module.isEnabled() && module.metadata().toggleable()) {
				list.add(ClientI18n.translate(module.metadata().nameTranslationKey()));
			}
		}

		if (list.isEmpty()) {
			list.add(ClientI18n.translate("client.module.fullbright.name"));
			list.add(ClientI18n.translate("client.module.auto_walk.name"));
			list.add(ClientI18n.translate("client.module.auto_web.name"));
			list.add(ClientI18n.translate("client.module.safe_walk_plus.name"));
		}

		Minecraft client = Minecraft.getInstance();
		list.sort(Comparator.comparingInt((String name) -> client.font.width(name)).reversed());
		return list;
	}
}
