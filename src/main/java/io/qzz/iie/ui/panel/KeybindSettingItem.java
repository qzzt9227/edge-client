package io.qzz.iie.ui.panel;

import com.mojang.blaze3d.platform.InputConstants;
import io.qzz.iie.setting.KeybindSetting;
import io.qzz.iie.setting.KeybindValue;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class KeybindSettingItem implements InlineSettingItem {
	private static final int KEY_ESCAPE = 256;
	private static final int KEY_BACKSPACE = 259;
	private static final int KEY_DELETE = 261;

	private final KeybindSetting setting;
	private boolean listening;

	public KeybindSettingItem(KeybindSetting setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
	}

	@Override
	public KeybindSetting setting() {
		return setting;
	}

	@Override
	public int height() {
		return 11;
	}

	@Override
	public int preferredWidth(UiPainter painter) {
		return painter.textWidth("Bind: ") + painter.textWidth("RIGHT_CONTROL") + 8;
	}

	public boolean isListening() {
		return listening;
	}

	public void setListening(boolean listening) {
		this.listening = listening;
	}

	@Override
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
		boolean hovered = mouseX >= x && mouseX <= x + width
			&& mouseY >= y && mouseY < y + height();
		int textY = y + 1;
		int availableWidth = width - 8;

		if (listening) {
			painter.text("[Binding...]", x + 4, textY, ClickGuiTheme.SETTING_BINDING);
			return;
		}

		String prefix = "Bind: ";
		KeybindValue val = setting.value();
		String keyStr = !val.isBound() ? "NONE" : getKeyName(val.keyCode());
		int keyColor = !val.isBound() ? ClickGuiTheme.MODULE_DISABLED : ClickGuiTheme.SETTING_BIND;

		painter.marqueeTwoPartText(
			prefix,
			ClickGuiTheme.SETTING_TEXT,
			keyStr,
			keyColor,
			x + 4,
			textY,
			availableWidth,
			hovered,
			time
		);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height()) {
			if (button == 0) {
				listening = !listening;
				return true;
			}
		} else if (listening) {
			listening = false;
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scancode, int modifiers) {
		if (!listening) {
			return false;
		}
		if (keyCode == KEY_ESCAPE) {
			listening = false;
			return true;
		}
		if (keyCode == KEY_BACKSPACE || keyCode == KEY_DELETE) {
			setting.clear();
			listening = false;
			return true;
		}
		if (keyCode > 0) {
			setting.bind(keyCode);
			listening = false;
			return true;
		}
		return false;
	}

	private static String getKeyName(int keyCode) {
		try {
			return InputConstants.Type.KEYSYM
				.getOrCreate(keyCode)
				.getDisplayName()
				.getString();
		} catch (Exception e) {
			return "KEY_" + keyCode;
		}
	}
}
