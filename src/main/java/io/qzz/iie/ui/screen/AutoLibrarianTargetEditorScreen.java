package io.qzz.iie.ui.screen;

import io.qzz.iie.module.impl.player.autolibrarian.EnchantmentCatalog;
import io.qzz.iie.module.impl.player.autolibrarian.EnchantmentTarget;
import io.qzz.iie.module.impl.player.autolibrarian.EnchantmentTargetsSetting;
import io.qzz.iie.module.impl.player.autolibrarian.RecommendedEnchantments;
import io.qzz.iie.module.impl.player.autolibrarian.TargetLevelSelection;
import io.qzz.iie.ui.binding.BindingUpdateResult;
import io.qzz.iie.ui.binding.RangedDoubleBinding;
import io.qzz.iie.ui.component.control.ClickControl;
import io.qzz.iie.ui.component.control.SliderControl;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputRouter;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.SliderPainter;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 使用 Edge Client 当前主题绘制的附魔目标编辑子页。
 */
public final class AutoLibrarianTargetEditorScreen extends Screen {
	private static final int KEY_ESCAPE = 256;

	private final EnchantmentTargetsSetting setting;
	private final Screen parent;
	private final UiInputRouter inputRouter = new UiInputRouter();
	private final List<UiInputTarget> inputTargets = new ArrayList<>();
	private final List<ActionView> actionViews = new ArrayList<>();
	private final List<TargetView> targetViews = new ArrayList<>();
	private final List<SliderView> sliderViews = new ArrayList<>();

	private List<EnchantmentCatalog.Entry> catalog = List.of();
	private EditBox searchBox;
	private String searchQuery = "";
	private String candidateId;
	private int candidateLevel = 1;
	private boolean candidateAnyLevel;
	private int selectedTarget;
	private int page;
	private boolean controlsDirty = true;
	private Layout layout;

	public AutoLibrarianTargetEditorScreen(
		EnchantmentTargetsSetting setting,
		Screen parent
	) {
		super(Component.translatable("client.gui.auto_librarian.targets.title"));
		this.setting = Objects.requireNonNull(setting, "setting");
		this.parent = parent;
	}

	@Override
	protected void init() {
		layout = Layout.calculate(width, height);
		catalog = EnchantmentCatalog.load(minecraft);
		ensureCandidate();
		createSearchBox();
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
		if (controlsDirty) {
			rebuildControls();
		}
		UiPainter painter = new UiPainter(graphics, font);
		painter.fill(0, 0, width, height, ClickGuiTheme.OVERLAY);
		painter.roundedRect(
			layout.window.left(),
			layout.window.top(),
			(int) layout.window.width(),
			(int) layout.window.height(),
			20,
			ClickGuiTheme.WINDOW_BORDER
		);
		painter.roundedRect(
			layout.content.left(),
			layout.content.top(),
			(int) layout.content.width(),
			(int) layout.content.height(),
			17,
			ClickGuiTheme.CONTENT
		);
		renderHeader(painter, mouseX, mouseY);
		renderSearchPane(painter, mouseX, mouseY);
		renderTargetsPane(painter, mouseX, mouseY);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		boolean handled = inputRouter.route(
			new UiInputEvent.PointerPressed(
				event.x(),
				event.y(),
				MouseButton.fromCode(event.button()),
				event.modifiers()
			),
			List.copyOf(inputTargets)
		);
		if (handled) {
			searchBox.setFocused(false);
		}
		return handled;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		return inputRouter.route(
			new UiInputEvent.PointerReleased(
				event.x(),
				event.y(),
				MouseButton.fromCode(event.button()),
				event.modifiers()
			),
			List.copyOf(inputTargets)
		) || super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(
		MouseButtonEvent event,
		double deltaX,
		double deltaY
	) {
		return inputRouter.route(
			new UiInputEvent.PointerDragged(
				event.x(),
				event.y(),
				deltaX,
				deltaY,
				MouseButton.fromCode(event.button()),
				event.modifiers()
			),
			List.copyOf(inputTargets)
		) || super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (super.keyPressed(event)) {
			return true;
		}
		if (inputRouter.route(
			new UiInputEvent.KeyPressed(
				event.key(),
				event.scancode(),
				event.modifiers()
			),
			List.copyOf(inputTargets)
		)) {
			return true;
		}
		if (event.key() == KEY_ESCAPE) {
			onClose();
			return true;
		}
		return false;
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		return super.keyReleased(event) || inputRouter.route(
			new UiInputEvent.KeyReleased(
				event.key(),
				event.scancode(),
				event.modifiers()
			),
			List.copyOf(inputTargets)
		);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return super.charTyped(event) || inputRouter.route(
			new UiInputEvent.CharacterTyped(event.codepoint()),
			List.copyOf(inputTargets)
		);
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

	private void createSearchBox() {
		searchBox = new EditBox(
			font,
			layout.searchField.left() + 6,
			layout.searchField.top() + 1,
			(int) layout.searchField.width() - 12,
			(int) layout.searchField.height() - 2,
			Component.translatable("client.gui.auto_librarian.targets.search")
		);
		searchBox.setBordered(false);
		searchBox.setMaxLength(80);
		searchBox.setTextColor(ClickGuiTheme.TEXT_PRIMARY);
		searchBox.setTextColorUneditable(ClickGuiTheme.TEXT_SECONDARY);
		searchBox.setHint(
			Component.translatable("client.gui.auto_librarian.targets.search_hint")
		);
		searchBox.setValue(searchQuery);
		searchBox.setResponder(value -> {
			searchQuery = value;
			controlsDirty = true;
		});
		addRenderableWidget(searchBox);
	}

	private void ensureCandidate() {
		if (candidateId != null && EnchantmentCatalog.find(catalog, candidateId) != null) {
			return;
		}
		EnchantmentCatalog.Entry first =
			RecommendedEnchantments.select(catalog, 1).stream().findFirst()
				.orElseGet(() -> catalog.stream().findFirst().orElse(null));
		if (first != null) {
			selectCandidate(first);
		}
	}

	private void rebuildControls() {
		controlsDirty = false;
		inputRouter.clear();
		inputTargets.clear();
		actionViews.clear();
		targetViews.clear();
		sliderViews.clear();

		addAction(layout.backButton, Component.literal("<"), this::onClose, null, true);

		int resultY = layout.searchField.bottom() + 8;
		for (EnchantmentCatalog.Entry entry :
			EnchantmentCatalog.search(catalog, searchQuery, layout.searchResults)) {
			Rect bounds = new Rect(
				layout.leftPane.left() + 12,
				resultY,
				layout.leftPane.width() - 24,
				27
			);
			addAction(
				bounds,
				Component.literal(entry.displayName() + "  " + entry.id()),
				() -> selectCandidate(entry),
				null,
				entry.id().equals(candidateId)
			);
			resultY += 31;
		}

		int candidateY = layout.leftPane.bottom() - 75;
		addAction(
			new Rect(layout.leftPane.left() + 12, candidateY, 30, 27),
			Component.literal("-"),
			() -> changeCandidateLevel(-1),
			null,
			false
		);
		addAction(
			new Rect(layout.leftPane.left() + 46, candidateY, 30, 27),
			Component.literal("+"),
			() -> changeCandidateLevel(1),
			null,
			false
		);
		addAction(
			new Rect(
				layout.leftPane.left() + 80,
				candidateY,
				layout.leftPane.width() - 92,
				27
			),
			Component.translatable("client.gui.auto_librarian.targets.add"),
			this::addCandidate,
			null,
			true
		);

		List<EnchantmentTarget> targets = setting.value();
		page = clampPage(page, targets.size());
		int firstIndex = page * layout.targetRows;
		int rowY = layout.rightPane.top() + 38;
		for (int index = firstIndex;
			index < Math.min(targets.size(), firstIndex + layout.targetRows);
			index++) {
			int targetIndex = index;
			EnchantmentTarget target = targets.get(index);
			Rect row = new Rect(
				layout.rightPane.left() + 12,
				rowY,
				layout.rightPane.width() - 56,
				29
			);
			ClickControl select = new ClickControl()
				.on(MouseButton.LEFT, () -> selectTarget(targetIndex));
			select.layout(row);
			inputTargets.add(select);
			targetViews.add(new TargetView(targetIndex, target, row));

			Rect remove = new Rect(
				layout.rightPane.right() - 40,
				rowY,
				28,
				29
			);
			addAction(
				remove,
				Component.literal("×"),
				() -> removeTarget(targetIndex),
				null,
				false
			);
			rowY += 33;
		}

		int navigationY =
			layout.rightPane.top() + 42 + layout.targetRows * 33;
		addAction(
			new Rect(layout.rightPane.left() + 12, navigationY, 86, 27),
			Component.translatable("client.gui.auto_librarian.targets.previous"),
			this::previousPage,
			null,
			false
		);
		addAction(
			new Rect(layout.rightPane.left() + 102, navigationY, 86, 27),
			Component.translatable("client.gui.auto_librarian.targets.next"),
			this::nextPage,
			null,
			false
		);
		addSelectedTargetControls();
	}

	private void addSelectedTargetControls() {
		if (setting.value().isEmpty()) {
			selectedTarget = 0;
			return;
		}
		selectedTarget = Math.clamp(selectedTarget, 0, setting.value().size() - 1);
		EnchantmentTarget target = setting.value().get(selectedTarget);
		int top = layout.rightPane.top() + 95 + layout.targetRows * 33;
		int left = layout.rightPane.left() + 12;
		int width = (int) layout.rightPane.width() - 24;

		addAction(
			new Rect(left, top, 38, 27),
			Component.literal("-"),
			() -> adjustSelectedLevel(-1),
			null,
			false
		);
		addAction(
			new Rect(left + 42, top, 38, 27),
			Component.literal("+"),
			() -> adjustSelectedLevel(1),
			null,
			false
		);
		addAction(
			new Rect(left + 84, top, width - 84, 27),
			Component.translatable(
				target.anyLevel()
					? "client.gui.auto_librarian.targets.any_level"
					: "client.gui.auto_librarian.targets.exact_level"
			),
			this::toggleSelectedLevelMode,
			null,
			target.anyLevel()
		);
		int sliderWidth = (width - 10) / 2;
		addPriceSlider(
			new Rect(left, top + 56, sliderWidth, 12),
			"client.gui.auto_librarian.targets.minimum_price",
			true
		);
		addPriceSlider(
			new Rect(left + sliderWidth + 10, top + 56, sliderWidth, 12),
			"client.gui.auto_librarian.targets.maximum_price",
			false
		);
	}

	private void addPriceSlider(Rect bounds, String translationKey, boolean minimum) {
		SliderControl control = new SliderControl(new RangedDoubleBinding() {
			@Override
			public Double get() {
				EnchantmentTarget target = selected();
				return (double) (minimum
					? target.minEmeraldPrice()
					: target.maxEmeraldPrice());
			}

			@Override
			public BindingUpdateResult set(Double value) {
				try {
					EnchantmentTarget target = selected();
					int price = (int) Math.round(value);
					if (minimum) {
						replaceSelected(target.withPrices(
							price,
							Math.max(price, target.maxEmeraldPrice())
						));
					} else {
						replaceSelected(target.withPrices(
							Math.min(target.minEmeraldPrice(), price),
							price
						));
					}
					return new BindingUpdateResult.Accepted();
				} catch (RuntimeException cause) {
					return new BindingUpdateResult.Rejected(cause);
				}
			}

			@Override
			public double minimum() {
				return 1.0;
			}

			@Override
			public double maximum() {
				return 64.0;
			}

			@Override
			public double step() {
				return 1.0;
			}
		});
		control.layout(bounds);
		inputTargets.add(control);
		sliderViews.add(new SliderView(bounds, translationKey, control));
	}

	private void addAction(
		Rect bounds,
		Component label,
		Runnable left,
		Runnable right,
		boolean accent
	) {
		ClickControl control = new ClickControl().on(MouseButton.LEFT, () -> {
			left.run();
			controlsDirty = true;
		});
		if (right != null) {
			control.on(MouseButton.RIGHT, () -> {
				right.run();
				controlsDirty = true;
			});
		}
		control.layout(bounds);
		inputTargets.add(control);
		actionViews.add(new ActionView(bounds, label, accent));
	}

	private void selectCandidate(EnchantmentCatalog.Entry entry) {
		candidateId = entry.id();
		candidateLevel = Math.max(1, entry.maxLevel());
		candidateAnyLevel = false;
		controlsDirty = true;
	}

	private void changeCandidateLevel(int direction) {
		TargetLevelSelection.State next = TargetLevelSelection.adjust(
			new TargetLevelSelection.State(candidateLevel, candidateAnyLevel),
			EnchantmentCatalog.maxLevel(catalog, candidateId),
			direction
		);
		candidateLevel = next.level();
		candidateAnyLevel = next.anyLevel();
	}

	private void addCandidate() {
		if (candidateId == null) {
			return;
		}
		if (setting.add(new EnchantmentTarget(
			candidateId,
			candidateLevel,
			candidateAnyLevel,
			1,
			64
		))) {
			selectedTarget = setting.value().size() - 1;
			page = selectedTarget / layout.targetRows;
		}
	}

	private void selectTarget(int index) {
		selectedTarget = index;
		controlsDirty = true;
	}

	private void removeTarget(int index) {
		setting.remove(setting.value().get(index).enchantmentId());
		selectedTarget = Math.max(0, Math.min(index, setting.value().size() - 1));
		page = clampPage(page, setting.value().size());
	}

	private void previousPage() {
		page = Math.max(0, page - 1);
	}

	private void nextPage() {
		page = Math.min(pageCount(setting.value().size()) - 1, page + 1);
	}

	private void adjustSelectedLevel(int direction) {
		EnchantmentTarget target = selected();
		TargetLevelSelection.State next = TargetLevelSelection.adjust(
			new TargetLevelSelection.State(target.level(), target.anyLevel()),
			EnchantmentCatalog.maxLevel(catalog, target.enchantmentId()),
			direction
		);
		replaceSelected(new EnchantmentTarget(
			target.enchantmentId(),
			next.level(),
			next.anyLevel(),
			target.minEmeraldPrice(),
			target.maxEmeraldPrice()
		));
	}

	private void toggleSelectedLevelMode() {
		EnchantmentTarget target = selected();
		replaceSelected(target.withAnyLevel(!target.anyLevel()));
	}

	private EnchantmentTarget selected() {
		return setting.value().get(selectedTarget);
	}

	private void replaceSelected(EnchantmentTarget target) {
		setting.replace(selectedTarget, target);
	}

	private void renderHeader(UiPainter painter, int mouseX, int mouseY) {
		if (layout.backButton.contains(mouseX, mouseY)) {
			painter.roundedRect(
				layout.backButton.left(),
				layout.backButton.top(),
				(int) layout.backButton.width(),
				(int) layout.backButton.height(),
				8,
				ClickGuiTheme.ROW_HOVER
			);
		}
		painter.text(
			"<",
			layout.backButton.left() + 8,
			layout.backButton.top() + 7,
			ClickGuiTheme.ACCENT
		);
		painter.text(
			Component.translatable("client.gui.auto_librarian.targets.title"),
			layout.content.left() + 48,
			layout.content.top() + 18,
			ClickGuiTheme.TEXT_PRIMARY
		);
	}

	private void renderSearchPane(UiPainter painter, int mouseX, int mouseY) {
		renderPane(painter, layout.leftPane);
		painter.text(
			Component.translatable("client.gui.auto_librarian.targets.search"),
			layout.leftPane.left() + 12,
			layout.leftPane.top() + 13,
			ClickGuiTheme.TEXT_SECONDARY
		);
		painter.roundedRectWithBorder(
			layout.searchField.left(),
			layout.searchField.top(),
			(int) layout.searchField.width(),
			(int) layout.searchField.height(),
			8,
			1,
			ClickGuiTheme.CONTROL_DARK,
			ClickGuiTheme.OUTLINE
		);

		String candidate = candidateId == null
			? Component.translatable(
				"client.gui.auto_librarian.targets.no_candidate"
			).getString()
			: Component.translatable(
				candidateAnyLevel
					? "client.gui.auto_librarian.targets.candidate_any"
					: "client.gui.auto_librarian.targets.candidate_exact",
				candidateId,
				candidateLevel
			).getString();
		painter.text(
			painter.trimToWidth(candidate, (int) layout.leftPane.width() - 24),
			layout.leftPane.left() + 12,
			layout.leftPane.bottom() - 104,
			ClickGuiTheme.TEXT_SECONDARY
		);
		renderActions(painter, mouseX, mouseY, layout.leftPane);
	}

	private void renderTargetsPane(UiPainter painter, int mouseX, int mouseY) {
		renderPane(painter, layout.rightPane);
		painter.text(
			Component.translatable(
				"client.gui.auto_librarian.targets.configured",
				setting.value().size()
			),
			layout.rightPane.left() + 12,
			layout.rightPane.top() + 13,
			ClickGuiTheme.TEXT_SECONDARY
		);
		for (TargetView view : targetViews) {
			int color = view.index() == selectedTarget
				? ClickGuiTheme.SELECTED
				: view.bounds().contains(mouseX, mouseY)
					? ClickGuiTheme.ROW_HOVER
					: ClickGuiTheme.CONTROL_DARK;
			painter.roundedRect(
				view.bounds().left(),
				view.bounds().top(),
				(int) view.bounds().width(),
				(int) view.bounds().height(),
				8,
				color
			);
			String level = view.target().anyLevel()
				? Component.translatable(
					"client.gui.auto_librarian.targets.any_level"
				).getString()
				: "Lv." + view.target().level();
			String text = EnchantmentCatalog.displayName(
				catalog,
				view.target().enchantmentId()
			) + "  " + level;
			painter.text(
				painter.trimToWidth(text, (int) view.bounds().width() - 14),
				view.bounds().left() + 7,
				view.bounds().top() + 9,
				view.index() == selectedTarget
					? 0xFF101820
					: ClickGuiTheme.TEXT_PRIMARY
			);
		}
		if (setting.value().isEmpty()) {
			painter.text(
				Component.translatable(
					"client.gui.auto_librarian.targets.empty"
				),
				layout.rightPane.left() + 12,
				layout.rightPane.top() + 48,
				ClickGuiTheme.TEXT_SECONDARY
			);
		} else {
			EnchantmentTarget target = selected();
			String selectedText = Component.translatable(
				"client.gui.auto_librarian.targets.selected",
				EnchantmentCatalog.displayName(catalog, target.enchantmentId())
			).getString();
			painter.text(
				painter.trimToWidth(
					selectedText,
					(int) layout.rightPane.width() - 24
				),
				layout.rightPane.left() + 12,
				layout.rightPane.top() + 76 + layout.targetRows * 33,
				ClickGuiTheme.TEXT_SECONDARY
			);
		}
		renderActions(painter, mouseX, mouseY, layout.rightPane);
		renderSliders(painter);
	}

	private void renderSliders(UiPainter painter) {
		for (SliderView view : sliderViews) {
			String value = Integer.toString((int) Math.round(view.control().value()));
			painter.text(
				Component.translatable(view.translationKey()),
				view.bounds().left(),
				view.bounds().top() - painter.lineHeight() - 3,
				ClickGuiTheme.TEXT_SECONDARY
			);
			SliderPainter.draw(painter, view.bounds(), view.control(), value);
		}
	}

	private void renderPane(UiPainter painter, Rect pane) {
		painter.roundedRect(
			pane.left(),
			pane.top(),
			(int) pane.width(),
			(int) pane.height(),
			14,
			ClickGuiTheme.SIDEBAR
		);
	}

	private void renderActions(
		UiPainter painter,
		int mouseX,
		int mouseY,
		Rect pane
	) {
		for (ActionView action : actionViews) {
			if (!intersects(action.bounds(), pane)) {
				continue;
			}
			int color = action.accent()
				? ClickGuiTheme.ACCENT
				: action.bounds().contains(mouseX, mouseY)
					? ClickGuiTheme.ROW_HOVER
					: ClickGuiTheme.CONTROL_DARK;
			painter.roundedRectWithBorder(
				action.bounds().left(),
				action.bounds().top(),
				(int) action.bounds().width(),
				(int) action.bounds().height(),
				8,
				1,
				color,
				ClickGuiTheme.OUTLINE
			);
			String label = action.label().getString();
			label = painter.trimToWidth(
				label,
				(int) action.bounds().width() - 10
			);
			painter.text(
				label,
				action.bounds().left()
					+ ((int) action.bounds().width() - painter.textWidth(label)) / 2,
				action.bounds().top()
					+ ((int) action.bounds().height() - painter.lineHeight()) / 2,
				action.accent() ? 0xFF101820 : ClickGuiTheme.TEXT_PRIMARY
			);
		}
	}

	private int pageCount(int size) {
		return Math.max(1, (size + layout.targetRows - 1) / layout.targetRows);
	}

	private int clampPage(int page, int size) {
		return Math.clamp(page, 0, pageCount(size) - 1);
	}

	private static boolean intersects(Rect first, Rect second) {
		return first.right() > second.left()
			&& first.left() < second.right()
			&& first.bottom() > second.top()
			&& first.top() < second.bottom();
	}

	private record ActionView(Rect bounds, Component label, boolean accent) {
	}

	private record TargetView(
		int index,
		EnchantmentTarget target,
		Rect bounds
	) {
	}

	private record SliderView(
		Rect bounds,
		String translationKey,
		SliderControl control
	) {
	}

	private record Layout(
		Rect window,
		Rect content,
		Rect backButton,
		Rect leftPane,
		Rect rightPane,
		Rect searchField,
		int targetRows,
		int searchResults
	) {
		private static Layout calculate(int screenWidth, int screenHeight) {
			int margin = Math.clamp(Math.min(screenWidth, screenHeight) / 24, 10, 22);
			int width = Math.min(screenWidth - margin * 2, 900);
			int height = Math.min(screenHeight - margin * 2, 560);
			int x = (screenWidth - width) / 2;
			int y = (screenHeight - height) / 2;
			Rect window = new Rect(x, y, width, height);
			Rect content = new Rect(x + 5, y + 5, width - 10, height - 10);
			int paneTop = y + 54;
			int paneHeight = height - 71;
			int gap = 8;
			int paneWidth = (width - 34 - gap) / 2;
			Rect leftPane = new Rect(x + 13, paneTop, paneWidth, paneHeight);
			Rect rightPane = new Rect(
				leftPane.right() + gap,
				paneTop,
				paneWidth,
				paneHeight
			);
			int targetRows = Math.clamp((paneHeight - 163) / 33, 1, 6);
			int searchResults = Math.clamp((paneHeight - 170) / 31, 1, 5);
			return new Layout(
				window,
				content,
				new Rect(x + 16, y + 17, 26, 24),
				leftPane,
				rightPane,
				new Rect(leftPane.left() + 12, leftPane.top() + 31, paneWidth - 24, 30),
				targetRows,
				searchResults
			);
		}
	}
}
