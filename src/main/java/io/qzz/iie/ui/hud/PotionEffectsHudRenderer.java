package io.qzz.iie.ui.hud;

import io.qzz.iie.api.hud.HudElementPreview;
import io.qzz.iie.api.hud.HudElementSize;
import io.qzz.iie.api.hud.HudPositionLayout;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.i18n.ClientI18n;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.UiPainter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * 药水效果状态与剩余时间 HUD 渲染器（带原版药水小图标）。
 */
public final class PotionEffectsHudRenderer implements HudElementPreview {
	private static final int ROW_HEIGHT = 14;
	private static final int ICON_SIZE = 11;

	private final BooleanSetting enabledSetting;
	private final HudPositionSetting positionSetting;
	private final BooleanSupplier editorActive;

	public PotionEffectsHudRenderer(
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
		Minecraft client = Minecraft.getInstance();
		List<EffectDisplay> effects = getActiveEffects(client.player);
		int maxW = 90;
		for (EffectDisplay eff : effects) {
			int w = client.font.width(eff.name() + " " + eff.duration());
			maxW = Math.max(maxW, w + ICON_SIZE + 16);
		}
		int count = Math.max(1, effects.size());
		return new HudElementSize(maxW, count * ROW_HEIGHT + 4);
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

		int x = bounds.left() + 2;
		int y = bounds.top() + 2;
		int w = (int) bounds.width() - 4;
		int h = (int) bounds.height() - 4;

		painter.fill(x - 2, y - 2, w + 4, h + 4, 0x60000000);

		List<EffectDisplay> effects = getActiveEffects(client.player);
		for (int i = 0; i < effects.size(); i++) {
			EffectDisplay eff = effects.get(i);
			int rowY = y + i * ROW_HEIGHT;

			// 1. 绘制原版药水小图标
			if (eff.effectId() != null) {
				painter.renderEffectIcon(eff.effectId(), x + 2, rowY + 1, ICON_SIZE, ICON_SIZE);
			}

			// 2. 绘制名称与持续时间
			painter.text(eff.name(), x + ICON_SIZE + 6, rowY + 3, 0xFFFFFFFF);
			int durW = painter.textWidth(eff.duration());
			painter.text(eff.duration(), x + w - durW - 2, rowY + 3, 0xFF7396FF);
		}
	}

	private record EffectDisplay(Identifier effectId, String name, String duration) {
	}

	private List<EffectDisplay> getActiveEffects(LocalPlayer player) {
		List<EffectDisplay> list = new ArrayList<>();
		if (player != null) {
			Collection<MobEffectInstance> active = player.getActiveEffects();
			for (MobEffectInstance instance : active) {
				MobEffect effect = instance.getEffect().value();
				Identifier effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect);
				String name = effect.getDisplayName().getString();
				if (instance.getAmplifier() > 0) {
					name += " " + toRoman(instance.getAmplifier() + 1);
				}
				String durationStr = formatDuration(instance.getDuration());
				list.add(new EffectDisplay(effectId, name, durationStr));
			}
		}

		if (list.isEmpty()) {
			// 预览数据
			list.add(new EffectDisplay(
				Identifier.fromNamespaceAndPath("minecraft", "speed"),
				ClientI18n.translate("client.hud.potion.speed") + " II",
				"01:45"
			));
			list.add(new EffectDisplay(
				Identifier.fromNamespaceAndPath("minecraft", "strength"),
				ClientI18n.translate("client.hud.potion.strength") + " I",
				"00:30"
			));
		}

		return list;
	}

	private static String formatDuration(int ticks) {
		if (ticks > 32000) {
			return "**:**";
		}
		int totalSeconds = ticks / 20;
		int minutes = totalSeconds / 60;
		int seconds = totalSeconds % 60;
		return String.format("%02d:%02d", minutes, seconds);
	}

	private static String toRoman(int number) {
		return switch (number) {
			case 1 -> "I";
			case 2 -> "II";
			case 3 -> "III";
			case 4 -> "IV";
			case 5 -> "V";
			default -> String.valueOf(number);
		};
	}
}
