package io.qzz.iie.ui.panel;

import io.qzz.iie.api.hud.HudElementPreview;
import io.qzz.iie.api.hud.HudPosition;
import io.qzz.iie.api.hud.HudPositionEditorApi;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.api.setting.SettingEditorApi;
import io.qzz.iie.config.JsonConfigService;
import io.qzz.iie.module.ModuleCategory;
import io.qzz.iie.module.ModuleManager;
import io.qzz.iie.module.impl.combat.test.TestModule;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;
import io.qzz.iie.setting.KeybindValue;
import net.minecraft.client.gui.screens.Screen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class PanelContract {
	private PanelContract() {
	}

	public static void verify() {
		verifyCategoryPanelDragAndCollapse();
		verifyModulePanelToggleAndAccordion();
		verifyInlineSettingInteractions();
		verifyDefaultLayoutDoesNotOverlap();
		verifyLayoutPersistsInConfig();
		verifyMarqueeScrollLogic();
		verifyHudCornerSnapping();
		verifyClientLanguageSwitching();
	}

	private static void verifyMarqueeScrollLogic() {
		int availableWidth = 100;
		int shortTextWidth = 80;
		int longTextWidth = 180;

		// Not hovered -> 0 offset
		check(
			io.qzz.iie.ui.render.UiPainter.calculateMarqueeOffset(longTextWidth, availableWidth, false, 1500L) == 0,
			"unhovered text must have zero offset"
		);

		// Short text hovered -> 0 offset
		check(
			io.qzz.iie.ui.render.UiPainter.calculateMarqueeOffset(shortTextWidth, availableWidth, true, 1500L) == 0,
			"short text within available width must not scroll"
		);

		// Long text hovered at start pause -> 0 offset
		check(
			io.qzz.iie.ui.render.UiPainter.calculateMarqueeOffset(longTextWidth, availableWidth, true, 200L) == 0,
			"marquee text during initial pause must have zero offset"
		);

		// Long text hovered during scroll -> offset > 0
		int midOffset = io.qzz.iie.ui.render.UiPainter.calculateMarqueeOffset(longTextWidth, availableWidth, true, 2500L);
		check(midOffset > 0 && midOffset <= 80, "marquee text during scroll phase must have positive offset <= overflow");
	}

	private static void verifyCategoryPanelDragAndCollapse() {
		ModuleManager manager = new ModuleManager();
		TestModule module = manager.register(new TestModule());

		AtomicInteger hudOpened = new AtomicInteger();
		HudPositionEditorApi hudApi = new HudPositionEditorApi() {
			@Override
			public void register(HudPositionSetting setting, HudElementPreview preview) {
			}

			@Override
			public void open(HudPositionSetting setting, Screen parent) {
				hudOpened.incrementAndGet();
			}
		};
		SettingEditorApi editorApi = SettingEditorApi.noop();
		InlineSettingFactory factory = new InlineSettingFactory(hudApi, editorApi, () -> null);

		CategoryPanel panel = new CategoryPanel(
			module.category(),
			List.of(module),
			manager,
			factory,
			20,
			30
		);

		check(panel.x() == 20 && panel.y() == 30, "panel must initialize at specified position");
		check(panel.isOpened(), "panel must be opened by default");

		// Header left click starts dragging
		check(panel.mouseClicked(25, 35, 0), "clicking header must be consumed");
		panel.mouseDragged(50, 60, 25, 25, 0, 800, 600);
		check(panel.x() == 45 && panel.y() == 55, "dragging must update panel coordinates");

		panel.mouseReleased(50, 60, 0);
		panel.mouseDragged(100, 100, 50, 40, 0, 800, 600);
		check(panel.x() == 45 && panel.y() == 55, "releasing mouse must terminate drag");

		// Right click on header collapses panel
		check(panel.mouseClicked(46, 56, 1), "right click on header must collapse panel");
		check(!panel.isOpened(), "panel must be collapsed");

		// Right click on header re-opens panel
		check(panel.mouseClicked(46, 56, 1), "right click on header must reopen panel");
		check(panel.isOpened(), "panel must be opened");

		// Collapse icon click on right side collapses panel
		check(panel.mouseClicked(panel.x() + panel.width() - 5, panel.y() + 5, 0),
			"clicking collapse symbol must toggle opened state");
		check(!panel.isOpened(), "panel must be collapsed after clicking collapse symbol");
	}

	private static void verifyModulePanelToggleAndAccordion() {
		ModuleManager manager = new ModuleManager();
		TestModule module = manager.register(new TestModule());
		InlineSettingFactory factory = new InlineSettingFactory(
			HudPositionEditorApi.noop(),
			SettingEditorApi.noop(),
			() -> null
		);

		ModulePanelItem item = new ModulePanelItem(module, manager, factory);
		check(!module.isEnabled(), "module must start disabled");
		check(!item.isExpanded(), "settings must start collapsed");
		check(item.height() == 14, "collapsed item height must be 14");

		// Left click on header toggles module
		check(item.mouseClicked(10, 10, 0, 0, 0, 96), "left click on module must be consumed");
		check(module.isEnabled(), "module must be enabled after left click");

		check(item.mouseClicked(10, 10, 0, 0, 0, 96), "second left click must be consumed");
		check(!module.isEnabled(), "module must be disabled after second left click");

		// Right click on header expands settings accordion
		check(item.mouseClicked(10, 10, 1, 0, 0, 96), "right click on module must be consumed");
		check(item.isExpanded(), "settings must be expanded after right click");
		check(item.height() > 14, "expanded height must include setting items");

		// Right click collapses settings accordion
		check(item.mouseClicked(10, 10, 1, 0, 0, 96), "second right click must be consumed");
		check(!item.isExpanded(), "settings must be collapsed after second right click");
	}

	private static void verifyInlineSettingInteractions() {
		// 1. BooleanSettingItem
		BooleanSetting boolSetting = new BooleanSetting("bool", "test.bool", false);
		BooleanSettingItem boolItem = new BooleanSettingItem(boolSetting);
		check(boolItem.mouseClicked(5, 5, 0, 0, 0, 96), "clicking boolean item must be consumed");
		check(boolSetting.value(), "boolean setting value must be flipped to true");

		// 2. DoubleSettingItem
		DoubleSetting doubleSetting = new DoubleSetting("num", "test.num", 5.0, 0.0, 10.0, 1.0);
		DoubleSettingItem doubleItem = new DoubleSettingItem(doubleSetting);
		check(doubleItem.mouseClicked(48, 5, 0, 0, 0, 96), "clicking slider must be consumed");
		check(doubleSetting.value() >= 4.0 && doubleSetting.value() <= 6.0, "slider click must update value near center");

		// 3. ChoiceSettingItem (Expandable popup drawer)
		ChoiceSetting<String> choiceSetting = new ChoiceSetting<>(
			"mode",
			"test.mode",
			"A",
			List.of(
				new ChoiceOption<>("a", "test.mode.a", "A"),
				new ChoiceOption<>("b", "test.mode.b", "B"),
				new ChoiceOption<>("c", "test.mode.c", "C")
			)
		);
		ChoiceSettingItem<String> choiceItem = new ChoiceSettingItem<>(choiceSetting);
		check(!choiceItem.isExpanded(), "choice item must start collapsed");
		check(choiceItem.height() == 12, "collapsed choice item height must be 12");

		// Click header expands dropdown drawer
		check(choiceItem.mouseClicked(10, 5, 0, 0, 0, 96), "clicking mode header must be consumed");
		check(choiceItem.isExpanded(), "choice item must be expanded after clicking header");
		check(choiceItem.height() == 12 + 3 * 11, "expanded height must include all 3 options");

		// Click option "C" (index 2, y = 12 + 2 * 11 + 5 = 39)
		check(choiceItem.mouseClicked(10, 39, 0, 0, 0, 96), "clicking option row must be consumed");
		check(choiceSetting.value().equals("C"), "clicking option C directly selects C");

		// Right click option "B" (index 1, y = 12 + 1 * 11 + 5 = 28) selects and closes
		check(choiceItem.mouseClicked(10, 28, 1, 0, 0, 96), "right clicking option row must be consumed");
		check(choiceSetting.value().equals("B"), "right click selects option B");
		check(!choiceItem.isExpanded(), "right click on option must close drawer");

		// 4. KeybindSettingItem
		KeybindSetting keybindSetting = new KeybindSetting("key", "test.key", KeybindValue.unbound());
		KeybindSettingItem keybindItem = new KeybindSettingItem(keybindSetting);
		check(!keybindItem.isListening(), "keybind must not be listening initially");
		check(keybindItem.mouseClicked(10, 5, 0, 0, 0, 96), "clicking keybind must enter listening");
		check(keybindItem.isListening(), "keybind must be listening");

		// Press key 82 (R)
		check(keybindItem.keyPressed(82, 0, 0), "key press must be consumed while listening");
		check(!keybindItem.isListening(), "keybind must stop listening after key press");
		check(keybindSetting.value().keyCode() == 82, "keybind value must be updated");

		// Cancel with ESC
		keybindItem.mouseClicked(10, 5, 0, 0, 0, 96);
		check(keybindItem.isListening(), "keybind must re-enter listening");
		check(keybindItem.keyPressed(256, 0, 0), "ESC must cancel listening");
		check(!keybindItem.isListening(), "listening must be cancelled");
		check(keybindSetting.value().keyCode() == 82, "original keybind must be retained on ESC");

		// 5. HudPositionSettingItem
		AtomicBoolean hudEdited = new AtomicBoolean();
		HudPositionSetting hudSetting = new HudPositionSetting(
			"pos",
			"test.pos",
			new HudPosition(0.5, 0.5)
		);
		HudPositionSettingItem hudItem = new HudPositionSettingItem(
			hudSetting,
			s -> hudEdited.set(true)
		);
		check(hudItem.mouseClicked(10, 5, 0, 0, 0, 96), "clicking HUD position setting must be consumed");
		check(hudEdited.get(), "HUD position editor callback must be invoked");
	}

	private static void verifyDefaultLayoutDoesNotOverlap() {
		CategoryPositionManager.clear();
		ModuleManager manager = new ModuleManager();
		TestModule mod1 = manager.register(new TestModule());

		InlineSettingFactory factory = new InlineSettingFactory(
			HudPositionEditorApi.noop(),
			SettingEditorApi.noop(),
			() -> null
		);

		CategoryPanel p1 = new CategoryPanel(
			new ModuleCategory("combat", "client.category.combat"),
			List.of(mod1),
			manager,
			factory,
			20,
			20
		);

		CategoryPanel p2 = new CategoryPanel(
			new ModuleCategory("movement", "client.category.movement"),
			List.of(mod1),
			manager,
			factory,
			20 + p1.width() + 14,
			20
		);

		check(p2.x() >= p1.x() + p1.width() + 14, "adjacent panels must not overlap horizontally");
	}

	private static void verifyLayoutPersistsInConfig() {
		Path tempDir = null;
		try {
			tempDir = Files.createTempDirectory("edge-layout-test");
			Path configPath = tempDir.resolve("edge-config.json");

			ModuleManager manager = new ModuleManager();
			manager.register(new TestModule());
			JsonConfigService config = new JsonConfigService(manager, configPath);
			check(config.load(), "config must load");

			CategoryPositionManager.updateState("combat", 150, 75, false, java.util.Set.of("client:test_module"));
			CategoryPositionManager.save();

			check(Files.isRegularFile(configPath), "updating panel state must write to edge-config.json");
			String json = Files.readString(configPath);
			check(json.contains("\"guiLayout\""), "edge-config.json must contain guiLayout section");
			check(json.contains("\"combat\"") && json.contains("150"), "saved coordinates must be in json");
			check(json.contains("\"client:test_module\""), "expanded module IDs must be stored in json");

			CategoryPositionManager.clear();
			check(CategoryPositionManager.getState("combat").isEmpty(), "clearing in-memory must empty state");

			ModuleManager restoredManager = new ModuleManager();
			restoredManager.register(new TestModule());
			JsonConfigService restoredConfig = new JsonConfigService(restoredManager, configPath);
			check(restoredConfig.load(), "restoring config must succeed");

			Optional<CategoryPositionManager.PanelState> state = CategoryPositionManager.getState("combat");
			check(state.isPresent(), "combat panel state must be restored from config");
			check(state.get().x() == 150 && state.get().y() == 75 && !state.get().opened(), "restored values must match");
			check(state.get().expandedModules().contains("client:test_module"), "restored expanded modules must match");
		} catch (IOException e) {
			throw new AssertionError("layout persistence test failed", e);
		} finally {
			if (tempDir != null) {
				try {
					Files.deleteIfExists(tempDir.resolve("edge-config.json"));
					Files.deleteIfExists(tempDir);
				} catch (IOException ignored) {
				}
			}
		}
	}

	private static void verifyHudCornerSnapping() {
		HudPositionSetting setting = new HudPositionSetting("hud", "test.hud", new HudPosition(0.5, 0.5));
		io.qzz.iie.api.hud.HudPositionDrag drag = new io.qzz.iie.api.hud.HudPositionDrag(setting);
		drag.layout(1000, 800, 100, 50);

		// Begin drag at center
		check(drag.begin(475, 385), "must begin drag");

		// Move close to top-left corner (rawLeft = 28 - 25 = 3, rawTop = 18 - 10 = 8, threshold is 16)
		drag.move(28, 18);
		check(drag.draft().x() == 0.0 && drag.draft().y() == 0.0, "drag near top-left must snap to 0.0, 0.0");

		// Move close to bottom-right corner (rawLeft = 920 - 25 = 895, rawTop = 755 - 10 = 745)
		drag.move(920, 755);
		check(drag.draft().x() == 1.0 && drag.draft().y() == 1.0, "drag near bottom-right must snap to 1.0, 1.0");

		drag.end();
		check(setting.value().x() == 1.0 && setting.value().y() == 1.0, "setting must store snapped coordinates");
	}

	private static void verifyClientLanguageSwitching() {
		io.qzz.iie.i18n.ClientI18n.setLanguage("zh_cn");
		check(
			io.qzz.iie.i18n.ClientI18n.translate("client.setting.click_gui.language").equals("界面语言"),
			"zh_cn must return Chinese language name"
		);

		io.qzz.iie.i18n.ClientI18n.setLanguage("en_us");
		check(
			io.qzz.iie.i18n.ClientI18n.translate("client.setting.click_gui.language").equals("Language"),
			"en_us must return English language name"
		);

		io.qzz.iie.i18n.ClientI18n.setLanguage("auto");
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
