package io.qzz.iie.ui.message;

import io.qzz.iie.api.message.MessageBoxApi;
import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleManager;

import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 将运行期间真正发生的模块开关变化转发到共享消息提示框 API。
 */
public final class ModuleStateMessageNotifier {
	private ModuleStateMessageNotifier() {
	}

	public static Runnable attach(
		ModuleManager moduleManager,
		MessageBoxApi messages
	) {
		ModuleManager manager = Objects.requireNonNull(moduleManager, "moduleManager");
		MessageBoxApi messageApi = Objects.requireNonNull(messages, "messages");
		Map<ModuleId, Boolean> previousStates = new HashMap<>();
		for (Module module : manager.modules()) {
			previousStates.put(module.id(), module.isEnabled());
		}

		return manager.addChangeListener(() -> {
			for (Module module : manager.modules()) {
				Boolean previous = previousStates.put(module.id(), module.isEnabled());
				if (previous == null || previous == module.isEnabled()) {
					continue;
				}
				String messageKey = module.isEnabled()
					? "client.message.module.enabled"
					: "client.message.module.disabled";
				messageApi.show(Component.translatable(
					messageKey,
					Component.translatable(module.metadata().nameTranslationKey())
				));
			}
		});
	}
}
