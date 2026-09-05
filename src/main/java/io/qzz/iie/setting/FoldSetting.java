package io.qzz.iie.setting;

/**
 * 可折叠设置分组。
 *
 * <p>用于在 GUI 中组织层级设置结构。值为布尔值（true 为展开，false 为折叠）。
 * 模块业务逻辑不依赖 UI，子设置通过 {@link #visibleWhen(java.util.function.BooleanSupplier)}
 * 关联父折叠项的开启状态，实现级联折叠/展开。</p>
 */
public final class FoldSetting extends BooleanSetting {
	public FoldSetting(String id, String translationKey, boolean defaultExpanded) {
		super(id, translationKey, defaultExpanded);
	}
}
