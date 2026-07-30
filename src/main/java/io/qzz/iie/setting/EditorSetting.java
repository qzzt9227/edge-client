package io.qzz.iie.setting;

import java.util.Objects;

/**
 * 声明需要由统一设置编辑子页处理的复杂设置。
 *
 * <p>编辑器 ID 是稳定的 GUI 适配键；设置本身不依赖任何屏幕或渲染类型。</p>
 */
public abstract class EditorSetting<T> extends Setting<T> {
	private final String editorId;

	protected EditorSetting(
		String id,
		String translationKey,
		T defaultValue,
		String editorId
	) {
		super(id, translationKey, defaultValue);
		this.editorId = validateEditorId(editorId);
	}

	public final String editorId() {
		return editorId;
	}

	private static String validateEditorId(String value) {
		String checked = Objects.requireNonNull(value, "editorId");
		if (!checked.matches("[a-z0-9][a-z0-9_.-]*")) {
			throw new IllegalArgumentException("Invalid setting editor ID: " + checked);
		}
		return checked;
	}
}
