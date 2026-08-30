package io.qzz.iie.ui.component.control;

import io.qzz.iie.setting.ColorSetting;
import io.qzz.iie.ui.input.InputResult;
import io.qzz.iie.ui.input.MouseButton;
import io.qzz.iie.ui.input.UiInputEvent;
import io.qzz.iie.ui.input.UiInputTarget;
import io.qzz.iie.ui.layout.Rect;
import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import java.util.List;
import java.util.Objects;

/**
 * PS 风格调色盘控件：正方形 SV 拾色区、色相滑条、预置纯色色块与 HTML 十六进制输入。
 */
public final class ColorPickerControl implements UiInputTarget {
	private final ColorSetting setting;
	private Rect bounds = new Rect(0, 0, 0, 0);

	private float hue = 0.0f;
	private float saturation = 1.0f;
	private float value = 1.0f;

	private boolean draggingSv;
	private boolean draggingHue;

	private boolean editingHex;
	private String hexBuffer = "";

	public ColorPickerControl(ColorSetting setting) {
		this.setting = Objects.requireNonNull(setting, "setting");
		syncHsvFromSetting();
		this.hexBuffer = setting.hex();
	}

	public ColorSetting setting() {
		return setting;
	}

	public void layout(Rect bounds) {
		this.bounds = Objects.requireNonNull(bounds, "bounds");
	}

	public void syncHsvFromSetting() {
		float[] hsv = rgbToHsv(setting.red(), setting.green(), setting.blue());
		this.hue = hsv[0];
		this.saturation = hsv[1];
		this.value = hsv[2];
		if (!editingHex) {
			this.hexBuffer = setting.hex();
		}
	}

	public float hue() {
		return hue;
	}

	public float saturation() {
		return saturation;
	}

	public float valueBrightness() {
		return value;
	}

	public String hexText() {
		return hexBuffer;
	}

	public List<Integer> presetColors() {
		return ColorSetting.PRESETS;
	}

	public void setHue(float h) {
		setHsv(h, this.saturation, this.value);
	}

	public void setSaturation(float s) {
		setHsv(this.hue, s, this.value);
	}

	public void setValue(float v) {
		setHsv(this.hue, this.saturation, v);
	}

	public void onHexInputChanged(String hex) {
		this.hexBuffer = hex;
		tryApplyHex(hex);
	}

	public void setHsv(float h, float s, float v) {
		this.hue = Math.clamp(h, 0.0f, 360.0f) % 360.0f;
		this.saturation = Math.clamp(s, 0.0f, 1.0f);
		this.value = Math.clamp(v, 0.0f, 1.0f);
		int rgb = hsvToRgb(this.hue, this.saturation, this.value);
		if (setting.hasAlpha()) {
			setting.set((setting.alpha() << 24) | rgb);
		} else {
			setting.set(rgb);
		}
		this.hexBuffer = setting.hex();
	}

	public void selectPreset(int rgb) {
		if (setting.hasAlpha()) {
			setting.set((setting.alpha() << 24) | (rgb & 0x00FFFFFF));
		} else {
			setting.set(rgb & 0x00FFFFFF);
		}
		syncHsvFromSetting();
	}

	@Override
	public Rect inputBounds() {
		return bounds;
	}

	@Override
	public InputResult handleInput(UiInputEvent event) {
		if (event instanceof UiInputEvent.PointerPressed pressed
			&& pressed.button() == MouseButton.LEFT) {
			double mx = pressed.x();
			double my = pressed.y();

			// 检查是否点击在 SV 正方形区域
			Rect svRect = svBoxRect();
			if (svRect.contains(mx, my)) {
				draggingSv = true;
				updateSv(mx, my);
				editingHex = false;
				return InputResult.CAPTURE_POINTER;
			}

			// 检查是否点击在色相滑条
			Rect hueRect = hueBarRect();
			if (hueRect.contains(mx, my)) {
				draggingHue = true;
				updateHue(my);
				editingHex = false;
				return InputResult.CAPTURE_POINTER;
			}

			// 检查是否点击在 5 种预置纯色色块
			List<Rect> swatchRects = presetSwatchRects();
			for (int i = 0; i < swatchRects.size(); i++) {
				if (swatchRects.get(i).contains(mx, my)) {
					selectPreset(ColorSetting.PRESETS.get(i));
					editingHex = false;
					return InputResult.CONSUMED;
				}
			}

			// 检查是否点击在 Hex 输入框
			Rect hexRect = hexInputRect();
			if (hexRect.contains(mx, my)) {
				editingHex = true;
				hexBuffer = setting.hex();
				return InputResult.CONSUMED;
			} else {
				commitHex();
				editingHex = false;
			}
		}

		if (event instanceof UiInputEvent.PointerDragged dragged
			&& dragged.button() == MouseButton.LEFT) {
			if (draggingSv) {
				updateSv(dragged.x(), dragged.y());
				return InputResult.CONSUMED;
			}
			if (draggingHue) {
				updateHue(dragged.y());
				return InputResult.CONSUMED;
			}
		}

		if (event instanceof UiInputEvent.PointerReleased released
			&& released.button() == MouseButton.LEFT) {
			if (draggingSv || draggingHue) {
				draggingSv = false;
				draggingHue = false;
				return InputResult.CONSUMED;
			}
		}

		if (editingHex) {
			if (event instanceof UiInputEvent.KeyPressed pressed) {
				int key = pressed.key();
				if (key == 259) { // Backspace
					if (!hexBuffer.isEmpty()) {
						hexBuffer = hexBuffer.substring(0, hexBuffer.length() - 1);
					}
					return InputResult.CONSUMED;
				}
				if (key == 257 || key == 335 || key == 256) { // Enter or Escape
					commitHex();
					editingHex = false;
					return InputResult.CONSUMED;
				}
			}
			if (event instanceof UiInputEvent.CharacterTyped typed) {
				char c = (char) typed.codePoint();
				if (c == '#') {
					// 静默转换：忽略前导或内嵌 # 号
					return InputResult.CONSUMED;
				}
				if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
					int maxLen = setting.hasAlpha() ? 8 : 6;
					if (hexBuffer.length() < maxLen) {
						hexBuffer += Character.toUpperCase(c);
						tryApplyHex(hexBuffer);
					}
					return InputResult.CONSUMED;
				}
			}
		}

		return InputResult.IGNORED;
	}

	private void updateSv(double pointerX, double pointerY) {
		Rect sv = svBoxRect();
		if (sv.width() <= 0 || sv.height() <= 0) {
			return;
		}
		float s = (float) Math.clamp((pointerX - sv.x()) / sv.width(), 0.0, 1.0);
		float v = (float) Math.clamp(1.0 - (pointerY - sv.y()) / sv.height(), 0.0, 1.0);
		this.saturation = s;
		this.value = v;
		int rgb = hsvToRgb(this.hue, this.saturation, this.value);
		if (setting.hasAlpha()) {
			setting.set((setting.alpha() << 24) | rgb);
		} else {
			setting.set(rgb);
		}
		this.hexBuffer = setting.hex();
	}

	private void updateHue(double pointerY) {
		Rect bar = hueBarRect();
		if (bar.height() <= 0) {
			return;
		}
		float fraction = (float) Math.clamp((pointerY - bar.y()) / bar.height(), 0.0, 1.0);
		this.hue = fraction * 360.0f % 360.0f;
		int rgb = hsvToRgb(this.hue, this.saturation, this.value);
		if (setting.hasAlpha()) {
			setting.set((setting.alpha() << 24) | rgb);
		} else {
			setting.set(rgb);
		}
		this.hexBuffer = setting.hex();
	}

	private void tryApplyHex(String text) {
		try {
			int rgb = ColorSetting.parseHex(text, setting.hasAlpha());
			setting.set(rgb);
			float[] hsv = rgbToHsv(setting.red(), setting.green(), setting.blue());
			this.hue = hsv[0];
			this.saturation = hsv[1];
			this.value = hsv[2];
		} catch (Exception ignored) {
		}
	}

	private void commitHex() {
		try {
			if (!hexBuffer.isEmpty()) {
				int rgb = ColorSetting.parseHex(hexBuffer, setting.hasAlpha());
				setting.set(rgb);
				syncHsvFromSetting();
			}
		} catch (Exception e) {
			hexBuffer = setting.hex();
		}
	}

	public Rect svBoxRect() {
		int padding = 4;
		int size = Math.max(30, (int) Math.min(bounds.width() - 28, bounds.height() - 36));
		return new Rect(bounds.x() + padding, bounds.y() + padding, size, size);
	}

	public Rect hueBarRect() {
		Rect sv = svBoxRect();
		int barWidth = 10;
		int x = (int) sv.right() + 4;
		return new Rect(x, sv.y(), barWidth, sv.height());
	}

	public List<Rect> presetSwatchRects() {
		Rect sv = svBoxRect();
		int top = (int) sv.bottom() + 4;
		int count = ColorSetting.PRESETS.size();
		int swatchWidth = (int) (bounds.width() - 8) / count - 2;
		int swatchHeight = 8;
		List<Rect> list = new java.util.ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			int x = (int) bounds.x() + 4 + i * (swatchWidth + 2);
			list.add(new Rect(x, top, swatchWidth, swatchHeight));
		}
		return List.copyOf(list);
	}

	public Rect hexInputRect() {
		Rect sv = svBoxRect();
		int top = (int) sv.bottom() + 15;
		int height = 12;
		int width = (int) bounds.width() - 8;
		return new Rect(bounds.x() + 4, top, width, height);
	}

	public void render(UiPainter painter, int mouseX, int mouseY, float delta, long time) {
		// 1. 渲染 SV 正方形 2D 渐变拾色区
		Rect sv = svBoxRect();
		renderSvBox(painter, (int) sv.x(), (int) sv.y(), (int) sv.width(), (int) sv.height(), hue);

		// SV 指针圆点 / 十字准星
		int cursorX = (int) Math.round(sv.x() + saturation * sv.width());
		int cursorY = (int) Math.round(sv.y() + (1.0f - value) * sv.height());
		painter.fill(cursorX - 2, cursorY - 2, 5, 1, 0xFFFFFFFF);
		painter.fill(cursorX - 2, cursorY + 2, 5, 1, 0xFFFFFFFF);
		painter.fill(cursorX - 2, cursorY - 1, 1, 3, 0xFFFFFFFF);
		painter.fill(cursorX + 2, cursorY - 1, 1, 3, 0xFFFFFFFF);
		painter.fill(cursorX, cursorY, 1, 1, 0xFF000000);

		// 2. 渲染色相滑条
		Rect hueBar = hueBarRect();
		renderHueBar(painter, (int) hueBar.x(), (int) hueBar.y(), (int) hueBar.width(), (int) hueBar.height());

		// 色相滑条游标
		int hueCursorY = (int) Math.round(hueBar.y() + (hue / 360.0f) * hueBar.height());
		painter.outline((int) hueBar.x() - 1, hueCursorY - 1, (int) hueBar.width() + 2, 3, 0xFFFFFFFF);

		// 3. 渲染 5 种预置纯色色块
		List<Rect> swatches = presetSwatchRects();
		for (int i = 0; i < swatches.size(); i++) {
			Rect swatch = swatches.get(i);
			int presetRgb = ColorSetting.PRESETS.get(i);
			painter.fill((int) swatch.x(), (int) swatch.y(), (int) swatch.width(), (int) swatch.height(), 0xFF000000 | presetRgb);
			if (swatch.contains(mouseX, mouseY)) {
				painter.outline((int) swatch.x() - 1, (int) swatch.y() - 1, (int) swatch.width() + 2, (int) swatch.height() + 2, 0xFFFFFFFF);
			}
		}

		// 4. 渲染 HTML Hex 代码输入/显示框
		Rect hexRect = hexInputRect();
		int hexBg = editingHex ? ClickGuiTheme.CONTROL_DARK : ClickGuiTheme.PANEL_HEADER;
		painter.fill((int) hexRect.x(), (int) hexRect.y(), (int) hexRect.width(), (int) hexRect.height(), hexBg);
		painter.outline((int) hexRect.x(), (int) hexRect.y(), (int) hexRect.width(), (int) hexRect.height(), ClickGuiTheme.OUTLINE);

		// 显示纯色预览色块
		int previewSize = 8;
		painter.fill((int) hexRect.x() + 2, (int) hexRect.y() + 2, previewSize, previewSize, setting.argb());

		String hexDisplay = hexBuffer + (editingHex && (time / 500) % 2 == 0 ? "_" : "");
		painter.text(hexDisplay, (int) hexRect.x() + previewSize + 6, (int) hexRect.y() + 2, ClickGuiTheme.TEXT_PRIMARY);
	}

	public static void renderSvBox(UiPainter painter, int x, int y, int width, int height, float hue) {
		int step = 2;
		for (int py = 0; py < height; py += step) {
			float v = 1.0f - (float) py / height;
			int h = Math.min(step, height - py);
			for (int px = 0; px < width; px += step) {
				float s = (float) px / width;
				int w = Math.min(step, width - px);
				int color = 0xFF000000 | hsvToRgb(hue, s, v);
				painter.fill(x + px, y + py, w, h, color);
			}
		}
		painter.outline(x - 1, y - 1, width + 2, height + 2, ClickGuiTheme.OUTLINE);
	}

	public static void renderHueBar(UiPainter painter, int x, int y, int width, int height) {
		for (int py = 0; py < height; py++) {
			float h = (float) py / height * 360.0f;
			int color = 0xFF000000 | hsvToRgb(h, 1.0f, 1.0f);
			painter.fill(x, y + py, width, 1, color);
		}
		painter.outline(x - 1, y - 1, width + 2, height + 2, ClickGuiTheme.OUTLINE);
	}

	public static int hsvToRgb(float h, float s, float v) {
		h = ((h % 360.0f) + 360.0f) % 360.0f;
		s = Math.clamp(s, 0.0f, 1.0f);
		v = Math.clamp(v, 0.0f, 1.0f);
		int hi = (int) (Math.floor(h / 60.0f)) % 6;
		float f = (h / 60.0f) - (float) Math.floor(h / 60.0f);
		float p = v * (1.0f - s);
		float q = v * (1.0f - f * s);
		float t = v * (1.0f - (1.0f - f) * s);
		float r = 0, g = 0, b = 0;
		switch (hi) {
			case 0 -> { r = v; g = t; b = p; }
			case 1 -> { r = q; g = v; b = p; }
			case 2 -> { r = p; g = v; b = t; }
			case 3 -> { r = p; g = q; b = v; }
			case 4 -> { r = t; g = p; b = v; }
			case 5 -> { r = v; g = p; b = q; }
		}
		int ri = Math.clamp((int) Math.round(r * 255.0f), 0, 255);
		int gi = Math.clamp((int) Math.round(g * 255.0f), 0, 255);
		int bi = Math.clamp((int) Math.round(b * 255.0f), 0, 255);
		return (ri << 16) | (gi << 8) | bi;
	}

	public static float[] rgbToHsv(int r, int g, int b) {
		float rf = r / 255.0f;
		float gf = g / 255.0f;
		float bf = b / 255.0f;
		float max = Math.max(rf, Math.max(gf, bf));
		float min = Math.min(rf, Math.min(gf, bf));
		float delta = max - min;
		float h = 0.0f;
		if (delta > 0.00001f) {
			if (max == rf) {
				h = 60.0f * (((gf - bf) / delta) % 6.0f);
			} else if (max == gf) {
				h = 60.0f * (((bf - rf) / delta) + 2.0f);
			} else {
				h = 60.0f * (((rf - gf) / delta) + 4.0f);
			}
			if (h < 0.0f) {
				h += 360.0f;
			}
		}
		float s = (max <= 0.00001f) ? 0.0f : (delta / max);
		float v = max;
		return new float[] { h, s, v };
	}
}
