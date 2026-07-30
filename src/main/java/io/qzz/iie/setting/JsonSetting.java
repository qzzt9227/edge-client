package io.qzz.iie.setting;

import com.google.gson.JsonElement;

/**
 * 复杂设置值的通用 JSON 编解码契约。
 *
 * <p>配置服务仍统一负责文件生命周期；实现类只描述单个值的结构。</p>
 */
public interface JsonSetting {
	JsonElement encodeJson();

	void decodeJson(JsonElement value);
}
