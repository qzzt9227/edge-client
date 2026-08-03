package io.qzz.iie.module;

import java.util.Objects;

/**
 * 分类由模块类所在的 {@code module/impl/<category>/} 包结构派生。
 *
 * <p>分类 id 就是模块包路径中 {@code module.impl} 之后的第一段，排序按
 * id 字母序。GUI 侧栏据此渲染，不再读取模块显式声明的分类。</p>
 */
public record ModuleCategory(String id, String translationKey)
	implements Comparable<ModuleCategory> {
	public ModuleCategory {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(translationKey, "translationKey");
		if (!id.matches("[a-z0-9][a-z0-9_.-]*")) {
			throw new IllegalArgumentException("Invalid category ID: " + id);
		}
	}

	@Override
	public int compareTo(ModuleCategory other) {
		return id.compareTo(other.id);
	}
}
