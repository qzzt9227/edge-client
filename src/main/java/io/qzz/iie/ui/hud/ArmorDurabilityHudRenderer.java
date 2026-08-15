package io.qzz.iie.ui.hud;

import io.qzz.iie.api.hud.HudElementPreview;
import io.qzz.iie.api.hud.HudElementSize;
import io.qzz.iie.api.hud.HudPositionLayout;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.UiPainter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * 盔甲耐久度 HUD 渲染器：
 * 穿戴时显示单品图标与耐久数字；未穿戴时显示原版空槽位图标。
 */
public final class ArmorDurabilityHudRenderer implements HudElementPreview {
	private static final int ROW_HEIGHT = 18;
	private static final int ITEM_SIZE = 16;

	private final BooleanSetting enabledSetting;
	private final HudPositionSetting positionSetting;
	private final BooleanSupplier editorActive;

	private static final EquipmentSlot[] ARMOR_SLOTS = {
		EquipmentSlot.HEAD,
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET
	};

	private static final Identifier[] EMPTY_SLOT_SPRITES = {
		Identifier.fromNamespaceAndPath("minecraft", "container/slot/helmet"),
		Identifier.fromNamespaceAndPath("minecraft", "container/slot/chestplate"),
		Identifier.fromNamespaceAndPath("minecraft", "container/slot/leggings"),
		Identifier.fromNamespaceAndPath("minecraft", "container/slot/boots")
	};

	public ArmorDurabilityHudRenderer(
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
		return new HudElementSize(64, ROW_HEIGHT * 4 + 4);
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

		LocalPlayer player = client.player;

		for (int i = 0; i < 4; i++) {
			int rowY = y + i * ROW_HEIGHT;

			ItemStack equipped = (player != null)
				? player.getItemBySlot(ARMOR_SLOTS[i])
				: ItemStack.EMPTY;

			boolean hasEquipped = !equipped.isEmpty();

			if (hasEquipped) {
				// 1. 穿戴时绘制物品图标（忽略附魔光效）
				painter.renderItem(new ItemStack(equipped.getItem()), x + 2, rowY + 1);

				// 2. 绘制耐久数字
				int maxDamage = equipped.getMaxDamage();
				int damage = equipped.getDamageValue();
				int remaining = Math.max(0, maxDamage - damage);
				double ratio = maxDamage > 0 ? (double) remaining / maxDamage : 1.0;

				String valText = remaining + "/" + maxDamage;
				painter.text(valText, x + ITEM_SIZE + 6, rowY + 5, getDurabilityColor(ratio));
			} else {
				// 未穿戴时绘制原版空槽位图标
				painter.renderSprite(EMPTY_SLOT_SPRITES[i], x + 2, rowY + 1, ITEM_SIZE, ITEM_SIZE);
				painter.text("-", x + ITEM_SIZE + 6, rowY + 5, 0xFF888888);
			}
		}
	}

	private static int getDurabilityColor(double ratio) {
		if (ratio > 0.6) {
			return 0xFF44E044; // 绿色
		} else if (ratio > 0.25) {
			return 0xFFE0E044; // 黄色
		} else {
			return 0xFFE04444; // 红色
		}
	}
}
