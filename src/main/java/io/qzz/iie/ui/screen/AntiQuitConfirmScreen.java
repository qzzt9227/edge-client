package io.qzz.iie.ui.screen;

import io.qzz.iie.ui.render.UiPainter;
import io.qzz.iie.ui.theme.ClickGuiTheme;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * 防误退二次确认对话框屏幕。
 *
 * <p>提供居中卡片弹窗，支持“确定”、“取消”、右上角“✕”以及 Esc 静默关闭。</p>
 */
public final class AntiQuitConfirmScreen extends Screen {
	private static final int CARD_WIDTH = 280;
	private static final int CARD_HEIGHT = 130;

	private final Screen parent;
	private final Component titleComponent;
	private final Component messageComponent;
	private final Runnable onConfirm;

	public AntiQuitConfirmScreen(
		Screen parent,
		Component title,
		Component message,
		Runnable onConfirm
	) {
		super(Objects.requireNonNull(title, "title"));
		this.parent = parent;
		this.titleComponent = title;
		this.messageComponent = Objects.requireNonNull(message, "message");
		this.onConfirm = Objects.requireNonNull(onConfirm, "onConfirm");
	}

	@Override
	protected void init() {
		int cardX = (width - CARD_WIDTH) / 2;
		int cardY = (height - CARD_HEIGHT) / 2;

		// 确定按钮
		addRenderableWidget(
			Button.builder(
				Component.translatable("client.gui.anti_quit.confirm"),
				b -> onConfirm.run()
			).bounds(cardX + 20, cardY + CARD_HEIGHT - 36, 115, 22).build()
		);

		// 取消按钮
		addRenderableWidget(
			Button.builder(
				Component.translatable("client.gui.anti_quit.cancel"),
				b -> cancel()
			).bounds(cardX + CARD_WIDTH - 135, cardY + CARD_HEIGHT - 36, 115, 22).build()
		);

		// 右上角关闭 "✕" 按钮
		addRenderableWidget(
			Button.builder(
				Component.literal("✕"),
				b -> cancel()
			).bounds(cardX + CARD_WIDTH - 28, cardY + 6, 20, 20).build()
		);
	}

	public void cancel() {
		if (minecraft != null) {
			minecraft.setScreenAndShow(parent);
		}
	}

	@Override
	public void onClose() {
		cancel();
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// 不绘制原版默认背景，由 extractRenderState 绘制遮罩与卡片
	}

	@Override
	public void extractRenderState(
		GuiGraphicsExtractor graphics,
		int mouseX,
		int mouseY,
		float delta
	) {
		UiPainter painter = new UiPainter(graphics, font);

		// 半透明全屏遮罩
		painter.fill(0, 0, width, height, ClickGuiTheme.OVERLAY);

		// 居中对话框卡片
		int cardX = (width - CARD_WIDTH) / 2;
		int cardY = (height - CARD_HEIGHT) / 2;

		painter.roundedRectWithBorder(
			cardX,
			cardY,
			CARD_WIDTH,
			CARD_HEIGHT,
			10,
			1,
			ClickGuiTheme.CONTENT,
			ClickGuiTheme.OUTLINE
		);

		// 标题
		int titleW = painter.textWidth(titleComponent);
		painter.text(
			titleComponent,
			cardX + (CARD_WIDTH - titleW) / 2,
			cardY + 22,
			ClickGuiTheme.TEXT_PRIMARY
		);

		// 说明文本
		int msgW = painter.textWidth(messageComponent);
		painter.text(
			messageComponent,
			cardX + (CARD_WIDTH - msgW) / 2,
			cardY + 48,
			ClickGuiTheme.TEXT_SECONDARY
		);

		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}
}
