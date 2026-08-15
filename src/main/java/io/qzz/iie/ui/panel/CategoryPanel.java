package io.qzz.iie.ui.panel;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleCategory;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class CategoryPanel {
	public static final int DEFAULT_WIDTH = 125;
	public static final int HEADER_HEIGHT = 15;
	public static final int MAX_BODY_HEIGHT = 280;

	private final ModuleCategory category;
	private final List<ModulePanelItem> modules = new ArrayList<>();

	private int x;
	private int y;
	private int width = DEFAULT_WIDTH;
	private boolean opened = true;

	private boolean dragging;
	private int dragStartX;
	private int dragStartY;

	private double scroll;
	private double animScroll;

	public CategoryPanel(
		ModuleCategory category,
		List<Module> categoryModules,
		ModuleManager moduleManager,
		InlineSettingFactory settingFactory,
		int initialX,
		int initialY
	) {
		this.category = Objects.requireNonNull(category, "category");
		this.x = initialX;
		this.y = initialY;

		for (Module module : categoryModules) {
			modules.add(new ModulePanelItem(module, moduleManager, settingFactory));
		}
	}

	public ModuleCategory category() {
		return category;
	}

	public int x() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int y() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int width() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean opened) {
		this.opened = opened;
	}

	public List<ModulePanelItem> modules() {
		return modules;
	}

	public int totalContentHeight() {
		int total = 0;
		for (ModulePanelItem item : modules) {
			total += item.height();
		}
		return total;
	}

	public void render(
		UiPainter painter,
		int mouseX,
		int mouseY,
		float delta,
		long time,
		AtomicInteger colorIndex,
		int screenHeight
	) {
		int effectiveMaxHeight = Math.max(100, Math.min(MAX_BODY_HEIGHT, screenHeight - y - 30));
		int contentH = totalContentHeight();
		double maxScroll = Math.max(0.0, contentH - effectiveMaxHeight);

		scroll = Math.clamp(scroll, 0.0, maxScroll);
		animScroll += (scroll - animScroll) * Math.min(1.0, Math.max(0.05, delta * 18.0));
		if (Math.abs(scroll - animScroll) < 0.05) {
			animScroll = scroll;
		}

		boolean hoveredHeader = mouseX >= x && mouseX <= x + width
			&& mouseY >= y && mouseY < y + HEADER_HEIGHT;

		// 1. Render Header
		int headerColor = hoveredHeader ? ClickGuiTheme.PANEL_HEADER_HOVER : ClickGuiTheme.PANEL_HEADER;
		painter.fill(x, y, width, HEADER_HEIGHT, headerColor);

		String title = io.qzz.iie.i18n.ClientI18n.translate(category.translationKey());
		int titleY = y + (HEADER_HEIGHT - painter.lineHeight()) / 2;
		painter.text(title, x + 4, titleY, ClickGuiTheme.TEXT_PRIMARY);

		String collapseSymbol = opened ? "-" : "+";
		int symbolWidth = painter.textWidth(collapseSymbol);
		painter.text(collapseSymbol, x + width - symbolWidth - 4, titleY, ClickGuiTheme.TEXT_PRIMARY);

		// 2. Render Body
		if (opened && !modules.isEmpty()) {
			int displayHeight = Math.min(contentH, effectiveMaxHeight);
			painter.fill(x, y + HEADER_HEIGHT, width, displayHeight + 2, ClickGuiTheme.PANEL_BODY);

			painter.enableScissor(x, y + HEADER_HEIGHT, x + width, y + HEADER_HEIGHT + displayHeight + 2);
			try {
				int currentItemY = y + HEADER_HEIGHT + 1 - (int) Math.round(animScroll);
				for (ModulePanelItem item : modules) {
					int h = item.height();
					if (currentItemY + h >= y + HEADER_HEIGHT && currentItemY <= y + HEADER_HEIGHT + displayHeight + 2) {
						item.render(painter, x, currentItemY, width, mouseX, mouseY, time, colorIndex);
					}
					currentItemY += h;
				}
			} finally {
				painter.disableScissor();
			}

			// Scrollbar
			if (contentH > effectiveMaxHeight) {
				int thumbHeight = Math.max(8, (int) Math.round((double) effectiveMaxHeight * effectiveMaxHeight / contentH));
				int travel = effectiveMaxHeight - thumbHeight;
				int thumbY = y + HEADER_HEIGHT + 1 + (int) Math.round(travel * (animScroll / maxScroll));
				painter.fill(x + width - 2, thumbY, 2, thumbHeight, ClickGuiTheme.SCROLLBAR);
			}
		}
	}

	public java.util.Set<String> getExpandedModuleIds() {
		java.util.Set<String> set = new java.util.HashSet<>();
		for (ModulePanelItem item : modules) {
			if (item.isExpanded()) {
				set.add(item.module().id().toString());
			}
		}
		return set;
	}

	public void syncPositionState() {
		CategoryPositionManager.updateState(category.id(), x, y, opened, getExpandedModuleIds());
		CategoryPositionManager.save();
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Header click
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + HEADER_HEIGHT) {
			if (mouseX >= x + width - 14) {
				opened = !opened;
				syncPositionState();
				return true;
			}
			if (button == 0) {
				dragging = true;
				dragStartX = (int) (mouseX - x);
				dragStartY = (int) (mouseY - y);
				return true;
			} else if (button == 1) {
				opened = !opened;
				syncPositionState();
				return true;
			}
		}

		// Body click
		if (opened) {
			int effectiveMaxHeight = MAX_BODY_HEIGHT;
			int contentH = totalContentHeight();
			int displayHeight = Math.min(contentH, effectiveMaxHeight);

			if (mouseX >= x && mouseX <= x + width && mouseY >= y + HEADER_HEIGHT && mouseY <= y + HEADER_HEIGHT + displayHeight + 2) {
				int currentItemY = y + HEADER_HEIGHT + 1 - (int) Math.round(animScroll);
				for (ModulePanelItem item : modules) {
					int h = item.height();
					if (item.mouseClicked(mouseX, mouseY, button, x, currentItemY, width)) {
						syncPositionState();
						return true;
					}
					currentItemY += h;
				}
				return true;
			}
		}
		return false;
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0 && dragging) {
			dragging = false;
			syncPositionState();
		} else if (button == 0) {
			dragging = false;
		}
		if (opened) {
			for (ModulePanelItem item : modules) {
				item.mouseReleased(mouseX, mouseY, button);
			}
		}
	}

	public void mouseDragged(
		double mouseX,
		double mouseY,
		double deltaX,
		double deltaY,
		int button,
		int screenWidth,
		int screenHeight
	) {
		if (dragging) {
			x = Math.clamp((int) (mouseX - dragStartX), 0, Math.max(0, screenWidth - width));
			y = Math.clamp((int) (mouseY - dragStartY), 0, Math.max(0, screenHeight - HEADER_HEIGHT));
		}
		if (opened) {
			int currentItemY = y + HEADER_HEIGHT + 1 - (int) Math.round(animScroll);
			for (ModulePanelItem item : modules) {
				item.mouseDragged(mouseX, mouseY, deltaX, deltaY, button, x, currentItemY, width);
				currentItemY += item.height();
			}
		}
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount, int screenHeight) {
		if (!opened) {
			return false;
		}
		int effectiveMaxHeight = Math.max(100, Math.min(MAX_BODY_HEIGHT, screenHeight - y - 30));
		int contentH = totalContentHeight();
		if (contentH <= effectiveMaxHeight) {
			return false;
		}

		if (mouseX >= x && mouseX <= x + width
			&& mouseY >= y + HEADER_HEIGHT && mouseY <= y + HEADER_HEIGHT + effectiveMaxHeight + 2) {
			double maxScroll = contentH - effectiveMaxHeight;
			scroll -= amount * 16.0;
			scroll = Math.clamp(scroll, 0.0, maxScroll);
			return true;
		}
		return false;
	}

	public boolean keyPressed(int keyCode, int scancode, int modifiers) {
		if (opened) {
			for (ModulePanelItem item : modules) {
				if (item.keyPressed(keyCode, scancode, modifiers)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean charTyped(char chr) {
		if (opened) {
			for (ModulePanelItem item : modules) {
				if (item.charTyped(chr)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isAnyKeybindListening() {
		if (opened) {
			for (ModulePanelItem item : modules) {
				if (item.isAnyKeybindListening()) {
					return true;
				}
			}
		}
		return false;
	}
}
