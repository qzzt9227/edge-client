package io.qzz.iie.module.impl.render.norender;

/**
 * 用于向 SpriteContents$AnimationState 附加精灵纹理 ID 的鸭子类型接口。
 */
public interface SpriteNameHolder {
	String edgeClient$getSpriteName();

	void edgeClient$setSpriteName(String name);
}
