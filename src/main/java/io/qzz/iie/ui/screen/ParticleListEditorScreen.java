package io.qzz.iie.ui.screen;

import io.qzz.iie.module.impl.render.norender.ParticleBlacklistSetting;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 动态粒子黑名单管理子页。
 *
 * <p>从 {@link BuiltInRegistries#PARTICLE_TYPE} 动态枚举所有已注册粒子（包含其他模组），
 * 支持搜索、单项切换与一键清空。</p>
 */
public final class ParticleListEditorScreen extends Screen {
	private static final int ITEM_HEIGHT = 18;
	private static final int CARD_WIDTH = 360;

	private final ParticleBlacklistSetting setting;
	private final Screen parent;

	private final List<String> allParticles = new ArrayList<>();
	private final List<String> filteredParticles = new ArrayList<>();

	private EditBox searchBox;
	private String searchQuery = "";
	private int scrollOffset;

	public ParticleListEditorScreen(ParticleBlacklistSetting setting, Screen parent) {
		super(Component.translatable("client.gui.no_render.particle_editor.title"));
		this.setting = Objects.requireNonNull(setting, "setting");
		this.parent = parent;
	}

	@Override
	protected void init() {
		allParticles.clear();
		for (Identifier id : BuiltInRegistries.PARTICLE_TYPE.keySet()) {
			allParticles.add(id.toString());
		}
		allParticles.sort(String::compareTo);

		int cardX = (width - CARD_WIDTH) / 2;
		int cardY = 30;
		int cardHeight = height - 60;

		// 搜索框
		searchBox = new EditBox(font, cardX + 16, cardY + 36, CARD_WIDTH - 32, 18, Component.literal("Search"));
		searchBox.setValue(searchQuery);
		searchBox.setResponder(query -> {
			this.searchQuery = query;
			this.scrollOffset = 0;
			updateFilter();
		});
		addRenderableWidget(searchBox);

		// 清空按钮
		addRenderableWidget(
			Button.builder(
				Component.translatable("client.gui.no_render.particle_editor.clear"),
				b -> setting.clear()
			).bounds(cardX + 16, cardY + cardHeight - 28, 120, 20).build()
		);

		// 返回按钮
		addRenderableWidget(
			Button.builder(
				Component.translatable("client.gui.no_render.particle_editor.back"),
				b -> onClose()
			).bounds(cardX + CARD_WIDTH - 136, cardY + cardHeight - 28, 120, 20).build()
		);

		updateFilter();
	}

	private void updateFilter() {
		filteredParticles.clear();
		String query = searchQuery.trim().toLowerCase(Locale.ROOT);
		for (String id : allParticles) {
			if (query.isEmpty() || id.toLowerCase(Locale.ROOT).contains(query)) {
				filteredParticles.add(id);
			}
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		UiPainter painter = new UiPainter(graphics, font);
		painter.fill(0, 0, width, height, ClickGuiTheme.OVERLAY);

		int cardX = (width - CARD_WIDTH) / 2;
		int cardY = 30;
		int cardHeight = height - 60;

		// 主卡片背景
		painter.roundedRect(cardX, cardY, CARD_WIDTH, cardHeight, 10, ClickGuiTheme.PANEL_BORDER);
		painter.roundedRect(cardX + 1, cardY + 1, CARD_WIDTH - 2, cardHeight - 2, 9, ClickGuiTheme.CONTENT);

		// 标题
		String titleText = io.qzz.iie.i18n.ClientI18n.translate("client.gui.no_render.particle_editor.title");
		String statsText = String.format(Locale.ROOT, "(%d/%d blocked)", setting.value().size(), allParticles.size());
		painter.text(titleText, cardX + 16, cardY + 14, ClickGuiTheme.TEXT_PRIMARY);
		painter.text(statsText, cardX + 24 + painter.textWidth(titleText), cardY + 14, ClickGuiTheme.TEXT_SECONDARY);

		// 粒子列表区域
		int listTop = cardY + 62;
		int listHeight = cardHeight - 96;
		int listWidth = CARD_WIDTH - 32;
		painter.fill(cardX + 16, listTop, listWidth, listHeight, ClickGuiTheme.CONTROL_DARK);

		int visibleCount = listHeight / ITEM_HEIGHT;
		int maxScroll = Math.max(0, filteredParticles.size() - visibleCount);
		scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);

		for (int i = 0; i < visibleCount; i++) {
			int index = scrollOffset + i;
			if (index >= filteredParticles.size()) {
				break;
			}
			String particleId = filteredParticles.get(index);
			int itemY = listTop + i * ITEM_HEIGHT;
			boolean itemHovered = mouseX >= cardX + 16 && mouseX <= cardX + 16 + listWidth
				&& mouseY >= itemY && mouseY < itemY + ITEM_HEIGHT;

			if (itemHovered) {
				painter.fill(cardX + 16, itemY, listWidth, ITEM_HEIGHT, ClickGuiTheme.MODULE_HOVER);
			}

			boolean blocked = setting.isBlocked(particleId);
			int nameColor = blocked ? ClickGuiTheme.SETTING_FALSE : ClickGuiTheme.SETTING_TEXT;
			painter.text(particleId, cardX + 20, itemY + 4, nameColor);

			String status = blocked
				? io.qzz.iie.i18n.ClientI18n.translate("client.gui.no_render.particle_editor.blocked")
				: io.qzz.iie.i18n.ClientI18n.translate("client.gui.no_render.particle_editor.allowed");
			int statusColor = blocked ? ClickGuiTheme.SETTING_FALSE : ClickGuiTheme.SETTING_TRUE;
			int statusX = cardX + 16 + listWidth - painter.textWidth(status) - 8;
			painter.text(status, statusX, itemY + 4, statusColor);
		}

		// 滚动条
		if (maxScroll > 0) {
			int scrollBarHeight = Math.max(16, (int) ((float) visibleCount / filteredParticles.size() * listHeight));
			int scrollBarY = listTop + (int) ((float) scrollOffset / maxScroll * (listHeight - scrollBarHeight));
			painter.fill(cardX + 16 + listWidth - 3, scrollBarY, 2, scrollBarHeight, ClickGuiTheme.PANEL_BORDER);
		}

		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		int cardX = (width - CARD_WIDTH) / 2;
		int cardY = 30;
		int cardHeight = height - 60;
		int listTop = cardY + 62;
		int listHeight = cardHeight - 96;
		int listWidth = CARD_WIDTH - 32;

		if (event.x() >= cardX + 16 && event.x() <= cardX + 16 + listWidth
			&& event.y() >= listTop && event.y() <= listTop + listHeight) {
			int clickedRow = (int) (event.y() - listTop) / ITEM_HEIGHT;
			int index = scrollOffset + clickedRow;
			if (index >= 0 && index < filteredParticles.size()) {
				setting.toggle(filteredParticles.get(index));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
			return true;
		}
		int cardX = (width - CARD_WIDTH) / 2;
		int cardY = 30;
		int cardHeight = height - 60;
		int listTop = cardY + 62;
		int listHeight = cardHeight - 96;
		int listWidth = CARD_WIDTH - 32;

		if (mouseX >= cardX + 16 && mouseX <= cardX + 16 + listWidth
			&& mouseY >= listTop && mouseY <= listTop + listHeight) {
			scrollOffset -= (int) Math.signum(verticalAmount) * 2;
			int visibleCount = listHeight / ITEM_HEIGHT;
			int maxScroll = Math.max(0, filteredParticles.size() - visibleCount);
			scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == 256) { // GLFW_KEY_ESCAPE
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		if (minecraft != null) {
			minecraft.setScreenAndShow(parent);
		}
	}
}
