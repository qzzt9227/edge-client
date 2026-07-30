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

	public void roundedRect(int x, int y, int width, int height, int radius, int color) {
		if (width <= 0 || height <= 0) {
			return;
		}
		int safeRadius = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
		if (safeRadius == 0) {
			fill(x, y, width, height, color);
			return;
		}

		fill(x, y + safeRadius, width, height - safeRadius * 2, color);
		for (int row = 0; row < safeRadius; row++) {
			double verticalDistance = safeRadius - (row + 0.5);
			double circleExtent = Math.sqrt(Math.max(
				0.0,
				safeRadius * (double) safeRadius - verticalDistance * verticalDistance
			));
			double boundary = safeRadius - circleExtent;
			int fullInset = (int) Math.ceil(boundary);
			drawAntialiasedRow(
				x,
				y + row,
				width,
				fullInset,
				boundary,
				color
			);
			drawAntialiasedRow(
				x,
				y + height - row - 1,
				width,
				fullInset,
				boundary,
				color
			);
		}
	}

	private void drawAntialiasedRow(
		int x,
		int y,
		int width,
		int fullInset,
		double boundary,
		int color
	) {
		fill(x + fullInset, y, width - fullInset * 2, 1, color);
		int edgePixel = fullInset - 1;
		double coverage = fullInset - boundary;
		if (edgePixel >= 0 && coverage > 0.0) {
			int edgeColor = withAlphaCoverage(color, coverage);
			fill(x + edgePixel, y, 1, 1, edgeColor);
			int rightEdgeX = x + width - edgePixel - 1;
			if (rightEdgeX != x + edgePixel) {
				fill(rightEdgeX, y, 1, 1, edgeColor);
			}
		}
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
