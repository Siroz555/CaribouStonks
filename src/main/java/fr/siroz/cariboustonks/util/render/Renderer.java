package fr.siroz.cariboustonks.util.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

/**
 * Renderer class provides better control and improved performance for rendering in the world
 * when updating to Minecraft versions 1.21.6-1.21.8.
 * <p>
 * This class is derived from Skyblocker's Renderer class, with a few adjustments.
 * The code itself is the same, as is the Javadoc.
 * The adjustments include better organization, a singleton, and visual code adjustments.
 * <p>
 * Credits to the Skyblocker Team (<a href="https://github.com/SkyblockerMod/Skyblocker">GitHub Skyblocker</a>).
 * In particular, AzureAaron for his writing and helps fix the rendering of circles that intersect with each other.
 *
 * @author AzureAaron
 * @author Skyblocker Team
 */
@ApiStatus.Internal
public final class Renderer {

	private static Renderer instance;

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final BufferAllocator GENERAL_ALLOCATOR = new BufferAllocator(RenderLayer.DEFAULT_BUFFER_SIZE);
	private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
	private static final float DEFAULT_LINE_WIDTH = 0f;

	private final List<RenderPipeline> excludedFromBatching;
	private final Int2ObjectMap<BufferAllocator> allocators;
	private final Int2ObjectMap<BatchedDraw> batchedDraws;
	private final Map<VertexFormat, MappableRingBuffer> vertexBuffers;
	private final List<PreparedDraw> preparedDraws;
	private final List<Draw> draws;
	private @Nullable BatchedDraw lastUnbatchedDraw;

	private Renderer() {
		this.excludedFromBatching = new ArrayList<>();
		this.allocators = new Int2ObjectArrayMap<>(5);
		this.batchedDraws = new Int2ObjectArrayMap<>(5);
		this.vertexBuffers = new Object2ObjectOpenHashMap<>();
		this.preparedDraws = new ArrayList<>();
		this.draws = new ArrayList<>();
		this.lastUnbatchedDraw = null;
	}

	public static Renderer getInstance() {
		return instance == null ? instance = new Renderer() : instance;
	}

	void excludePipelineFromBatching(RenderPipeline pipeline) {
		excludedFromBatching.add(pipeline);
	}

	void executeDraws() {
		// End all the batches and prepare the draws
		endBatches();

		// Set up the draws
		setupDraws();

		// Execute the draws
		for (Draw draw : draws) {
			draw(draw);
		}

		// Rotate the buffers
		// ensures that we're likely to be using buffers that the GPU isn't (prevents synchronization/stalls)
		for (MappableRingBuffer buffer : vertexBuffers.values()) {
			buffer.rotate();
		}

		// Clear the draws from this frame
		batchedDraws.clear();
		preparedDraws.clear();
		draws.clear();
	}

	public BufferBuilder getBuffer(@NotNull RenderPipeline pipeline) {
		return getBuffer(pipeline, null, DEFAULT_LINE_WIDTH);
	}

	public BufferBuilder getBuffer(@NotNull RenderPipeline pipeline, @NotNull GpuTextureView textureView) {
		return getBuffer(pipeline, textureView, DEFAULT_LINE_WIDTH);
	}

	public BufferBuilder getBuffer(@NotNull RenderPipeline pipeline, float lineWidth) {
		return getBuffer(pipeline, null, lineWidth);
	}

	public void close() {
		GENERAL_ALLOCATOR.close();

		for (BufferAllocator allocator : allocators.values()) {
			allocator.close();
		}

		for (MappableRingBuffer vertexBuffer : vertexBuffers.values()) {
			vertexBuffer.close();
		}
	}

	/**
	 * Returns the appropriate {@code BufferBuilder} that should be used with the given pipeline, texture view, and line width.
	 */
	private BufferBuilder getBuffer(RenderPipeline pipeline, @Nullable GpuTextureView textureView, float lineWidth) {
		if (!excludedFromBatching.contains(pipeline)) {
			return setupBatched(pipeline, textureView, lineWidth);
		} else {
			return setupUnbatched(pipeline, textureView, lineWidth);
		}
	}

	private BufferBuilder setupBatched(RenderPipeline pipeline, @Nullable GpuTextureView textureView, float lineWidth) {
		int hash = hash(pipeline, textureView, lineWidth);
		BatchedDraw draw = batchedDraws.get(hash);

		if (draw == null) {
			BufferAllocator allocator = allocators.computeIfAbsent(hash, _hash -> new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE));
			BufferBuilder bufferBuilder = new BufferBuilder(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
			batchedDraws.put(hash, new BatchedDraw(bufferBuilder, pipeline, textureView, lineWidth));
			return bufferBuilder;
		} else {
			return draw.bufferBuilder();
		}
	}

	private @NotNull BufferBuilder setupUnbatched(RenderPipeline pipeline, @Nullable GpuTextureView textureView, float lineWidth) {
		if (lastUnbatchedDraw != null) {
			prepareBatchedDraw(lastUnbatchedDraw);
		}

		BufferBuilder bufferBuilder = new BufferBuilder(GENERAL_ALLOCATOR, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
		lastUnbatchedDraw = new BatchedDraw(bufferBuilder, pipeline, textureView, lineWidth);

		return bufferBuilder;
	}

	/**
	 * Calculates the hash of the given inputs which serves as the keys to our maps where we store stuff for the batched draws.
	 * This is much faster than using an object-based key as we do not need to create any objects to find the instances we want.
	 */
	private int hash(@NotNull RenderPipeline pipeline, @Nullable GpuTextureView textureView, float lineWidth) {
		// This manually calculates the hash, avoiding Objects#hash to not incur the array allocation each time
		int hash = 1;
		hash = 31 * hash + pipeline.hashCode();
		hash = 31 * hash + Objects.hashCode(textureView);
		hash = 31 * hash + Float.hashCode(lineWidth);

		return hash;
	}

	private void endBatches() {
		for (Int2ObjectMap.Entry<BatchedDraw> entry : Int2ObjectMaps.fastIterable(batchedDraws)) {
			prepareBatchedDraw(entry.getValue());
		}

		if (lastUnbatchedDraw != null) {
			prepareBatchedDraw(lastUnbatchedDraw);
			lastUnbatchedDraw = null;
		}
	}

	private void prepareBatchedDraw(@NotNull BatchedDraw draw) {
		preparedDraws.add(new PreparedDraw(
				draw.bufferBuilder().end(),
				draw.pipeline(),
				draw.textureView(),
				draw.lineWidth()
		));
	}

	private void setupDraws() {
		setupVertexBuffers();
		Object2IntMap<VertexFormat> vertexBufferPositions = new Object2IntOpenHashMap<>();

		for (PreparedDraw prepared : preparedDraws) {
			BuiltBuffer builtBuffer = prepared.builtBuffer();
			BuiltBuffer.DrawParameters drawParameters = builtBuffer.getDrawParameters();
			VertexFormat format = drawParameters.format();

			MappableRingBuffer vertices = vertexBuffers.get(format);
			ByteBuffer vertexData = builtBuffer.getBuffer();
			int vertexBufferPosition = vertexBufferPositions.getInt(format);
			int remainingVertexBytes = vertexData.remaining();

			// Copy vertex data into the shared vertex buffer
			copyDataInto(vertices, vertexData, vertexBufferPosition, remainingVertexBytes);
			// Update vertex buffer position
			vertexBufferPositions.put(format, vertexBufferPosition + remainingVertexBytes);

			draws.add(new Draw(
					builtBuffer,
					vertices.getBlocking(),
					vertexBufferPosition / format.getVertexSize(),
					drawParameters.indexCount(),
					prepared.pipeline(),
					prepared.textureView(),
					prepared.lineWidth()
			));
		}
	}

	/**
	 * Maps the {@code target} buffer and copies the {@code source} data into it.
	 */
	private void copyDataInto(@NotNull MappableRingBuffer target, ByteBuffer source, int position, int remainingBytes) {
		CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

		try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(target.getBlocking().slice(position, remainingBytes), false, true)) {
			MemoryUtil.memCopy(source, mappedView.data());
		}
	}

	/**
	 * Resizes/allocates the necessary vertex buffers.
	 */
	private void setupVertexBuffers() {
		Object2IntMap<VertexFormat> vertexBufferSizes = collectVertexBufferSizes();

		for (Object2IntMap.Entry<VertexFormat> entry : Object2IntMaps.fastIterable(vertexBufferSizes)) {
			VertexFormat format = entry.getKey();
			int vertexBufferSize = entry.getIntValue();

			vertexBuffers.compute(format, (k, vertexBuffer) -> initOrResizeBuffer(vertexBuffer, "CaribouStonks vertex buffer for: " + format, vertexBufferSize));
		}
	}

	@Contract("null, _, _ -> new")
	private @NotNull MappableRingBuffer initOrResizeBuffer(MappableRingBuffer buffer, String name, int neededSize) {
		if (buffer == null || buffer.size() < neededSize) {
			if (buffer != null) {
				buffer.close();
			}

			return new MappableRingBuffer(() -> name, GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_VERTEX, neededSize);
		}

		return buffer;
	}

	/**
	 * Collect the required buffer size for each vertex format in use.
	 */
	private @NotNull Object2IntMap<VertexFormat> collectVertexBufferSizes() {
		// If we ever need to create our own shared index buffers,
		// then we can turn this into an Object2LongMap and pack
		// both the vertex & index buffer sizes into a single long (since they're two ints)
		Object2IntMap<VertexFormat> vertexSizes = new Object2IntOpenHashMap<>();

		for (PreparedDraw prepared : preparedDraws) {
			BuiltBuffer.DrawParameters drawParameters = prepared.builtBuffer().getDrawParameters();
			VertexFormat format = drawParameters.format();

			vertexSizes.put(format, vertexSizes.getOrDefault(format, 0) + drawParameters.vertexCount() * format.getVertexSize());
		}

		return vertexSizes;
	}

	private void draw(@NotNull Draw draw) {
		GpuBuffer indices;
		VertexFormat.IndexType indexType;

		if (draw.pipeline().getVertexFormatMode() == VertexFormat.DrawMode.QUADS) {
			// The quads we're rendering are translucent, so they need to be sorted for our index buffer
			draw.builtBuffer().sortQuads(GENERAL_ALLOCATOR, RenderSystem.getProjectionType().getVertexSorter());
			indices = draw.pipeline().getVertexFormat().uploadImmediateIndexBuffer(draw.builtBuffer().getSortedBuffer());
			indexType = draw.builtBuffer().getDrawParameters().indexType();
		} else {
			// Use a general shape index buffer for other draw modes
			RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(draw.pipeline().getVertexFormatMode());
			indices = shapeIndexBuffer.getIndexBuffer(draw.indexCount());
			indexType = shapeIndexBuffer.getIndexType();
		}

		draw(draw, indices, indexType);
	}

	private void draw(@NotNull Draw draw, GpuBuffer indices, VertexFormat.IndexType indexType) {
		applyViewOffsetZLayering();
		GpuBufferSlice dynamicTransforms = setupDynamicTransforms(draw.lineWidth);

		try (RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(() -> "cariboustonks world rendering",
						getMainColorTexture(),
						OptionalInt.empty(),
						getMainDepthTexture(),
						OptionalDouble.empty())
		) {
			renderPass.setPipeline(draw.pipeline);

			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);

			// Bind texture if applicable
			if (draw.textureView != null) {
				// Sampler0 is used for texture inputs in vertices
				renderPass.bindSampler("Sampler0", draw.textureView);
			}

			renderPass.setVertexBuffer(0, draw.vertices);
			renderPass.setIndexBuffer(indices, indexType);

			renderPass.drawIndexed(draw.baseVertex, 0, draw.indexCount, 1);
		}

		draw.builtBuffer().close();
		unapplyViewOffsetZLayering();
	}

	private GpuBufferSlice setupDynamicTransforms(float lineWidth) {
		return RenderSystem.getDynamicUniforms().write(
				RenderSystem.getModelViewMatrix(),
				COLOR_MODULATOR,
				RenderSystem.getModelOffset(),
				RenderSystem.getTextureMatrix(),
				lineWidth
		);
	}

	private GpuTextureView getMainColorTexture() {
		return CLIENT.getFramebuffer().getColorAttachmentView();
	}

	private GpuTextureView getMainDepthTexture() {
		return CLIENT.getFramebuffer().getDepthAttachmentView();
	}

	private void applyViewOffsetZLayering() {
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		RenderSystem.getProjectionType().apply(modelViewStack, 1f);
	}

	private void unapplyViewOffsetZLayering() {
		RenderSystem.getModelViewStack().popMatrix();
	}

	private record Draw(
			BuiltBuffer builtBuffer,
			GpuBuffer vertices,
			int baseVertex,
			int indexCount,
			RenderPipeline pipeline,
			@Nullable GpuTextureView textureView,
			float lineWidth
	) {
	}

	private record PreparedDraw(
			BuiltBuffer builtBuffer,
			RenderPipeline pipeline,
			@Nullable GpuTextureView textureView,
			float lineWidth
	) {
	}

	private record BatchedDraw(
			BufferBuilder bufferBuilder,
			RenderPipeline pipeline,
			@Nullable GpuTextureView textureView,
			float lineWidth
	) {
	}
}
