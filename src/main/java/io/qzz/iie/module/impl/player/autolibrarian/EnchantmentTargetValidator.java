package io.qzz.iie.module.impl.player.autolibrarian;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * 使用当前注册表校验目标并把等级收敛到真实最大等级。
 */
public final class EnchantmentTargetValidator {
	private EnchantmentTargetValidator() {
	}

	public static Result validateAndClamp(
		List<EnchantmentTarget> targets,
		ToIntFunction<String> maximumLevel
	) {
		ArrayList<String> invalidIds = new ArrayList<>();
		ArrayList<EnchantmentTarget> normalized = new ArrayList<>();
		for (EnchantmentTarget target : targets) {
			int maximum = maximumLevel.applyAsInt(target.enchantmentId());
			if (maximum < 1) {
				invalidIds.add(target.enchantmentId());
				normalized.add(target);
				continue;
			}
			normalized.add(target.withLevel(Math.clamp(target.level(), 1, maximum)));
		}
		List<EnchantmentTarget> result = List.copyOf(normalized);
		return new Result(!result.equals(targets), result, List.copyOf(invalidIds));
	}

	public record Result(
		boolean changed,
		List<EnchantmentTarget> targets,
		List<String> invalidIds
	) {
		public boolean valid() {
			return invalidIds.isEmpty();
		}
	}
}
