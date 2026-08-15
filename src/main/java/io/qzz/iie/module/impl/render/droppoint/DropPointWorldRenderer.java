package io.qzz.iie.module.impl.render.droppoint;

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
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Objects;

/** Fabric 26.2 extraction/drawing adapter for the single landing surface. */
public final class DropPointWorldRenderer {
	private static final RenderPipeline PIPELINE = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(Identifier.fromNamespaceAndPath("client", "pipeline/drop_point_surface"))
			.build()
	);
	private static final Vector4f COLOR_MODULATOR = new Vector4f(1F, 1F, 1F, 1F);
	private static final Vector3f MODEL_OFFSET = new Vector3f();
	private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
	private static final StagedVertexBuffer STAGED_BUFFER = new StagedVertexBuffer(
		() -> "Drop point surface buffer",
		RenderType.SMALL_BUFFER_SIZE
	);
	private static DropPointModule module;
	private static DropPointRenderState extractedState;
	private static boolean installed;

	private DropPointWorldRenderer() {
	}

	public static void install(DropPointModule installedModule) {
		module = Objects.requireNonNull(installedModule, "installedModule");
		if (!installed) {
			installed = true;
			LevelExtractionEvents.END_EXTRACTION.register(context -> extractedState = module.renderState());
			LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(DropPointWorldRenderer::drawSurface);
		}
	}

	private static void drawSurface(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
		DropPointRenderState state = extractedState;
		if (state == null) {
			return;
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
			DropPointSurfaceRenderer.renderTopFace(
				matrices.last().pose(),
				builder,
				state.x(),
				state.y(),
				state.z(),
				state.color()
			);
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
		try (RenderPass renderPass = RenderSystem.getDevice()
			.createCommandEncoder()
			.createRenderPass(
				() -> "Drop point surface rendering",
				colorTexture,
				Optional.empty(),
				target.getDepthTextureView(),
				OptionalDouble.empty()
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
