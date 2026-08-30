package io.qzz.iie.module.impl.player.packetmine;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Objects;
import java.util.Optional;

/**
 * 包挖掘方块进度三维立体渲染器：通过无深度冲突图层渲染膨胀盒、上升柱与脉冲发光线框。
 */
public final class PacketMineWorldRenderer {
	private static final RenderPipeline PIPELINE = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(Identifier.fromNamespaceAndPath("client", "pipeline/packet_mine_box"))
			.build()
	);
	private static final Vector4f COLOR_MODULATOR = new Vector4f(1F, 1F, 1F, 1F);
	private static final Vector3f MODEL_OFFSET = new Vector3f();
	private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
	private static final StagedVertexBuffer STAGED_BUFFER = new StagedVertexBuffer(
		() -> "Packet mine box buffer",
		RenderType.SMALL_BUFFER_SIZE
	);
	private static PacketMineModule module;
	private static PacketMineVisualState.Snapshot extractedSnapshot;
	private static boolean installed;

	private PacketMineWorldRenderer() {
	}

	public static void install(PacketMineModule installedModule) {
		module = Objects.requireNonNull(installedModule, "installedModule");
		if (!installed) {
			installed = true;
			LevelExtractionEvents.END_EXTRACTION.register(context -> extractedSnapshot = PacketMineVisualState.snapshot());
			LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(PacketMineWorldRenderer::drawBoxes);
		}
	}

	private static void drawBoxes(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
		PacketMineVisualState.Snapshot snapshot = extractedSnapshot;
		if (snapshot == null || !snapshot.miningActive() || snapshot.targetPos() == null) {
			return;
		}

		BlockPos pos = snapshot.targetPos();
		float progress = snapshot.progress();
		PacketMineRenderStyle style = snapshot.renderStyle();
		int fillColor = snapshot.fillColor();
		int lineColor = snapshot.lineColor();

		PacketMineBox box;
		switch (style) {
			case EXPAND -> {
				box = PacketMinePolicy.calculateExpandBox(pos, progress);
			}
			case RISE -> {
				box = PacketMinePolicy.calculateRiseBox(pos, progress);
			}
			case PULSE_FRAME -> {
				fillColor = PacketMinePolicy.calculatePulseColor(fillColor, progress, System.currentTimeMillis());
				lineColor = PacketMinePolicy.calculatePulseColor(lineColor, progress, System.currentTimeMillis());
				box = PacketMinePolicy.calculateFullBox(pos);
			}
			default -> box = PacketMinePolicy.calculateFullBox(pos);
		}

		RenderPipeline pipeline = PIPELINE;
		VertexFormat formatBinding = pipeline.getVertexFormatBinding(0);
		if (formatBinding == null) {
			return;
		}
		PrimitiveTopology primitive = pipeline.getPrimitiveTopology();
		StagedVertexBuffer.Draw draw = STAGED_BUFFER.appendDraw(
			formatBinding,
			primitive,
			primitive == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null
		);

		PoseStack matrices = context.poseStack();
		Vec3 camera = context.levelState().cameraRenderState.pos;
		matrices.pushPose();
		try {
			matrices.translate(-camera.x, -camera.y, -camera.z);
			VertexConsumer builder = STAGED_BUFFER.getVertexBuilder(draw);
			renderBoxWithOutline(matrices.last().pose(), builder, box, fillColor, lineColor);
		} finally {
			matrices.popPose();
		}

		STAGED_BUFFER.upload();
		StagedVertexBuffer.ExecuteInfo info = STAGED_BUFFER.getExecuteInfo(draw);
		if (info != null) {
			draw(Minecraft.getInstance(), info, pipeline);
		}
		STAGED_BUFFER.endFrame();
	}

	private static void renderBoxWithOutline(
		Matrix4fc matrix,
		VertexConsumer buffer,
		PacketMineBox box,
		int fillColor,
		int lineColor
	) {
		// 1. 渲染半透明立方体填充面
		if (((fillColor >> 24) & 0xFF) > 0) {
			renderCubeFaces(matrix, buffer, box, fillColor);
		}

		// 2. 渲染立体高亮边框线（12条边）
		if (((lineColor >> 24) & 0xFF) > 0) {
			renderWireframeEdges(matrix, buffer, box, lineColor, 0.012f);
		}
	}

	private static void renderCubeFaces(
		Matrix4fc positionMatrix,
		VertexConsumer buffer,
		PacketMineBox box,
		int argb
	) {
		float a = ((argb >> 24) & 0xFF) / 255.0f;
		float r = ((argb >> 16) & 0xFF) / 255.0f;
		float g = ((argb >> 8) & 0xFF) / 255.0f;
		float b = (argb & 0xFF) / 255.0f;

		float minX = (float) box.minX();
		float minY = (float) box.minY();
		float minZ = (float) box.minZ();
		float maxX = (float) box.maxX();
		float maxY = (float) box.maxY();
		float maxZ = (float) box.maxZ();

		// 顶面 (Top)
		buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(r, g, b, a);

		// 底面 (Bottom)
		buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(r, g, b, a);

		// 北面 (North)
		buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(r, g, b, a);

		// 南面 (South)
		buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(r, g, b, a);

		// 西面 (West)
		buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(r, g, b, a);

		// 东面 (East)
		buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(r, g, b, a);
		buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(r, g, b, a);
	}

	private static void renderWireframeEdges(
		Matrix4fc matrix,
		VertexConsumer buffer,
		PacketMineBox box,
		int argb,
		float thickness
	) {
		float t = thickness;
		float minX = (float) box.minX();
		float minY = (float) box.minY();
		float minZ = (float) box.minZ();
		float maxX = (float) box.maxX();
		float maxY = (float) box.maxY();
		float maxZ = (float) box.maxZ();

		// 顶部 4 条边
		renderCubeFaces(matrix, buffer, new PacketMineBox(minX - t, maxY - t, minZ - t, maxX + t, maxY + t, minZ + t), argb);
		renderCubeFaces(matrix, buffer, new PacketMineBox(minX - t, maxY - t, maxZ - t, maxX + t, maxY + t, maxZ + t), argb);
		renderCubeFaces(matrix, buffer, new PacketMineBox(minX - t, maxY - t, minZ - t, minX + t, maxY + t, maxZ + t), argb);
		renderCubeFaces(matrix, buffer, new PacketMineBox(maxX - t, maxY - t, minZ - t, maxX + t, maxY + t, maxZ + t), argb);

		// 底部 4 条边
		renderCubeFaces(matrix, buffer, new PacketMineBox(minX - t, minY - t, minZ - t, maxX + t, minY + t, minZ + t), argb);
		renderCubeFaces(matrix, buffer, new PacketMineBox(minX - t, minY - t, maxZ - t, maxX + t, minY + t, maxZ + t), argb);
		renderCubeFaces(matrix, buffer, new PacketMineBox(minX - t, minY - t, minZ - t, minX + t, minY + t, maxZ + t), argb);
		renderCubeFaces(matrix, buffer, new PacketMineBox(maxX - t, minY - t, minZ - t, maxX + t, minY + t, maxZ + t), argb);

		// 垂直 4 条边
		renderCubeFaces(matrix, buffer, new PacketMineBox(minX - t, minY - t, minZ - t, minX + t, maxY + t, minZ + t), argb);
		renderCubeFaces(matrix, buffer, new PacketMineBox(maxX - t, minY - t, minZ - t, maxX + t, maxY + t, minZ + t), argb);
		renderCubeFaces(matrix, buffer, new PacketMineBox(minX - t, minY - t, maxZ - t, minX + t, maxY + t, maxZ + t), argb);
		renderCubeFaces(matrix, buffer, new PacketMineBox(maxX - t, minY - t, maxZ - t, maxX + t, maxY + t, maxZ + t), argb);
	}

	private static void draw(Minecraft client, StagedVertexBuffer.ExecuteInfo info, RenderPipeline pipeline) {
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
			RenderSystem.getModelViewMatrixCopy(),
			COLOR_MODULATOR,
			MODEL_OFFSET,
			TEXTURE_MATRIX
		);
		RenderTarget target = client.gameRenderer.mainRenderTarget();
		GpuTextureView colorTexture = target.getColorTextureView();
		if (colorTexture == null) {
			return;
		}
		// 使用不绑定深度缓冲区的 RenderPass，彻底消除与原版地形方块的 Z-Fighting 闪烁与遮挡问题
		try (RenderPass renderPass = RenderSystem.getDevice()
			.createCommandEncoder()
			.createRenderPass(
				() -> "Packet mine box rendering",
				colorTexture,
				Optional.empty()
			)) {
			renderPass.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
			renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
			renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
			renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
		}
	}
}
