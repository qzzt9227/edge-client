package io.qzz.iie.api.message;

import net.minecraft.network.chat.Component;

import java.time.Duration;

/**
 * 模块与扩展使用的消息提示框 API。
 *
 * <p>调用方只提交消息内容和可选显示时长；尺寸、字体、字号、透明度、颜色、
 * 动画和 HUD 绘制均由客户端统一处理。</p>
 */
public interface MessageBoxApi {
	Duration DEFAULT_DURATION = Duration.ofSeconds(3);

	void show(Component message, Duration duration);

	default void show(Component message) {
		show(message, DEFAULT_DURATION);
	}

	static MessageBoxApi noop() {
		return (message, duration) -> {
		};
	}
}
