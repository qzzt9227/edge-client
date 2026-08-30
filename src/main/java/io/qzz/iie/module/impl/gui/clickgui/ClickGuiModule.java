package io.qzz.iie.module.impl.gui.clickgui;

import io.qzz.iie.api.hud.HudPosition;
import io.qzz.iie.api.hud.HudPositionSetting;
import io.qzz.iie.i18n.ClientI18n;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.ChoiceOption;
import io.qzz.iie.setting.ChoiceSetting;
import io.qzz.iie.setting.ColorSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.KeybindSetting;
import io.qzz.iie.setting.KeybindValue;

import java.util.List;

/**
 * Click GUI 与 HUD 可视化外观/语言设置容器。
 *
 * <p>该模块不拥有绘制逻辑，也没有启用/禁用语义。GUI 和 HUD 适配器只读取
 * 这里声明的设置，因此扩展功能不需要接触具体渲染代码。</p>
 */
public final class ClickGuiModule extends Module {
	private static final int GLFW_KEY_RIGHT_SHIFT = 344;

	private final ChoiceSetting<String> language = setting(new ChoiceSetting<>(
		"language",
		"client.setting.click_gui.language",
		ClientI18n.LANG_ZH_CN,
		List.of(
			new ChoiceOption<>("auto", "client.option.click_gui.language.auto", ClientI18n.LANG_AUTO),
			new ChoiceOption<>("zh_cn", "client.option.click_gui.language.zh_cn", ClientI18n.LANG_ZH_CN),
			new ChoiceOption<>("en_us", "client.option.click_gui.language.en_us", ClientI18n.LANG_EN_US)
		)
	));

	private final ChoiceSetting<String> customFont = setting(new ChoiceSetting<>(
		"custom_font",
		"client.setting.click_gui.custom_font",
		io.qzz.iie.font.ClientFontManager.DEFAULT_FONT,
		io.qzz.iie.font.ClientFontManager.getAvailableFontOptions()
	));

	private final HudEditorSetting hudEditor = setting(new HudEditorSetting(
		"hud_editor",
		"client.setting.click_gui.hud_editor"
	));

	private final BooleanSetting armorHudEnabled = setting(new BooleanSetting(
		"armor_hud_enabled",
		"client.setting.click_gui.armor_hud_enabled",
		true
	));

	private final HudPositionSetting armorHudPosition = setting(new HudPositionSetting(
		"armor_hud_position",
		"client.setting.click_gui.armor_hud_position",
		new HudPosition(0.6529017857142857, 1.0)
	));

	private final BooleanSetting potionHudEnabled = setting(new BooleanSetting(
		"potion_hud_enabled",
		"client.setting.click_gui.potion_hud_enabled",
		true
	));

	private final HudPositionSetting potionHudPosition = setting(new HudPositionSetting(
		"potion_hud_position",
		"client.setting.click_gui.potion_hud_position",
		new HudPosition(0.32155172413793104, 1.0)
	));

	private final BooleanSetting arrayListEnabled = setting(new BooleanSetting(
		"array_list_enabled",
		"client.setting.click_gui.array_list_enabled",
		true
	));

	private final HudPositionSetting arrayListPosition = setting(new HudPositionSetting(
		"array_list_position",
		"client.setting.click_gui.array_list_position",
		new HudPosition(0.0, 0.4772570449323752)
	));

	private final BooleanSetting cpsHudEnabled = setting(new BooleanSetting(
		"cps_hud_enabled",
		"client.setting.click_gui.cps_hud_enabled",
		true
	));

	private final HudPositionSetting cpsHudPosition = setting(new HudPositionSetting(
		"cps_hud_position",
		"client.setting.click_gui.cps_hud_position",
		new HudPosition(0.0, 0.35)
	));

	private final BooleanSetting versionHudEnabled = setting(new BooleanSetting(
		"version_hud_enabled",
		"client.setting.click_gui.version_hud_enabled",
		true
	));

	private final HudPositionSetting versionHudPosition = setting(new HudPositionSetting(
		"version_hud_position",
		"client.setting.click_gui.version_hud_position",
		new HudPosition(0.0, 0.0)
	));

	private final KeybindSetting openShortcut = setting(new KeybindSetting(
		"open_shortcut",
		"client.setting.click_gui.open_shortcut",
		new KeybindValue(GLFW_KEY_RIGHT_SHIFT)
	));

	private final DoubleSetting guiTextScale = setting(new DoubleSetting(
		"gui_text_scale",
		"client.setting.click_gui.gui_text_scale",
		1.3,
		0.75,
		1.5,
		0.05
	));

	private final DoubleSetting messageBoxScale = setting(new DoubleSetting(
		"message_box_scale",
		"client.setting.click_gui.message_box_scale",
		1.0,
		0.5,
		2.0,
		0.05
	));

	private final DoubleSetting messageTextScale = setting(new DoubleSetting(
		"message_text_scale",
		"client.setting.click_gui.message_text_scale",
		1.0,
		0.5,
		2.0,
		0.05
	));

	private final DoubleSetting messageOpacity = setting(new DoubleSetting(
		"message_opacity",
		"client.setting.click_gui.message_opacity",
		0.8,
		0.1,
		1.0,
		0.05
	));

	private final ChoiceSetting<String> messageFont = setting(new ChoiceSetting<>(
		"message_font",
		"client.setting.click_gui.message_font",
		"minecraft:uniform",
		List.of(
			fontOption("default", "minecraft:default"),
			fontOption("uniform", "minecraft:uniform"),
			fontOption("alternate", "minecraft:alt")
		)
	));

	private final ColorSetting messageTextColor = setting(new ColorSetting(
		"message_text_color",
		"client.setting.click_gui.message_text_color",
		0xDEE8ED,
		false
	));

	public ClickGuiModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "click_gui"),
			"client.module.click_gui.name",
			"client.module.click_gui.description",
			100,
			false
		));

		language.addChangeListener(() -> ClientI18n.setLanguage(language.value()));
		ClientI18n.setLanguage(language.value());

		customFont.addChangeListener(() -> io.qzz.iie.font.ClientFontManager.applyFont(customFont.value()));
		io.qzz.iie.font.ClientFontManager.applyFont(customFont.value());
	}

	public ChoiceSetting<String> language() {
		return language;
	}

	public ChoiceSetting<String> customFont() {
		return customFont;
	}

	public HudEditorSetting hudEditor() {
		return hudEditor;
	}

	public BooleanSetting armorHudEnabled() {
		return armorHudEnabled;
	}

	public HudPositionSetting armorHudPosition() {
		return armorHudPosition;
	}

	public BooleanSetting potionHudEnabled() {
		return potionHudEnabled;
	}

	public HudPositionSetting potionHudPosition() {
		return potionHudPosition;
	}

	public BooleanSetting arrayListEnabled() {
		return arrayListEnabled;
	}

	public HudPositionSetting arrayListPosition() {
		return arrayListPosition;
	}

	public BooleanSetting cpsHudEnabled() {
		return cpsHudEnabled;
	}

	public HudPositionSetting cpsHudPosition() {
		return cpsHudPosition;
	}

	public BooleanSetting versionHudEnabled() {
		return versionHudEnabled;
	}

	public HudPositionSetting versionHudPosition() {
		return versionHudPosition;
	}

	public DoubleSetting guiTextScale() {
		return guiTextScale;
	}

	public KeybindSetting openShortcut() {
		return openShortcut;
	}

	public DoubleSetting messageBoxScale() {
		return messageBoxScale;
	}

	public DoubleSetting messageTextScale() {
		return messageTextScale;
	}

	public DoubleSetting messageOpacity() {
		return messageOpacity;
	}

	public ChoiceSetting<String> messageFont() {
		return messageFont;
	}

	public ColorSetting messageTextColorSetting() {
		return messageTextColor;
	}

	public int messageTextColor() {
		return 0xFF000000 | messageTextColor.rgb();
	}

	private static ChoiceOption<String> fontOption(String id, String resourceId) {
		return new ChoiceOption<>(
			id,
			"client.option.click_gui.font." + id,
			resourceId
		);
	}
}
