package io.qzz.iie.ui.screen;

import io.qzz.iie.api.hud.HudPositionEditorApi;
import io.qzz.iie.api.setting.SettingEditorApi;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleCategory;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.ui.panel.CategoryPanel;
import io.qzz.iie.ui.panel.CategoryPositionManager;
import io.qzz.iie.ui.panel.InlineSettingFactory;
import io.qzz.iie.ui.panel.ModulePanelItem;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleSupplier;

public final class ClickGuiScreen extends Screen {
	private static final int KEY_ESCAPE = 256;

	private final ModuleManager moduleManager;
	private final String version;
	private final Screen parent;
	private final DoubleSupplier textScale;
	private final HudPositionEditorApi hudPositions;
	private final SettingEditorApi settingEditors;

	private final List<CategoryPanel> categoryPanels = new ArrayList<>();
	private boolean initializedPanels;

	public ClickGuiScreen(ModuleManager moduleManager, String version, Screen parent) {
		this(
			moduleManager,
			version,
			parent,
			() -> 1.0,
			HudPositionEditorApi.noop(),
			SettingEditorApi.noop()
		);
	}

	public ClickGuiScreen(
		ModuleManager moduleManager,
		String version,
		Screen parent,
		DoubleSupplier textScale
	) {
		this(
			moduleManager,
			version,
			parent,
			textScale,
			HudPositionEditorApi.noop(),
			SettingEditorApi.noop()
		);
	}

	public ClickGuiScreen(
		ModuleManager moduleManager,
		String version,
		Screen parent,
		DoubleSupplier textScale,
		HudPositionEditorApi hudPositions
	) {
		this(
			moduleManager,
			version,
			parent,
			textScale,
			hudPositions,
			SettingEditorApi.noop()
		);
	}

	public ClickGuiScreen(
		ModuleManager moduleManager,
		String version,
		Screen parent,
		DoubleSupplier textScale,
		HudPositionEditorApi hudPositions,
		SettingEditorApi settingEditors
	) {
		super(Component.translatable("client.gui.title"));
		this.moduleManager = Objects.requireNonNull(moduleManager, "moduleManager");
		this.version = Objects.requireNonNull(version, "version");
		this.parent = parent;
		this.textScale = Objects.requireNonNull(textScale, "textScale");
		this.hudPositions = Objects.requireNonNull(hudPositions, "hudPositions");
		this.settingEditors = Objects.requireNonNull(settingEditors, "settingEditors");
	}

	@Override
	protected void init() {
		if (!initializedPanels) {
			categoryPanels.clear();
			InlineSettingFactory settingFactory = new InlineSettingFactory(
				hudPositions,
				settingEditors,
				() -> this
			);

			List<ModuleCategory> categories = new ArrayList<>(moduleManager.categories());
			categories.sort(null);

			int currentX = 20;
			int currentY = 20;
			int screenW = width > 0 ? width : 800;

			for (ModuleCategory category : categories) {
				List<Module> categoryModules = moduleManager.modules().stream()
					.filter(m -> m.category().equals(category))
					.sorted(
						Comparator.comparingInt((Module m) -> m.metadata().order())
							.thenComparing(m -> m.id().toString())
					)
					.toList();

				java.util.Optional<CategoryPositionManager.PanelState> saved =
					CategoryPositionManager.getState(category.id());

				int posX;
				int posY;
				boolean open;
				java.util.Set<String> expandedSet;

				if (saved.isPresent()) {
					posX = saved.get().x();
					posY = saved.get().y();
					open = saved.get().opened();
					expandedSet = saved.get().expandedModules();
				} else {
					int estimatedWidth = CategoryPanel.DEFAULT_WIDTH;
					if (currentX + estimatedWidth > screenW - 20 && currentX > 20) {
						currentX = 20;
						currentY += 280;
					}
					posX = currentX;
					posY = currentY;
					open = true;
					expandedSet = java.util.Set.of();
					currentX += estimatedWidth + 14;
					CategoryPositionManager.updateState(category.id(), posX, posY, open, expandedSet);
				}

				CategoryPanel panel = new CategoryPanel(
					category,
					categoryModules,
					moduleManager,
					settingFactory,
					posX,
					posY
				);
				panel.setOpened(open);
				if (!expandedSet.isEmpty()) {
					for (ModulePanelItem item : panel.modules()) {
						if (expandedSet.contains(item.module().id().toString())) {
							item.setExpanded(true);
						}
					}
				}
				categoryPanels.add(panel);
			}
			CategoryPositionManager.save();
			initializedPanels = true;
		}
	}

	@Override
	public void extractRenderState(
		GuiGraphicsExtractor graphics,
		int mouseX,
		int mouseY,
		float delta
	) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		UiPainter painter = new UiPainter(
			graphics,
			font,
			textScale.getAsDouble(),
			io.qzz.iie.font.ClientFontManager.getActiveFontDescription()
		);

		// 1. Semi-transparent background overlay
		painter.fill(0, 0, width, height, ClickGuiTheme.OVERLAY);

		// 2. Bottom-left watermark
		int line1Y = height - 20;
		int line2Y = height - 10;
		painter.text("Edge Client " + version, 4, line1Y, ClickGuiTheme.WATERMARK);
		painter.text("dev, Edge Client", 4, line2Y, ClickGuiTheme.WATERMARK);

		// 3. Render all category panels
		long time = System.currentTimeMillis();
		AtomicInteger colorIndex = new AtomicInteger(0);

		for (CategoryPanel panel : categoryPanels) {
			panel.render(painter, mouseX, mouseY, delta, time, colorIndex, height);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int button = event.button();
		double mouseX = event.x();
		double mouseY = event.y();

		// Check panels in reverse order (top-most z-order panel first)
		for (int i = categoryPanels.size() - 1; i >= 0; i--) {
			CategoryPanel panel = categoryPanels.get(i);
			if (panel.mouseClicked(mouseX, mouseY, button)) {
				// Bring clicked panel to front
				categoryPanels.remove(i);
				categoryPanels.add(panel);
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		int button = event.button();
		double mouseX = event.x();
		double mouseY = event.y();

		for (CategoryPanel panel : categoryPanels) {
			panel.mouseReleased(mouseX, mouseY, button);
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		int button = event.button();
		double mouseX = event.x();
		double mouseY = event.y();

		for (CategoryPanel panel : categoryPanels) {
			panel.mouseDragged(mouseX, mouseY, deltaX, deltaY, button, width, height);
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(
		double mouseX,
		double mouseY,
		double horizontal,
		double vertical
	) {
		for (int i = categoryPanels.size() - 1; i >= 0; i--) {
			CategoryPanel panel = categoryPanels.get(i);
			if (panel.mouseScrolled(mouseX, mouseY, vertical, height)) {
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		boolean isAnyListening = false;
		for (CategoryPanel panel : categoryPanels) {
			if (panel.isAnyKeybindListening()) {
				isAnyListening = true;
				break;
			}
		}

		if (isAnyListening) {
			for (CategoryPanel panel : categoryPanels) {
				if (panel.keyPressed(event.key(), event.scancode(), event.modifiers())) {
					return true;
				}
			}
		}

		if (event.key() == KEY_ESCAPE) {
			onClose();
			return true;
		}

		for (CategoryPanel panel : categoryPanels) {
			if (panel.keyPressed(event.key(), event.scancode(), event.modifiers())) {
				return true;
			}
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		for (CategoryPanel panel : categoryPanels) {
			if (panel.charTyped((char) event.codepoint())) {
				return true;
			}
		}
		return super.charTyped(event);
	}

	@Override
	public void onClose() {
		for (CategoryPanel panel : categoryPanels) {
			panel.syncPositionState();
		}
		CategoryPositionManager.save();
		minecraft.setScreenAndShow(parent);
	}

	@Override
	public void removed() {
		for (CategoryPanel panel : categoryPanels) {
			panel.syncPositionState();
		}
		CategoryPositionManager.save();
		super.removed();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
