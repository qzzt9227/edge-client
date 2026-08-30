package io.qzz.iie.module.impl.player.packetmine;

/**
 * 挖掘进度三维立体几何包围盒。
 */
public record PacketMineBox(
	double minX, double minY, double minZ,
	double maxX, double maxY, double maxZ
) {
}
