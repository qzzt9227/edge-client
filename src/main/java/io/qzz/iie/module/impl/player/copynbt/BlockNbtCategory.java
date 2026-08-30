package io.qzz.iie.module.impl.player.copynbt;

/**
 * 支持携带 NBT 的方块分类。
 */
public enum BlockNbtCategory {
	/**
	 * 特殊与功能方块：信标、刷怪笼、蜂窝/蜂箱、重生锚、磁石、潮涌核心等。
	 */
	SPECIAL,

	/**
	 * 文本与音画装饰：告示牌类、玩家头颅、唱片机、音符盒、旗帜、床等。
	 */
	DECORATIVE,

	/**
	 * 容器与存储：箱子、陷阱箱、末影箱、潜影盒、木桶、漏斗、饰罐、雕纹书架等。
	 */
	CONTAINERS,

	/**
	 * 熔炼与加工：熔炉、高炉、烟熏炉、酿造台、营火等。
	 */
	PROCESSING,

	/**
	 * 红石与机械：发射器、投掷器、活塞、自动合成器、红石比较器、传感器等。
	 */
	REDSTONE,

	/**
	 * 高级与指令：命令方块、结构方块、拼图方块等。
	 */
	ADVANCED,

	/**
	 * 其他方块实体。
	 */
	OTHER
}
