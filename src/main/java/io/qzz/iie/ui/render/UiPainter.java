package io.qzz.iie.ui.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;

import java.util.Objects;

public final class UiPainter {
	private final GuiGraphicsExtractor graphics;
	private final Font font;
	private final double textScale;
	private final FontDescription fontDescription;

	public UiPainter(GuiGraphicsExtractor graphics, Font font) {
		this(graphics, font, 1.0, null);
	}

	public UiPainter(GuiGraphicsExtractor graphics, Font font, double textScale) {
		this(graphics, font, textScale, null);
	}

	public UiPainter(
		GuiGraphicsExtractor graphics,
		Font font,
		double textScale,
		FontDescription fontDescription
	) {
		this.graphics = Objects.requireNonNull(graphics, "graphics");
		this.font = Objects.requireNonNull(font, "font");
		if (!Double.isFinite(textScale) || textScale <= 0.0) {
			throw new IllegalArgumentException("textScale must be finite and positive");
		}
		this.textScale = textScale;
		this.fontDescription = fontDescription;
	}

	public void fill(int x, int y, int width, int height, int color) {
		if (width <= 0 || height <= 0) {
			return;
		}
		graphics.fill(x, y, x + width, y + height, color);
	}

	public void renderItem(net.minecraft.world.item.ItemStack stack, int x, int y) {
		if (stack == null || stack.isEmpty()) {
			return;
		}
		graphics.item(stack, x, y);
	}

	public void renderEffectIcon(net.minecraft.resources.Identifier effectId, int x, int y, int width, int height) {
		if (effectId == null) {
			return;
		}
		net.minecraft.resources.Identifier spriteId = net.minecraft.resources.Identifier.fromNamespaceAndPath(
			effectId.getNamespace(),
			"mob_effect/" + effectId.getPath()
		);
		graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, spriteId, x, y, width, height);
	}

	public void renderSprite(net.minecraft.resources.Identifier spriteId, int x, int y, int width, int height) {
		if (spriteId == null) {
			return;
		}
		graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, spriteId, x, y, width, height);
	}

	public void roundedRect(int x, int y, int width, int height, int radius, int color) {
		if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
			return;
		}
		int safeRadius = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
		if (safeRadius == 0) {
			fill(x, y, width, height, color);
			return;
		}

		int middleHeight = height - safeRadius * 2;
		if (middleHeight > 0) {
			fill(x, y + safeRadius, width, middleHeight, color);
		}

		for (int py = 0; py < safeRadius; py++) {
			int topY = y + py;
			int bottomY = y + height - 1 - py;
			int solidMinPx = safeRadius;

			for (int px = 0; px < safeRadius; px++) {
				double coverage = calculateCornerCoverage(px, py, safeRadius);
				if (coverage <= 0.0) {
					continue;
				}
				if (coverage >= 1.0) {
					solidMinPx = px;
					break;
				}
				int edgeColor = withAlphaCoverage(color, coverage);
				fill(x + px, topY, 1, 1, edgeColor);
				int rightX = x + width - 1 - px;
				if (rightX != x + px) {
					fill(rightX, topY, 1, 1, edgeColor);
				}
				if (bottomY != topY) {
					fill(x + px, bottomY, 1, 1, edgeColor);
					if (rightX != x + px) {
						fill(rightX, bottomY, 1, 1, edgeColor);
					}
				}
			}

			int solidWidth = width - solidMinPx * 2;
			if (solidWidth > 0) {
				fill(x + solidMinPx, topY, solidWidth, 1, color);
				if (bottomY != topY) {
					fill(x + solidMinPx, bottomY, solidWidth, 1, color);
				}
			}
		}
	}

	private static double calculateCornerCoverage(int px, int py, int radius) {
		double r2 = (double) radius * radius;
		int insideSamples = 0;

		for (int subY = 0; subY < 4; subY++) {
			double sy = py + (subY + 0.5) * 0.25;
			double dy = radius - sy;

			for (int subX = 0; subX < 4; subX++) {
				double sx = px + (subX + 0.5) * 0.25;
				double dx = radius - sx;

				if (dx <= 0.0 || dy <= 0.0 || (dx * dx + dy * dy <= r2)) {
					insideSamples++;
				}
			}
		}

		return insideSamples / 16.0;
	}

	/**
	 * 绘制带平滑圆角边框的控件，避免在圆角底色上叠加方形描边。
	 */
	public void roundedRectWithBorder(
		int x,
		int y,
		int width,
		int height,
		int radius,
		int borderWidth,
		int fillColor,
		int borderColor
	) {
		int safeBorder = Math.max(0, Math.min(borderWidth, Math.min(width, height) / 2));
		if (safeBorder == 0) {
			roundedRect(x, y, width, height, radius, fillColor);
			return;
		}
		roundedRect(x, y, width, height, radius, borderColor);
		roundedRect(
			x + safeBorder,
			y + safeBorder,
			width - safeBorder * 2,
			height - safeBorder * 2,
			Math.max(0, radius - safeBorder),
			fillColor
		);
	}

	public void outline(int x, int y, int width, int height, int color) {
		graphics.outline(x, y, width, height, color);
	}

	private static int withAlphaCoverage(int color, double coverage) {
		int alpha = color >>> 24;
		int coveredAlpha = (int) Math.round(alpha * Math.clamp(coverage, 0.0, 1.0));
		return (coveredAlpha << 24) | (color & 0x00FFFFFF);
	}

	public void text(Component text, int x, int y, int color) {
		drawScaledText(styled(Objects.requireNonNull(text, "text")), x, y, color);
	}

	public void text(String text, int x, int y, int color) {
		Objects.requireNonNull(text, "text");
		if (fontDescription != null) {
			drawScaledText(styled(Component.literal(text)), x, y, color);
			return;
		}
		if (textScale == 1.0) {
			graphics.text(font, text, x, y, color, false);
			return;
		}
		withTranslation(x, y, () -> {
			graphics.pose().pushMatrix();
			try {
				graphics.pose().scale((float) textScale, (float) textScale);
				graphics.text(font, text, 0, 0, color, false);
			} finally {
				graphics.pose().popMatrix();
			}
		});
	}

	public int textWidth(Component text) {
		return scaled(font.width(styled(text)));
	}

	public int textWidth(String text) {
		return scaled(fontDescription == null
			? font.width(text)
			: font.width(styled(Component.literal(text))));
	}

	public int lineHeight() {
		return scaled(font.lineHeight);
	}

	public String trimToWidth(String text, int maximumWidth) {
		int unscaledMaximum = Math.max(0, (int) Math.floor(maximumWidth / textScale));
		if (unscaledTextWidth(text) <= unscaledMaximum) {
			return text;
		}
		String ellipsis = "...";
		int contentWidth = Math.max(0, unscaledMaximum - unscaledTextWidth(ellipsis));
		if (fontDescription == null) {
			return font.plainSubstrByWidth(text, contentWidth) + ellipsis;
		}

		int codePoints = text.codePointCount(0, text.length());
		int low = 0;
		int high = codePoints;
		while (low < high) {
			int middle = (low + high + 1) >>> 1;
			int end = text.offsetByCodePoints(0, middle);
			if (unscaledTextWidth(text.substring(0, end)) <= contentWidth) {
				low = middle;
			} else {
				high = middle - 1;
			}
		}
		return text.substring(0, text.offsetByCodePoints(0, low)) + ellipsis;
	}

	public static int calculateMarqueeOffset(
		int textWidth,
		int availableWidth,
		boolean hovered,
		long time
	) {
		if (!hovered || textWidth <= availableWidth) {
			return 0;
		}
		int overflow = textWidth - availableWidth;
		long cycle = 2000L + overflow * 40L;
		long phase = time % cycle;
		long pauseStart = 600L;
		long pauseEnd = 600L;
		long scrollDuration = Math.max(1L, cycle - pauseStart - pauseEnd);
		if (phase < pauseStart) {
			return 0;
		} else if (phase < pauseStart + scrollDuration) {
			double progress = (double) (phase - pauseStart) / (double) scrollDuration;
			return (int) Math.round(overflow * progress);
		} else {
			return overflow;
		}
	}

	public void marqueeText(
		String text,
		int x,
		int y,
		int availableWidth,
		int color,
		boolean hovered,
		long time
	) {
		int width = textWidth(text);
		if (width <= availableWidth) {
			text(text, x, y, color);
			return;
		}

		int scrollOffset = calculateMarqueeOffset(width, availableWidth, hovered, time);

		enableScissor(x, y - 2, x + availableWidth, y + lineHeight() + 2);
		try {
			text(text, x - scrollOffset, y, color);
		} finally {
			disableScissor();
		}
	}

	public void marqueeTwoPartText(
		String part1,
		int color1,
		String part2,
		int color2,
		int x,
		int y,
		int availableWidth,
		boolean hovered,
		long time
	) {
		int w1 = textWidth(part1);
		int w2 = textWidth(part2);
		int totalWidth = w1 + w2;

		if (totalWidth <= availableWidth) {
			text(part1, x, y, color1);
			text(part2, x + w1, y, color2);
			return;
		}

		int scrollOffset = calculateMarqueeOffset(totalWidth, availableWidth, hovered, time);

		enableScissor(x, y - 2, x + availableWidth, y + lineHeight() + 2);
		try {
			int drawX = x - scrollOffset;
			text(part1, drawX, y, color1);
			text(part2, drawX + w1, y, color2);
		} finally {
			disableScissor();
		}
	}

	public void withTranslation(float x, float y, Runnable renderAction) {
		Objects.requireNonNull(renderAction, "renderAction");
		graphics.pose().pushMatrix();
		try {
			graphics.pose().translate(x, y);
			renderAction.run();
		} finally {
			graphics.pose().popMatrix();
		}
	}

	public void enableScissor(int left, int top, int right, int bottom) {
		graphics.enableScissor(left, top, right, bottom);
	}

	public void disableScissor() {
		graphics.disableScissor();
	}

	private void drawScaledText(Component text, int x, int y, int color) {
		if (textScale == 1.0) {
			graphics.text(font, text, x, y, color, false);
			return;
		}
		withTranslation(x, y, () -> {
			graphics.pose().pushMatrix();
			try {
				graphics.pose().scale((float) textScale, (float) textScale);
				graphics.text(font, text, 0, 0, color, false);
			} finally {
				graphics.pose().popMatrix();
			}
		});
	}

	private int scaled(int value) {
		return (int) Math.round(value * textScale);
	}

	private int unscaledTextWidth(String text) {
		return fontDescription == null
			? font.width(text)
			: font.width(styled(Component.literal(text)));
	}

	private Component styled(Component text) {
		if (fontDescription == null) {
			return text;
		}
		return text.copy().withStyle(style -> style.withFont(fontDescription));
	}
}
