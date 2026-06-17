package fr.siroz.cariboustonks.platform.rendering.world;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.siroz.cariboustonks.events.RenderEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class CaribouWorldRenderer {
	private static final StagedVertexBuffer VERTEX_BUFFER = new StagedVertexBuffer(
			() -> "CaribouStonks WorldRenderer Vertex Buffer",
			RenderType.SMALL_BUFFER_SIZE
	);
	private static final List<Draw> DRAWS = new ArrayList<>();
	private static StagedVertexBuffer.@Nullable Draw lastDraw = null;
	private static @Nullable RenderPipeline lastPipeline = null;
	private static @Nullable TextureSetup lastTextureSetup = null;

	private static @Nullable RenderDispatcher renderDispatcher = null;

	private CaribouWorldRenderer() {
	}

	/**
	 * Init
	 */
	public static void bootstrap() {
		renderDispatcher = new RenderDispatcher();
		LevelRenderEvents.START_MAIN.register(CaribouWorldRenderer::begin);
		LevelRenderEvents.END_MAIN.register(CaribouWorldRenderer::executeDraws);
	}

	/**
	 * Returns the {@link BufferBuilder} for the given {@link RenderPipeline}
	 *
	 * @param pipeline the pipeline
	 * @return the VertexBuilder
	 * @see #getBuffer(RenderPipeline, TextureSetup)
	 */
	public static @NonNull VertexConsumer getBuffer(@NonNull RenderPipeline pipeline) {
		return getBuffer(pipeline, TextureSetup.noTexture());
	}

	/**
	 * Returns the {@link BufferBuilder} for the given {@link RenderPipeline} with {@link TextureSetup}
	 *
	 * @param pipeline     the pipeline
	 * @param textureSetup the textureSetup
	 * @return the VertexBuilder
	 * @see #getBuffer(RenderPipeline)
	 */
	public static @NonNull VertexConsumer getBuffer(RenderPipeline pipeline, TextureSetup textureSetup) {
		if (lastDraw == null || pipeline != lastPipeline || !textureSetup.equals(lastTextureSetup)) {
			lastDraw = VERTEX_BUFFER.appendDraw(
					Objects.requireNonNull(pipeline.getVertexFormatBinding(0)),
					pipeline.getPrimitiveTopology()
			);
			DRAWS.add(new Draw(lastDraw, pipeline, textureSetup));
		}

		return VERTEX_BUFFER.getVertexBuilder(Objects.requireNonNull(lastDraw));
	}

	/**
	 * >>> <b>MIXIN</b> <<<
	 *
	 * @param levelRenderState levelRenderState
	 * @param frustum          frustum
	 */
	public static void extract(LevelRenderState levelRenderState, Frustum frustum) {
		if (renderDispatcher == null) return;

		renderDispatcher.begin(levelRenderState, frustum);
		RenderEvents.WORLD_RENDER_EVENT.invoker().onWorldRender(renderDispatcher);
		renderDispatcher.end();
	}

	/**
	 * >>> <b>MIXIN</b> <<<
	 */
	public static void close() {
		VERTEX_BUFFER.close();
	}

	private static void begin(LevelTerrainRenderContext context) {
		lastDraw = null;
		lastPipeline = null;
		lastTextureSetup = null;
	}

	private static void executeDraws(LevelRenderContext context) {
		if (renderDispatcher == null) return;
		// Emit all states to Vertex
		renderDispatcher.flush(context.levelState().cameraRenderState);
		// Upload Vertex Buffer
		VERTEX_BUFFER.upload();
		// Dispatch
		dispatchDraws();
		// Cleanup
		VERTEX_BUFFER.endDraw();
		VERTEX_BUFFER.endFrame();
		DRAWS.clear();
	}

	private static void dispatchDraws() {
		applyViewOffsetZLayering();

		RenderTarget mainRenderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		try (RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(
						() -> "CaribouStonks World Rendering",
						Objects.requireNonNull(mainRenderTarget.getColorTextureView()),
						Optional.empty(),
						mainRenderTarget.useDepth ? mainRenderTarget.getDepthTextureView() : null,
						OptionalDouble.empty()
				)
		) {
			RenderSystem.bindDefaultUniforms(renderPass);

			for (Draw draw : DRAWS) {
				draw(draw, renderPass);
			}
		}

		unapplyViewOffsetZLayering();
	}

	private static void draw(@NonNull Draw draw, @NonNull RenderPass renderPass) {
		StagedVertexBuffer.ExecuteInfo executeInfo = VERTEX_BUFFER.getExecuteInfo(draw.draw());
		if (executeInfo == null) return;

		renderPass.setPipeline(draw.pipeline());
		renderPass.setUniform("DynamicTransforms", setupDynamicTransforms());

		if (draw.textureSetup().texure0() != null) {
			// Sampler0 is used for normal texture inputs in shaders
			renderPass.bindTexture("Sampler0", draw.textureSetup().texure0(), draw.textureSetup().sampler0());
		}

		if (draw.textureSetup().texure1() != null) {
			// Sampler1 is used for alternate texture inputs in shaders
			renderPass.bindTexture("Sampler1", draw.textureSetup().texure1(), draw.textureSetup().sampler1());
		}

		if (draw.textureSetup().texure2() != null) {
			// Sampler2 is used for lightmap texture inputs in shaders
			renderPass.bindTexture("Sampler2", draw.textureSetup().texure2(), draw.textureSetup().sampler2());
		}

		renderPass.setVertexBuffer(0, executeInfo.vertexBuffer().slice());
		renderPass.setIndexBuffer(executeInfo.indexBuffer(), executeInfo.indexType());
		renderPass.drawIndexed(executeInfo.indexCount(), 1, executeInfo.firstIndex(), executeInfo.baseVertex(), 0);
	}

	private static @NonNull GpuBufferSlice setupDynamicTransforms() {
		return RenderSystem.getDynamicUniforms().writeTransform(
				RenderSystem.getModelViewMatrixCopy(),
				new Vector4f(1f, 1f, 1f, 1f) // w: alpha
		);
	}

	private static void applyViewOffsetZLayering() {
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		RenderSystem.getProjectionType().applyLayeringTransform(modelViewStack, 1f);
	}

	private static void unapplyViewOffsetZLayering() {
		RenderSystem.getModelViewStack().popMatrix();
	}

	private record Draw(
			StagedVertexBuffer.Draw draw,
			RenderPipeline pipeline,
			TextureSetup textureSetup
	) {
	}
}
