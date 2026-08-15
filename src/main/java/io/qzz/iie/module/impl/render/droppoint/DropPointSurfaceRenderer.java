package io.qzz.iie.module.impl.render.droppoint;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4fc;

/** Reusable world-rendering primitive for one translucent block top surface. */
public final class DropPointSurfaceRenderer {
	private DropPointSurfaceRenderer() {
	}

	public static void renderTopFace(
		Matrix4fc positionMatrix,
		VertexConsumer buffer,
		int blockX,
		int blockY,
		int blockZ,
		DropPointColor color
	) {
		float minX = blockX;
		float maxX = blockX + 1.0F;
		float minZ = blockZ;
		float maxZ = blockZ + 1.0F;
		float y = blockY + 1.002F;
		buffer.addVertex(positionMatrix, minX, y, maxZ).setColor(
			color.redFloat(), color.greenFloat(), color.blueFloat(), color.opacityFloat()
		);
		buffer.addVertex(positionMatrix, maxX, y, maxZ).setColor(
			color.redFloat(), color.greenFloat(), color.blueFloat(), color.opacityFloat()
		);
		buffer.addVertex(positionMatrix, maxX, y, minZ).setColor(
			color.redFloat(), color.greenFloat(), color.blueFloat(), color.opacityFloat()
		);
		buffer.addVertex(positionMatrix, minX, y, minZ).setColor(
			color.redFloat(), color.greenFloat(), color.blueFloat(), color.opacityFloat()
		);
	}
}
