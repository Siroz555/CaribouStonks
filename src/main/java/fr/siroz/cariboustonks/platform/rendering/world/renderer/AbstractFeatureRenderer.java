package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.oit.OitStage;
import org.joml.Matrix4fStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents an abstract base of Minecraft Renderer Feature for the custom Mod rendering
 */
public abstract class AbstractFeatureRenderer<Submit extends SubmitNode> implements FeatureRenderer<Submit> {
	private Group currentGroup;
	private final List<Group> groups = new ArrayList<>();

	protected abstract void buildGroup(FeatureFrameContext context, List<Submit> submits);

	@NonNull
	protected final VertexConsumer getVertexBuilder(RenderPipeline pipeline) {
		return currentGroup().getVertexBuilder(pipeline, TextureSetup.noTexture());
	}

	@NonNull
	protected final VertexConsumer getVertexBuilder(RenderPipeline pipeline, TextureSetup textureSetup) {
		return currentGroup().getVertexBuilder(pipeline, textureSetup);
	}

	@Override
	public final void prepareGroup(@NonNull FeatureFrameContext context, @NonNull List<Submit> submits, boolean strictlyOrdered) {
		currentGroup = new Group(context.stagedVertexBuffer(), !strictlyOrdered);
		buildGroup(context, submits);
		groups.add(this.currentGroup);
		currentGroup = null;
	}

	@Override
	public void executeGroup(@NonNull FeatureFrameContext context, @Nullable OitStage stage, @NonNull RenderPass renderPass, int groupIndex, @NonNull List<Submit> submits, boolean strictlyOrdered) {
		Group group = groups.get(groupIndex);

		applyViewOffsetZLayering();

		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy());

		RenderSystem.bindDefaultUniforms(renderPass);
		renderPass.setUniform("DynamicTransforms", dynamicTransforms);

		for (int i = 0; i < group.draws.size(); i++) {
			PreparedDraw draw = group.preparedDraws.get(i);
			StagedVertexBuffer.ExecuteInfo info = context.stagedVertexBuffer().getExecuteInfo(group.draws.get(i));

			if (info != null) {
				executeDraw(renderPass, draw, info);
			}
		}

		unapplyViewOffsetZLayering();
	}

	@Override
	public final void finishExecute(@NonNull FeatureFrameContext context) {
		groups.clear();
	}

	private Group currentGroup() {
		return Objects.requireNonNull(currentGroup, "Not preparing group");
	}

	private static void executeDraw(RenderPass renderPass, PreparedDraw draw, StagedVertexBuffer.ExecuteInfo info) {
		renderPass.setPipeline(RenderSystem.getCompiledPipeline(draw.pipeline()));

		if (draw.textureSetup().texure0() != null) {
			// Sampler0 is used for normal texture inputs in shaders
			renderPass.setUniform("Sampler0", draw.textureSetup().texure0(), draw.textureSetup().sampler0());
		}

		if (draw.textureSetup().texure1() != null) {
			// Sampler1 is used for alternate texture inputs in shaders
			renderPass.setUniform("Sampler1", draw.textureSetup().texure1(), draw.textureSetup().sampler1());
		}

		if (draw.textureSetup().texure2() != null) {
			// Sampler2 is used for lightmap texture inputs in shaders
			renderPass.setUniform("Sampler2", draw.textureSetup().texure2(), draw.textureSetup().sampler2());
		}

		renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
		renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());

		renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
	}

	private static void applyViewOffsetZLayering() {
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		RenderSystem.getProjectionType().applyLayeringTransform(modelViewStack, 1f);
	}

	private static void unapplyViewOffsetZLayering() {
		RenderSystem.getModelViewStack().popMatrix();
	}

	private static class Group {
		private final StagedVertexBuffer stagedBuffer;
		private final boolean canReorder;
		private final List<StagedVertexBuffer.Draw> draws = new ArrayList<>();
		private final List<PreparedDraw> preparedDraws = new ArrayList<>();
		private @Nullable RenderPipeline lastPipeline;
		private @Nullable TextureSetup lastTextureSetup;
		private StagedVertexBuffer.@Nullable Draw lastDraw;

		private Group(StagedVertexBuffer stagedBuffer, boolean canReorder) {
			this.stagedBuffer = stagedBuffer;
			this.canReorder = canReorder;
		}

		public @NonNull VertexConsumer getVertexBuilder(RenderPipeline pipeline, TextureSetup textureSetup) {
			if (lastDraw == null || pipeline != lastPipeline || textureSetup != lastTextureSetup || !canConsolidateConsecutiveGeometry(pipeline)) {
				lastDraw = getOrAddDraw(pipeline, textureSetup);
				lastPipeline = pipeline;
				lastTextureSetup = textureSetup;
			}

			return stagedBuffer.getVertexBuilder(lastDraw);
		}

		private StagedVertexBuffer.Draw getOrAddDraw(RenderPipeline pipeline, TextureSetup textureSetup) {
			PreparedDraw preparedPrimitiveDraw = new PreparedDraw(pipeline, textureSetup);

			int existingIndex = canReorder && canConsolidateConsecutiveGeometry(pipeline)
					? preparedDraws.indexOf(preparedPrimitiveDraw)
					: -1;
			if (existingIndex != -1) {
				return draws.get(existingIndex);
			}

			StagedVertexBuffer.Draw draw = stagedBuffer.appendDraw(
					Objects.requireNonNull(pipeline.getVertexFormatBinding(0)),
					pipeline.getPrimitiveTopology()
			);

			draws.add(draw);
			preparedDraws.add(preparedPrimitiveDraw);

			return draw;
		}

		private static boolean canConsolidateConsecutiveGeometry(RenderPipeline pipeline) {
			return !pipeline.getPrimitiveTopology().connectedPrimitives;
		}
	}

	private record PreparedDraw(RenderPipeline pipeline, TextureSetup textureSetup) {
	}
}
