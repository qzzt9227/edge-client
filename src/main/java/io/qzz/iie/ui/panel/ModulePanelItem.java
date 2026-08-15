package io.qzz.iie.ui.panel;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.setting.Setting;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class ModulePanelItem {
	private static final int HEADER_HEIGHT = 14;

	private final Module module;
	private final ModuleManager moduleManager;
	private final List<InlineSettingItem> settings = new ArrayList<>();
	private boolean expanded;

	public ModulePanelItem(
		Module module,
		ModuleManager moduleManager,
		InlineSettingFactory settingFactory
	) {
		this.module = Objects.requireNonNull(module, "module");
		this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager");
		Objects.requireNonNull(settingFactory, "settingFactory");

		for (Setting<?> setting : module.settings()) {
			settings.add(settingFactory.create(setting));
		}
	}

	public Module module() {
		return module;
	}

	public List<InlineSettingItem> settings() {
		return settings;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public int height() {
		if (!expanded || settings.isEmpty()) {
			return HEADER_HEIGHT;
		}
		int total = HEADER_HEIGHT;
		for (InlineSettingItem item : settings) {
			if (item.isVisible()) {
				total += item.height();
			}
		}
		return total;
	}

	public void render(
		UiPainter painter,
		int x,
		int y,
		int width,
		int mouseX,
		int mouseY,
		long time,
		AtomicInteger colorIndex
	) {
		boolean hoveredHeader = mouseX >= x && mouseX <= x + width
			&& mouseY >= y && mouseY < y + HEADER_HEIGHT;

		if (hoveredHeader) {
			painter.fill(x, y, width, HEADER_HEIGHT, ClickGuiTheme.MODULE_HOVER);
		}

		String name = io.qzz.iie.i18n.ClientI18n.translate(module.metadata().nameTranslationKey());
		int textWidth = painter.textWidth(name);
		int availableWidth = width - 8;
		int textY = y + (HEADER_HEIGHT - painter.lineHeight()) / 2;

		int color;
		if (module.metadata().toggleable()) {
			color = module.isEnabled() ? ClickGuiTheme.MODULE_ENABLED : ClickGuiTheme.MODULE_DISABLED;
		} else {
			color = ClickGuiTheme.TEXT_PRIMARY;
		}

		if (textWidth <= availableWidth) {
			int textX = x + (width - textWidth) / 2;
			painter.text(name, textX, textY, color);
		} else {
			painter.marqueeText(name, x + 4, textY, availableWidth, color, hoveredHeader, time);
		}

		if (expanded && !settings.isEmpty()) {
			int currentY = y + HEADER_HEIGHT;
			for (InlineSettingItem item : settings) {
				if (item.isVisible()) {
					item.render(painter, x, currentY, width, mouseX, mouseY, time, colorIndex);
					currentY += item.height();
				}
			}
		}
	}

	public boolean mouseClicked(
		double mouseX,
		double mouseY,
		int button,
		int x,
		int y,
		int width
	) {
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + HEADER_HEIGHT) {
			if (button == 0) {
				if (module.metadata().toggleable()) {
					moduleManager.setEnabled(module.id(), !module.isEnabled());
				} else if (module.hasSettings()) {
					expanded = !expanded;
				}
				return true;
			} else if (button == 1) {
				if (module.hasSettings()) {
					expanded = !expanded;
				}
				return true;
			}
		}

		if (expanded && !settings.isEmpty()) {
			int currentY = y + HEADER_HEIGHT;
			for (InlineSettingItem item : settings) {
				if (item.isVisible()) {
					if (item.mouseClicked(mouseX, mouseY, button, x, currentY, width)) {
						return true;
					}
					currentY += item.height();
				}
			}
		}
		return false;
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
		if (expanded) {
			for (InlineSettingItem item : settings) {
				if (item.isVisible()) {
					item.mouseReleased(mouseX, mouseY, button);
				}
			}
		}
	}

	public void mouseDragged(
		double mouseX,
		double mouseY,
		double deltaX,
		double deltaY,
		int button,
		int x,
		int y,
		int width
	) {
		if (expanded) {
			int currentY = y + HEADER_HEIGHT;
			for (InlineSettingItem item : settings) {
				if (item.isVisible()) {
					item.mouseDragged(mouseX, mouseY, deltaX, deltaY, button, x, currentY, width);
					currentY += item.height();
				}
			}
		}
	}

	public boolean keyPressed(int keyCode, int scancode, int modifiers) {
		if (expanded) {
			for (InlineSettingItem item : settings) {
				if (item.isVisible() && item.keyPressed(keyCode, scancode, modifiers)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean charTyped(char chr) {
		if (expanded) {
			for (InlineSettingItem item : settings) {
				if (item.isVisible() && item.charTyped(chr)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isAnyKeybindListening() {
		if (expanded) {
			for (InlineSettingItem item : settings) {
				if (item instanceof KeybindSettingItem keybind && keybind.isListening()) {
					return true;
				}
			}
		}
		return false;
	}
}
