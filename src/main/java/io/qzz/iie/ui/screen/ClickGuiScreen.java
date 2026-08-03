package io.qzz.iie.ui.screen;

import io.qzz.iie.api.hud.HudPositionEditorApi;
import io.qzz.iie.api.setting.SettingEditorApi;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleCategory;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindValue;
import io.qzz.iie.setting.Setting;
import io.qzz.iie.ui.animation.ArgbColor;
import io.qzz.iie.ui.animation.AnimatedScroll;
import io.qzz.iie.ui.binding.BindingUpdateResult;
import io.qzz.iie.ui.binding.ValueBinding;
import io.qzz.iie.ui.component.control.ClickControl;
import io.qzz.iie.ui.component.control.ChoiceControl;
import io.qzz.iie.ui.component.control.KeybindControl;
import io.qzz.iie.ui.component.control.HudPositionControl;
import io.qzz.iie.ui.component.control.SliderControl;
import io.qzz.iie.ui.component.control.TextFieldControl;
import io.qzz.iie.ui.component.control.ToggleControl;
import io.qzz.iie.ui.component.control.UnsupportedControl;
import io.qzz.iie.ui.component.control.EditorSettingControl;
import io.qzz.iie.ui.factory.SettingControlFactory;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputRouter;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.ChoicePainter;
import io.qzz.iie.ui.render.KeybindPainter;
import io.qzz.iie.ui.render.TogglePainter;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.render.SliderPainter;
import io.qzz.iie.ui.theme.ClickGuiMotion;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.DoubleSupplier;

public final class ClickGuiScreen extends Screen {
	private static final int KEY_ESCAPE = 256;

	private final ModuleManager moduleManager;
	private final String version;
	private final Screen parent;
	private final DoubleSupplier textScale;
	private final UiInputRouter inputRouter = new UiInputRouter();
	private final SettingControlFactory settingControlFactory;
	private final ClickGuiAnimations animations = new ClickGuiAnimations();
	private final List<UiInputTarget> inputTargets = new ArrayList<>();
	private final List<ModuleRowView> moduleRows = new ArrayList<>();
	private final List<SettingRowView> settingRows = new ArrayList<>();

	private ModuleEnabledRowView moduleEnabledRow;
	private ModuleCategory selectedCategory;
	private Module settingsModule;
	private BuiltInPage builtInPage;
	private String searchQuery = "";
	private final AnimatedScroll scrollOffset =
		new AnimatedScroll(ClickGuiMotion.SCROLL);
	private boolean sidebarCollapsed;
	private boolean controlsDirty = true;
	private ClickGuiLayout layout;
	private TextFieldControl searchField;

	public ClickGuiScreen(ModuleManager moduleManager, String version, Screen parent) {
		this(
			moduleManager,
			version,
			parent,
			() -> 1.0,
			HudPositionEditorApi.noop()
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
		HudPositionEditorApi editor = Objects.requireNonNull(
			hudPositions,
			"hudPositions"
		);
		this.settingControlFactory = new SettingControlFactory(
			setting -> editor.open(setting, this),
			settingEditors::supports,
			setting -> Objects.requireNonNull(
				settingEditors,
				"settingEditors"
			).open(setting, this)
		);
	}

	@Override
	protected void init() {
		layout = ClickGuiLayout.calculate(width, height, sidebarCollapsed);
		animations.resetLayout();
		ensureSelectedCategory();
		ensureSearchField();
		rebuildControls();
	}

	@Override
	public void tick() {
		if (controlsDirty) {
			rebuildControls();
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
		animations.beginFrame();
		advanceScrollAnimation();
		if (controlsDirty) {
			rebuildControls();
		}

		UiPainter painter = new UiPainter(graphics, font, textScale.getAsDouble());
		painter.fill(
			0,
			0,
			width,
			height,
			ArgbColor.interpolate(
				0x00000000,
				ClickGuiTheme.OVERLAY,
				animations.guiOpenProgress()
			)
		);
		painter.withTranslation(0, animations.guiOpenOffset(), () ->
			renderWindow(painter, mouseX, mouseY)
		);
	}

	private void renderWindow(UiPainter painter, int mouseX, int mouseY) {
		painter.roundedRect(
			layout.windowX(),
			layout.windowY(),
			layout.windowWidth(),
			layout.windowHeight(),
			layout.radius(),
			ClickGuiTheme.WINDOW_BORDER
		);

		if (!sidebarCollapsed) {
			renderSidebar(painter, mouseX, mouseY);
		}
		renderContent(painter, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		UiInputEvent.PointerPressed input = new UiInputEvent.PointerPressed(
			event.x(),
			event.y(),
			MouseButton.fromCode(event.button()),
			event.modifiers()
		);
		return inputRouter.route(input, List.copyOf(inputTargets))
			|| super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		UiInputEvent.PointerReleased input = new UiInputEvent.PointerReleased(
			event.x(),
			event.y(),
			MouseButton.fromCode(event.button()),
			event.modifiers()
		);
		return inputRouter.route(input, List.copyOf(inputTargets))
			|| super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		UiInputEvent.PointerDragged input = new UiInputEvent.PointerDragged(
			event.x(),
			event.y(),
			deltaX,
			deltaY,
			MouseButton.fromCode(event.button()),
			event.modifiers()
		);
		return inputRouter.route(input, List.copyOf(inputTargets))
			|| super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(
		double mouseX,
		double mouseY,
		double horizontal,
		double vertical
	) {
		UiInputEvent.Scroll input = new UiInputEvent.Scroll(
			mouseX,
			mouseY,
			horizontal,
			vertical
		);
		return inputRouter.route(input, List.copyOf(inputTargets))
			|| super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		UiInputEvent.KeyPressed input = new UiInputEvent.KeyPressed(
			event.key(),
			event.scancode(),
			event.modifiers()
		);
		if (inputRouter.route(input, List.copyOf(inputTargets))) {
			return true;
		}
		if (event.key() == KEY_ESCAPE) {
			if (settingsModule != null || builtInPage != null) {
				settingsModule = null;
				builtInPage = null;
				scrollOffset.reset();
				markControlsDirty(true);
				return true;
			}
			if (!searchQuery.isEmpty()) {
				setSearchQuery("");
				return true;
			}
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		UiInputEvent.KeyReleased input = new UiInputEvent.KeyReleased(
			event.key(),
			event.scancode(),
			event.modifiers()
		);
		return inputRouter.route(input, List.copyOf(inputTargets))
			|| super.keyReleased(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return inputRouter.route(
			new UiInputEvent.CharacterTyped(event.codepoint()),
			List.copyOf(inputTargets)
		) || super.charTyped(event);
	}

	@Override
	public void onClose() {
		inputRouter.clear();
		minecraft.setScreenAndShow(parent);
	}

	@Override
	public void removed() {
		inputRouter.clear();
		super.removed();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void ensureSelectedCategory() {
		if (selectedCategory != null && categories().contains(selectedCategory)) {
			return;
		}
		selectedCategory = categories().stream().findFirst().orElse(null);
	}

	private void ensureSearchField() {
		if (searchField != null) {
			return;
		}
		searchField = new TextFieldControl(new ValueBinding<>() {
			@Override
			public String get() {
				return searchQuery;
			}

			@Override
			public BindingUpdateResult set(String value) {
				setSearchQuery(value);
				return new BindingUpdateResult.Accepted();
			}
		}, 64);
	}

	private void rebuildControls() {
		controlsDirty = false;
		inputTargets.clear();
		moduleRows.clear();
		settingRows.clear();
		moduleEnabledRow = null;
		layout = ClickGuiLayout.calculate(width, height, sidebarCollapsed);

		addMenuControl();
		if (!sidebarCollapsed) {
			addSidebarControls();
		}

		if (settingsModule == null && builtInPage == null) {
			addSearchControl();
			addModuleControls();
		} else if (settingsModule != null) {
			addBackControl();
			addSettingsControls();
		} else {
			addBackControl();
		}
		addScrollControl();
		clampScrollOffset();
	}

	private void addMenuControl() {
		ClickControl menu = new ClickControl().on(MouseButton.LEFT, () -> {
			sidebarCollapsed = !sidebarCollapsed;
			markControlsDirty(true);
		});
		menu.layout(layout.menuButton());
		inputTargets.add(menu);
	}

	private void addSidebarControls() {
		int categoryY = layout.sidebarContentTop();
		for (ModuleCategory category : categories()) {
			Rect bounds = new Rect(
				layout.sidebarX() + layout.padding(),
				categoryY,
				layout.sidebarWidth() - layout.padding() * 2,
				layout.navigationRowHeight()
			);
			ClickControl control = new ClickControl().on(MouseButton.LEFT, () -> {
				selectedCategory = category;
				settingsModule = null;
				builtInPage = null;
				searchQuery = "";
				scrollOffset.reset();
				markControlsDirty(true);
			});
			control.layout(bounds);
			inputTargets.add(control);
			categoryY += layout.navigationRowHeight() + layout.smallGap();
		}

		addFooterControl(
			layout.configButton(),
			BuiltInPage.CONFIG_MANAGER
		);
	}

	private void addFooterControl(Rect bounds, BuiltInPage page) {
		ClickControl control = new ClickControl().on(MouseButton.LEFT, () -> {
			settingsModule = null;
			builtInPage = page;
			scrollOffset.reset();
			markControlsDirty(true);
		});
		control.layout(bounds);
		inputTargets.add(control);
	}

	private void addSearchControl() {
		searchField.layout(layout.searchField());
		inputTargets.add(searchField);
	}

	private void addBackControl() {
		ClickControl back = new ClickControl().on(MouseButton.LEFT, this::returnToCategory);
		back.layout(layout.backButton());
		inputTargets.add(back);
	}

	private void addModuleControls() {
		List<Module> modules = visibleModules();
		double rowY = layout.listArea().y() - scrollOffset.value();
		for (Module module : modules) {
			Rect rowBounds = new Rect(
				layout.listArea().x(),
				rowY,
				layout.listArea().width() - layout.scrollbarWidth() - layout.smallGap(),
				layout.moduleRowHeight()
			);
			if (intersects(rowBounds, layout.listArea())) {
				ClickControl row = new ClickControl()
					.on(MouseButton.RIGHT, () -> openModuleSettings(module));
				row.layout(rowBounds);
				inputTargets.add(row);

				boolean toggleable = module.metadata().toggleable();
				Rect gearBounds = new Rect(
					rowBounds.x() + rowBounds.width() - (toggleable ? 80 : 30),
					rowBounds.y() + (rowBounds.height() - 24) / 2,
					24,
					24
				);
				ClickControl gear = new ClickControl()
					.on(MouseButton.LEFT, () -> openModuleSettings(module));
				gear.layout(gearBounds);
				gear.setEnabled(module.hasSettings());
				inputTargets.add(gear);

				ToggleControl toggle = null;
				Rect toggleBounds = null;
				if (toggleable) {
					toggle = settingControlFactory.createModuleEnabled(moduleManager, module.id());
					toggleBounds = new Rect(
						rowBounds.x() + rowBounds.width() - 46,
						rowBounds.y() + (rowBounds.height() - 22) / 2,
						42,
						22
					);
					toggle.layout(toggleBounds);
					inputTargets.add(toggle);
				}
				moduleRows.add(new ModuleRowView(module, rowBounds, gearBounds, toggleBounds, toggle));
			}
			rowY += layout.moduleRowHeight() + layout.smallGap();
		}
	}

	private void addSettingsControls() {
		double rowY = layout.listArea().y() - scrollOffset.value();
		rowY = addModuleEnabledControl(rowY);
		for (Setting<?> setting : settingsModule.settings()) {
			Rect rowBounds = new Rect(
				layout.listArea().x(),
				rowY,
				layout.listArea().width() - layout.scrollbarWidth() - layout.smallGap(),
				layout.settingRowHeight()
			);
			if (intersects(rowBounds, layout.listArea())) {
				UiInputTarget control = settingControlFactory.create(setting);
				Rect controlBounds;
				if (control instanceof ToggleControl toggle) {
					controlBounds = new Rect(
						rowBounds.x() + rowBounds.width() - 46,
						rowBounds.y() + (rowBounds.height() - 22) / 2,
						42,
						22
					);
					toggle.layout(controlBounds);
				} else if (control instanceof SliderControl slider) {
					controlBounds = new Rect(
						rowBounds.x() + rowBounds.width() * 0.48,
						rowBounds.y() + rowBounds.height() - 20,
						rowBounds.width() * 0.48 - 8,
						12
					);
					slider.layout(controlBounds);
				} else if (control instanceof UnsupportedControl unsupported) {
					controlBounds = new Rect(
						rowBounds.x() + rowBounds.width() * 0.58,
						rowBounds.y() + 13,
						rowBounds.width() * 0.38,
						18
					);
					unsupported.layout(controlBounds);
				} else if (control instanceof ChoiceControl choice) {
					controlBounds = new Rect(
						rowBounds.x() + rowBounds.width() * 0.58,
						rowBounds.y() + 11,
						rowBounds.width() * 0.38,
						26
					);
					choice.layout(controlBounds, 24);
				} else if (control instanceof KeybindControl keybind) {
					controlBounds = new Rect(
						rowBounds.x() + rowBounds.width() * 0.58,
						rowBounds.y() + 11,
						rowBounds.width() * 0.38,
						26
					);
					keybind.layout(controlBounds);
				} else if (control instanceof HudPositionControl position) {
					controlBounds = new Rect(
						rowBounds.x() + rowBounds.width() * 0.58,
						rowBounds.y() + 11,
						rowBounds.width() * 0.38,
						26
					);
					position.layout(controlBounds);
				} else if (control instanceof EditorSettingControl editorControl) {
					controlBounds = new Rect(
						rowBounds.x() + rowBounds.width() * 0.58,
						rowBounds.y() + 11,
						rowBounds.width() * 0.38,
						26
					);
					editorControl.layout(controlBounds);
				} else {
					throw new IllegalStateException("Unknown control: " + control.getClass().getName());
				}
				inputTargets.add(control);
				settingRows.add(new SettingRowView(setting, rowBounds, controlBounds, control));
			}
			rowY += layout.settingRowHeight() + layout.smallGap();
		}
	}

	private double addModuleEnabledControl(double rowY) {
		if (!settingsModule.metadata().toggleable()) {
			return rowY;
		}
		Rect rowBounds = new Rect(
			layout.listArea().x(),
			rowY,
			layout.listArea().width() - layout.scrollbarWidth() - layout.smallGap(),
			layout.settingRowHeight()
		);
		if (intersects(rowBounds, layout.listArea())) {
			ToggleControl toggle =
				settingControlFactory.createModuleEnabled(moduleManager, settingsModule.id());
			Rect toggleBounds = new Rect(
				rowBounds.x() + rowBounds.width() - 46,
				rowBounds.y() + (rowBounds.height() - 22) / 2,
				42,
				22
			);
			toggle.layout(toggleBounds);
			inputTargets.add(toggle);
			moduleEnabledRow = new ModuleEnabledRowView(rowBounds, toggleBounds, toggle);
		}
		return rowY + layout.settingRowHeight() + layout.smallGap();
	}

	private void addScrollControl() {
		UiInputTarget scrollTarget = new UiInputTarget() {
			@Override
			public Rect inputBounds() {
				return layout.listArea();
			}

			@Override
			public InputResult handleInput(UiInputEvent event) {
				if (event instanceof UiInputEvent.Scroll scroll) {
					inputRouter.clear();
					scrollOffset.scrollBy(
						-scroll.vertical() * layout.moduleRowHeight() * 0.55
					);
					return InputResult.CONSUMED;
				}
				return InputResult.IGNORED;
			}
		};
		inputTargets.add(0, scrollTarget);
	}

	private void openModuleSettings(Module module) {
		if (!module.hasSettings()) {
			return;
		}
		settingsModule = module;
		builtInPage = null;
		scrollOffset.reset();
		animations.beginSettingsPage();
		markControlsDirty(true);
	}

	private void returnToCategory() {
		settingsModule = null;
		builtInPage = null;
		scrollOffset.reset();
		animations.finishSettingsPage();
		markControlsDirty(true);
	}

	private void setSearchQuery(String value) {
		searchQuery = Objects.requireNonNull(value, "value");
		scrollOffset.reset();
		controlsDirty = true;
	}

	private void markControlsDirty(boolean clearInputState) {
		controlsDirty = true;
		if (clearInputState) {
			inputRouter.clear();
		}
	}

	private List<ModuleCategory> categories() {
		List<ModuleCategory> categories = new ArrayList<>(moduleManager.categories());
		categories.sort(null);
		return List.copyOf(categories);
	}

	private List<Module> visibleModules() {
		String query = searchQuery.strip().toLowerCase(Locale.ROOT);
		return moduleManager.modules().stream()
			.filter(module -> module.category().equals(selectedCategory))
			.filter(module -> query.isEmpty() || matches(module, query))
			.sorted(
				Comparator.comparingInt((Module module) -> module.metadata().order())
					.thenComparing(Module::id)
			)
			.toList();
	}

	private boolean matches(Module module, String query) {
		return module.id().toString().contains(query)
			|| translated(module.metadata().nameTranslationKey()).toLowerCase(Locale.ROOT).contains(query)
			|| translated(module.metadata().descriptionTranslationKey()).toLowerCase(Locale.ROOT).contains(query);
	}

	private void clampScrollOffset() {
		double maximum = Math.max(0.0, contentHeight() - layout.listArea().height());
		scrollOffset.setMaximum(maximum);
	}

	private void advanceScrollAnimation() {
		double previous = scrollOffset.value();
		double current = scrollOffset.advance(animations.frameDeltaSeconds());
		if (Double.compare(previous, current) != 0) {
			controlsDirty = true;
		}
	}

	private double contentHeight() {
		int count;
		int rowHeight;
		if (settingsModule != null) {
			count = settingsModule.settings().size()
				+ (settingsModule.metadata().toggleable() ? 1 : 0);
			rowHeight = layout.settingRowHeight();
		} else if (builtInPage != null) {
			return 0.0;
		} else {
			count = visibleModules().size();
			rowHeight = layout.moduleRowHeight();
		}
		return Math.max(0, count * (rowHeight + layout.smallGap()) - layout.smallGap());
	}

	private void renderSidebar(UiPainter painter, int mouseX, int mouseY) {
		painter.roundedRect(
			layout.sidebarX(),
			layout.sidebarY(),
			layout.sidebarWidth(),
			layout.sidebarHeight(),
			layout.innerRadius(),
			ClickGuiTheme.SIDEBAR
		);
		drawMenuIcon(painter, layout.menuButton());
		painter.text("Edge Client", layout.sidebarX() + 54, layout.sidebarY() + 17, ClickGuiTheme.TEXT_PRIMARY);
		painter.text("v" + version, layout.sidebarX() + 54, layout.sidebarY() + 31, ClickGuiTheme.TEXT_SECONDARY);

		List<ModuleCategory> categories = categories();
		renderSidebarSelection(painter, categories);
		int categoryY = layout.sidebarContentTop();
		for (ModuleCategory category : categories) {
			Rect bounds = new Rect(
				layout.sidebarX() + layout.padding(),
				categoryY,
				layout.sidebarWidth() - layout.padding() * 2,
				layout.navigationRowHeight()
			);
			if (!category.equals(selectedCategory) && bounds.contains(mouseX, mouseY)) {
				painter.roundedRect(
					bounds.left(),
					bounds.top(),
					(int) bounds.width(),
					(int) bounds.height(),
					8,
					ClickGuiTheme.ROW_HOVER
				);
			}

			String name = translated(category.translationKey());
			painter.text(name, bounds.left() + 12, bounds.top() + 15, ClickGuiTheme.TEXT_PRIMARY);
			String count = Integer.toString(moduleCount(category));
			painter.text(
				count,
				bounds.right() - painter.textWidth(count) - 12,
				bounds.top() + 15,
				ClickGuiTheme.TEXT_SECONDARY
			);
			categoryY += layout.navigationRowHeight() + layout.smallGap();
		}

		renderFooterButton(
			painter,
			layout.configButton(),
			translated("client.gui.config_manager"),
			builtInPage == BuiltInPage.CONFIG_MANAGER,
			mouseX,
			mouseY
		);
	}

	private void renderSidebarSelection(
		UiPainter painter,
		List<ModuleCategory> categories
	) {
		Rect current = animations.advanceSidebarSelection(
			layout,
			categories,
			selectedCategory,
			builtInPage == BuiltInPage.CONFIG_MANAGER
		);
		painter.roundedRect(
			current.left(),
			current.top(),
			(int) Math.round(current.width()),
			(int) Math.round(current.height()),
			8,
			ClickGuiTheme.SELECTED
		);
	}

	private void renderFooterButton(
		UiPainter painter,
		Rect bounds,
		String label,
		boolean selected,
		int mouseX,
		int mouseY
	) {
		if (!selected && bounds.contains(mouseX, mouseY)) {
			painter.roundedRect(
				bounds.left(),
				bounds.top(),
				(int) bounds.width(),
				(int) bounds.height(),
				8,
				ClickGuiTheme.CONTROL_OFF
			);
		}
		painter.roundedRect(
			bounds.left() + 10,
			bounds.top() + 13,
			18,
			12,
			4,
			ClickGuiTheme.ICON_MUTED
		);
		painter.roundedRect(
			bounds.left() + 12,
			bounds.top() + 11,
			8,
			5,
			2,
			ClickGuiTheme.ICON_MUTED
		);
		painter.text(label, bounds.left() + 38, bounds.top() + 14, ClickGuiTheme.TEXT_PRIMARY);
	}

	private void renderContent(UiPainter painter, int mouseX, int mouseY) {
		painter.roundedRect(
			layout.contentX(),
			layout.contentY(),
			layout.contentWidth(),
			layout.contentHeight(),
			layout.innerRadius(),
			ClickGuiTheme.CONTENT
		);
		if (sidebarCollapsed) {
			drawMenuIcon(painter, layout.menuButton());
		}

		if (settingsModule != null) {
			painter.withTranslation(animations.settingsPageOffset(), 0, () -> renderContentForeground(
				painter,
				mouseX,
				mouseY
			));
			return;
		}
		renderContentForeground(painter, mouseX, mouseY);
	}

	private void renderContentForeground(UiPainter painter, int mouseX, int mouseY) {
		String titleText = currentTitle();
		int titleX;
		if (settingsModule != null || builtInPage != null) {
			titleX = layout.backButton().right() + 8;
		} else {
			titleX = sidebarCollapsed ? layout.contentX() + 54 : layout.contentX() + layout.padding();
		}
		painter.text(titleText, titleX, layout.contentY() + 18, ClickGuiTheme.TEXT_PRIMARY);

		if (settingsModule == null && builtInPage == null) {
			renderSearch(painter, mouseX, mouseY);
			renderModuleList(painter, mouseX, mouseY);
		} else if (settingsModule != null) {
			renderSettingsPage(painter, mouseX, mouseY);
		} else {
			renderBuiltInPage(painter);
		}
	}

	private void renderSearch(UiPainter painter, int mouseX, int mouseY) {
		Rect bounds = layout.searchField();
		int borderColor = inputRouter.isFocused(searchField)
			? ClickGuiTheme.ACCENT
			: ClickGuiTheme.OUTLINE;
		painter.roundedRectWithBorder(
			bounds.left(),
			bounds.top(),
			(int) bounds.width(),
			(int) bounds.height(),
			8,
			1,
			ClickGuiTheme.CONTROL_DARK,
			borderColor
		);
		String value = searchField.value();
		painter.text(
			value.isEmpty() ? translated("client.gui.search") : value,
			bounds.left() + 10,
			bounds.top() + 8,
			value.isEmpty() ? ClickGuiTheme.TEXT_SECONDARY : ClickGuiTheme.TEXT_PRIMARY
		);
	}

	private void renderModuleList(UiPainter painter, int mouseX, int mouseY) {
		Rect clip = layout.listArea();
		painter.enableScissor(clip.left(), clip.top(), clip.right(), clip.bottom());
		try {
			for (ModuleRowView row : moduleRows) {
				if (row.bounds().contains(mouseX, mouseY)) {
					painter.roundedRect(
						row.bounds().left(),
						row.bounds().top(),
						(int) row.bounds().width(),
						(int) row.bounds().height(),
						10,
						ClickGuiTheme.ROW_HOVER
					);
				}

				int textWidth = (int) row.bounds().width()
					- (row.module().metadata().toggleable() ? 100 : 55);
				painter.text(
					translated(row.module().metadata().nameTranslationKey()),
					row.bounds().left() + 14,
					row.bounds().top() + 12,
					ClickGuiTheme.TEXT_PRIMARY
				);
				painter.text(
					painter.trimToWidth(
						translated(row.module().metadata().descriptionTranslationKey()),
						textWidth
					),
					row.bounds().left() + 14,
					row.bounds().top() + 27,
					ClickGuiTheme.TEXT_SECONDARY
				);
				boolean hovered = row.bounds().contains(mouseX, mouseY);
				drawGear(
					painter,
					row.gearBounds(),
					row.module().hasSettings(),
					hovered ? ClickGuiTheme.ROW_HOVER : ClickGuiTheme.CONTENT
				);
				if (row.toggle() != null) {
					TogglePainter.draw(
						painter,
						row.toggleBounds(),
						row.toggle().advanceAnimation(animations.frameDeltaSeconds())
					);
				}
			}
		} finally {
			painter.disableScissor();
		}

		if (visibleModules().isEmpty()) {
			painter.text(
				translated("client.gui.no_modules"),
				clip.left() + 14,
				clip.top() + 16,
				ClickGuiTheme.TEXT_SECONDARY
			);
		}
		renderScrollbar(painter);
	}

	private void renderSettingsPage(UiPainter painter, int mouseX, int mouseY) {
		Rect back = layout.backButton();
		painter.text("<", back.left(), back.top(), ClickGuiTheme.ACCENT);

		Rect clip = layout.listArea();
		painter.enableScissor(clip.left(), clip.top(), clip.right(), clip.bottom());
		try {
			renderModuleEnabledRow(painter, mouseX, mouseY);
			boolean pointerCoveredByChoiceDrawer = settingRows.stream().anyMatch(
				row -> row.control() instanceof ChoiceControl choice
					&& choice.coversDrawer(mouseX, mouseY)
			);
			for (SettingRowView row : settingRows) {
				if (!pointerCoveredByChoiceDrawer && row.bounds().contains(mouseX, mouseY)) {
					painter.roundedRect(
						row.bounds().left(),
						row.bounds().top(),
						(int) row.bounds().width(),
						(int) row.bounds().height(),
						10,
						ClickGuiTheme.ROW_HOVER
					);
				}
				painter.text(
					translated(row.setting().translationKey()),
					row.bounds().left() + 14,
					row.bounds().top() + 13,
					ClickGuiTheme.TEXT_PRIMARY
				);
				if (row.control() instanceof ToggleControl toggle) {
					TogglePainter.draw(
						painter,
						row.controlBounds(),
						toggle.advanceAnimation(animations.frameDeltaSeconds())
					);
				} else if (row.control() instanceof SliderControl slider) {
					renderSlider(painter, row.controlBounds(), slider);
				} else if (row.control() instanceof ChoiceControl) {
					// Choice headers and drawers render after ordinary controls.
				} else if (row.control() instanceof KeybindControl keybind) {
					KeybindPainter.draw(painter, keybind, keybindDisplayText(keybind));
				} else if (row.control() instanceof HudPositionControl
					|| row.control() instanceof EditorSettingControl) {
					painter.roundedRectWithBorder(
						row.controlBounds().left(),
						row.controlBounds().top(),
						(int) row.controlBounds().width(),
						(int) row.controlBounds().height(),
						8,
						1,
						0xCC151922,
						0x665D6677
					);
					String edit = translated(
						row.control() instanceof HudPositionControl
							? "client.gui.hud_position.edit"
							: row.control() instanceof EditorSettingControl editorControl
								&& editorControl.isEnabled()
								? "client.gui.setting_editor.edit"
								: "client.gui.unsupported_setting"
					);
					painter.text(
						edit,
						row.controlBounds().left()
							+ ((int) row.controlBounds().width() - painter.textWidth(edit)) / 2,
						row.controlBounds().top()
							+ ((int) row.controlBounds().height() - painter.lineHeight()) / 2,
						ClickGuiTheme.TEXT_PRIMARY
					);
				} else if (row.control() instanceof UnsupportedControl) {
					String unsupported = translated("client.gui.unsupported_setting");
					painter.text(
						unsupported,
						row.controlBounds().right() - painter.textWidth(unsupported),
						row.controlBounds().top(),
						ClickGuiTheme.TEXT_SECONDARY
					);
				}
			}
			for (SettingRowView row : settingRows) {
				if (row.control() instanceof ChoiceControl choice) {
					choice.advanceAnimation(animations.frameDeltaSeconds());
					ChoicePainter.drawHeader(painter, choice, ClickGuiScreen::translated);
				}
			}
			for (SettingRowView row : settingRows) {
				if (row.control() instanceof ChoiceControl choice) {
					ChoicePainter.drawDrawer(
						painter,
						choice,
						choice.drawerProgress(),
						ClickGuiScreen::translated
					);
				}
			}
		} finally {
			painter.disableScissor();
		}
		renderScrollbar(painter);
	}

	private void renderModuleEnabledRow(UiPainter painter, int mouseX, int mouseY) {
		if (moduleEnabledRow == null) {
			return;
		}
		if (moduleEnabledRow.bounds().contains(mouseX, mouseY)) {
			painter.roundedRect(
				moduleEnabledRow.bounds().left(),
				moduleEnabledRow.bounds().top(),
				(int) moduleEnabledRow.bounds().width(),
				(int) moduleEnabledRow.bounds().height(),
				10,
				ClickGuiTheme.ROW_HOVER
			);
		}
		painter.text(
			translated("client.gui.module_enabled"),
			moduleEnabledRow.bounds().left() + 14,
			moduleEnabledRow.bounds().top() + 13,
			ClickGuiTheme.TEXT_PRIMARY
		);
		TogglePainter.draw(
			painter,
			moduleEnabledRow.toggleBounds(),
			moduleEnabledRow.toggle().advanceAnimation(animations.frameDeltaSeconds())
		);
	}

	private void renderBuiltInPage(UiPainter painter) {
		Rect back = layout.backButton();
		painter.text("<", back.left(), back.top(), ClickGuiTheme.ACCENT);
		painter.text(
			translated("client.gui.not_implemented"),
			layout.listArea().left() + 14,
			layout.listArea().top() + 16,
			ClickGuiTheme.TEXT_SECONDARY
		);
	}

	private void renderSlider(UiPainter painter, Rect bounds, SliderControl slider) {
		SliderPainter.draw(
			painter,
			bounds,
			slider,
			formatValue(slider.value(), slider.step())
		);
	}

	private void drawGear(UiPainter painter, Rect bounds, boolean enabled, int backgroundColor) {
		int color = enabled ? ClickGuiTheme.ICON_MUTED : ClickGuiTheme.CONTROL_OFF;
		int centerX = bounds.left() + (int) bounds.width() / 2;
		int centerY = bounds.top() + (int) bounds.height() / 2;
		painter.roundedRect(centerX - 8, centerY - 8, 16, 16, 8, color);
		painter.roundedRect(centerX - 3, centerY - 3, 6, 6, 3, backgroundColor);
	}

	private void drawMenuIcon(UiPainter painter, Rect bounds) {
		int x = bounds.left() + 2;
		int y = bounds.top() + 3;
		for (int line = 0; line < 3; line++) {
			painter.roundedRect(x, y + line * 6, 20, 2, 1, ClickGuiTheme.TEXT_PRIMARY);
		}
	}

	private void renderScrollbar(UiPainter painter) {
		double contentHeight = contentHeight();
		double viewportHeight = layout.listArea().height();
		if (contentHeight <= viewportHeight || contentHeight <= 0.0) {
			return;
		}

		int trackX = layout.listArea().right() - layout.scrollbarWidth();
		int thumbHeight = Math.max(
			22,
			(int) Math.round(viewportHeight * viewportHeight / contentHeight)
		);
		double maximumScroll = contentHeight - viewportHeight;
		int travel = (int) viewportHeight - thumbHeight;
		int thumbY = layout.listArea().top()
			+ (int) Math.round(travel * scrollOffset.value() / maximumScroll);
		painter.roundedRect(
			trackX,
			thumbY,
			layout.scrollbarWidth(),
			thumbHeight,
			layout.scrollbarWidth() / 2,
			ClickGuiTheme.SCROLLBAR
		);
	}

	private String currentTitle() {
		if (settingsModule != null) {
			return translated(settingsModule.metadata().nameTranslationKey());
		}
		if (builtInPage == BuiltInPage.CONFIG_MANAGER) {
			return translated("client.gui.config_manager");
		}
		return translated(selectedCategory.translationKey());
	}

	private int moduleCount(ModuleCategory category) {
		return (int) moduleManager.modules().stream()
			.filter(module -> module.category().equals(category))
			.count();
	}

	private static String translated(String key) {
		return Component.translatable(key).getString();
	}

	private static String keybindDisplayText(KeybindControl control) {
		if (control.isListening()) {
			return translated("client.gui.keybind.listening");
		}
		KeybindValue keybind = control.setting().value();
		if (!keybind.isBound()) {
			return translated("client.gui.keybind.unbound");
		}
		return InputConstants.Type.KEYSYM
			.getOrCreate(keybind.keyCode())
			.getDisplayName()
			.getString();
	}

	private static String formatValue(double value, double step) {
		if (step >= 1.0) {
			return Long.toString(Math.round(value));
		}
		if (step >= 0.1) {
			return String.format(Locale.ROOT, "%.1f", value);
		}
		return String.format(Locale.ROOT, "%.2f", value);
	}

	private static boolean intersects(Rect first, Rect second) {
		return first.right() > second.left()
			&& first.left() < second.right()
			&& first.bottom() > second.top()
			&& first.top() < second.bottom();
	}

	private enum BuiltInPage {
		CONFIG_MANAGER
	}

	private record ModuleRowView(
		Module module,
		Rect bounds,
		Rect gearBounds,
		Rect toggleBounds,
		ToggleControl toggle
	) {
	}

	private record SettingRowView(
		Setting<?> setting,
		Rect bounds,
		Rect controlBounds,
		UiInputTarget control
	) {
	}

	private record ModuleEnabledRowView(
		Rect bounds,
		Rect toggleBounds,
		ToggleControl toggle
	) {
	}

}
