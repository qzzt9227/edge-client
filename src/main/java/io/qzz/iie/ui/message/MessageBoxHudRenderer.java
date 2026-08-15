package io.qzz.iie.ui.message;

import io.qzz.iie.ui.render.UiPainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.BooleanSupplier;

/**
 * Fabric HUD 适配器。模块和扩展不直接依赖该类。
 */
public final class MessageBoxHudRenderer {
	private static final int MARGIN = 12;
	private static final int BACKGROUND_RGB = 0x182028;
	private static final int ACCENT_RGB = 0xA9C8E7;

	private final MessageBoxManager messages;
	private final Supplier<MessageBoxAppearance> appearance;
	private final BooleanSupplier hidden;
	private final Map<String, FontDescription> fonts = new HashMap<>();

	public MessageBoxHudRenderer(
		MessageBoxManager messages,
		Supplier<MessageBoxAppearance> appearance
	) {
		this(messages, appearance, () -> false);
	}

	public MessageBoxHudRenderer(
		MessageBoxManager messages,
		Supplier<MessageBoxAppearance> appearance,
		BooleanSupplier hidden
	) {
		this.messages = Objects.requireNonNull(messages, "messages");
		this.appearance = Objects.requireNonNull(appearance, "appearance");
		this.hidden = Objects.requireNonNull(hidden, "hidden");
	}

	public void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (hidden.getAsBoolean()) {
			return;
		}
		List<MessageBoxSnapshot> snapshots = messages.snapshots();
		if (snapshots.isEmpty()) {
			return;
		}

		MessageBoxAppearance style = Objects.requireNonNull(
			appearance.get(),
			"message box appearance"
		);
		double fittedScale = fittedScale(graphics, style.boxScale());
		int boxWidth = Math.max(
			1,
			(int) Math.round(MessageBoxAppearance.BASE_WIDTH * fittedScale)
		);
		int boxHeight = Math.max(
			1,
			(int) Math.round(MessageBoxAppearance.BASE_HEIGHT * fittedScale)
		);
		int gap = Math.max(3, (int) Math.round(7 * fittedScale));
		int radius = Math.max(6, (int) Math.round(14 * fittedScale));
		int y = MARGIN;

		FontDescription font = fonts.computeIfAbsent(
			style.fontResourceId(),
			id -> new FontDescription.Resource(Identifier.parse(id))
		);
		UiPainter textPainter = new UiPainter(
			graphics,
			Minecraft.getInstance().font,
			style.textScale(),
			font
		);

		for (MessageBoxSnapshot snapshot : snapshots) {
			if (y + boxHeight > graphics.guiHeight() - MARGIN) {
				break;
			}
			int x = graphics.guiWidth() - MARGIN - boxWidth;
			int background = withAlpha(
				BACKGROUND_RGB,
				style.opacity() * snapshot.visibility()
			);
			int accent = withAlpha(ACCENT_RGB, snapshot.visibility());
			textPainter.roundedRect(x, y, boxWidth, boxHeight, radius, background);
			textPainter.roundedRect(
				x + Math.max(5, (int) Math.round(8 * fittedScale)),
				y + Math.max(5, (int) Math.round(8 * fittedScale)),
				Math.max(2, (int) Math.round(3 * fittedScale)),
				Math.max(1, boxHeight - Math.max(10, (int) Math.round(16 * fittedScale))),
				Math.max(1, (int) Math.round(2 * fittedScale)),
				accent
			);

			int textX = x + Math.max(15, (int) Math.round(22 * fittedScale));
			int availableWidth = Math.max(
				1,
				boxWidth - (textX - x) - Math.max(8, (int) Math.round(12 * fittedScale))
			);
			Component message = snapshot.message();
			String text = textPainter.trimToWidth(
				message.getString(),
				availableWidth
			);
			TextColor messageColor = message.getStyle().getColor();
			int resolvedTextColor = messageColor == null
				? style.textColor()
				: 0xFF000000 | messageColor.getValue() & 0x00FFFFFF;
			int textY = y + Math.max(0, (boxHeight - textPainter.lineHeight()) / 2);
			textPainter.text(
				Component.literal(text).withStyle(message.getStyle()),
				textX,
				textY,
				withAlpha(resolvedTextColor & 0x00FFFFFF, snapshot.visibility())
			);
			y += boxHeight + gap;
		}
	}

	private static double fittedScale(
		GuiGraphicsExtractor graphics,
		double requestedScale
	) {
		double widthScale = Math.max(
			0.1,
			(graphics.guiWidth() - MARGIN * 2.0) / MessageBoxAppearance.BASE_WIDTH
		);
		double heightScale = Math.max(
			0.1,
			(graphics.guiHeight() - MARGIN * 2.0) / MessageBoxAppearance.BASE_HEIGHT
		);
		return Math.min(requestedScale, Math.min(widthScale, heightScale));
	}

	private static int withAlpha(int rgb, double opacity) {
		int alpha = (int) Math.round(255.0 * Math.clamp(opacity, 0.0, 1.0));
		return alpha << 24 | rgb & 0x00FFFFFF;
	}
}
