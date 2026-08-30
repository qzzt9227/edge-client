package io.qzz.iie.setting;

import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * 表示双精度浮点数区间范围的不可变值对象。
 *
 * @param min 区间下限
 * @param max 区间上限
 */
public record DoubleRange(double min, double max) {
	public DoubleRange {
		if (!Double.isFinite(min) || !Double.isFinite(max)) {
			throw new IllegalArgumentException("Range values must be finite numbers");
		}
		if (min > max) {
			double temp = min;
			min = max;
			max = temp;
		}
	}

	/**
	 * 在 [min, max] 范围内均匀采样一个随机值。
	 */
	public double randomValue() {
		return sample(ThreadLocalRandom.current());
	}

	/**
	 * 使用指定的随机数生成器在 [min, max] 范围内采样。
	 */
	public double sample(RandomGenerator random) {
		if (min >= max) {
			return min;
		}
		return min + random.nextDouble() * (max - min);
	}
}
