package io.qzz.iie.ui.panel;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 管理与持久化 ClickGUI 各分类窗口的位置、折叠状态以及展开中的模块。
 *
 * <p>状态直接集成入客户端的 {@code JsonConfigService} 进行统一实时保存与读取。</p>
 */
public final class CategoryPositionManager {
	private static final Map<String, PanelState> STATES = new LinkedHashMap<>();
	private static Runnable saveCallback = () -> {};

	public record PanelState(int x, int y, boolean opened, Set<String> expandedModules) {
		public PanelState {
			expandedModules = expandedModules == null ? Set.of() : Set.copyOf(expandedModules);
		}

		public PanelState(int x, int y, boolean opened) {
			this(x, y, opened, Set.of());
		}
	}

	private CategoryPositionManager() {
	}

	public static Map<String, PanelState> getAllStates() {
		return Collections.unmodifiableMap(new LinkedHashMap<>(STATES));
	}

	public static void setAllStates(Map<String, PanelState> newStates) {
		STATES.clear();
		if (newStates != null) {
			STATES.putAll(newStates);
		}
	}

	public static Optional<PanelState> getState(String categoryId) {
		return Optional.ofNullable(STATES.get(categoryId));
	}

	public static void updateState(String categoryId, int x, int y, boolean opened) {
		PanelState existing = STATES.get(categoryId);
		Set<String> expanded = existing != null ? existing.expandedModules() : Set.of();
		STATES.put(categoryId, new PanelState(x, y, opened, expanded));
	}

	public static void updateState(
		String categoryId,
		int x,
		int y,
		boolean opened,
		Set<String> expandedModules
	) {
		STATES.put(categoryId, new PanelState(x, y, opened, expandedModules));
	}

	public static void setSaveCallback(Runnable callback) {
		saveCallback = callback != null ? callback : () -> {};
	}

	public static void save() {
		saveCallback.run();
	}

	public static void clear() {
		STATES.clear();
	}
}
