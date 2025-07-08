package fr.siroz.cariboustonks.util.render;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.animation.AnimationUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * <b>3D</b> renderings in the world.
 * <p>
 * Credits to the Skyblocker Team (<a href="https://github.com/SkyblockerMod/Skyblocker">GitHub Skyblocker</a>),
 * Wynntils Team (<a href="https://github.com/Wynntils/Wynntils">GitHub Wynntils</a>) and other mods
 * for helping me lay a foundation and better understand the latest version of the renderings.
 */
@SuppressWarnings("checkstyle:linelength")
public final class WorldRenderUtils {

	private static final Identifier TRANSLUCENT_DRAW = CaribouStonks.identifier("translucent_draw");
	private static final BufferAllocator ALLOCATOR = new BufferAllocator(1536); // 256
	private static final int MAX_BUILD_HEIGHT = 300;

	private WorldRenderUtils() {
	}

	@ApiStatus.Internal
	public static void initRenderUtilities() {
		CustomRenderPipelines.init();
		CustomRenderLayers.init();
		WorldRenderEvents.AFTER_TRANSLUCENT.addPhaseOrdering(Event.DEFAULT_PHASE, TRANSLUCENT_DRAW);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(TRANSLUCENT_DRAW, WorldRenderUtils::drawTranslucent);
	}

	/**
	 * This is called after all {@link WorldRenderEvents#AFTER_TRANSLUCENT} listeners have been called
	 * so that we can draw all remaining render layers.
	 * <p>
	 * Source: Skyblocker
	 */
	private static void drawTranslucent(@NotNull WorldRenderContext context) {
		VertexConsumerProvider.Immediate immediate = (VertexConsumerProvider.Immediate) context.consumers();
		assert immediate != null;
		immediate.draw();
	}

	/**
	 * Draws a {@link Text} in the world.
	 * <p>
	 * The {@code scale} is set to {@code 1}.
	 *
	 * @param context       WorldRenderContext
	 * @param text          the text to display
	 * @param position      the position
	 * @param throughBlocks if rendering can be done through blocks
	 */
	public static void renderText(
			@NotNull WorldRenderContext context,
			@NotNull Text text,
			@NotNull Vec3d position,
			boolean throughBlocks
	) {
		renderText(context, text, position, 1, throughBlocks);
	}

	/**
	 * Draws a {@link Text} in the world with the given {@code scale}.
	 *
	 * @param context       WorldRenderContext
	 * @param text          the text to display
	 * @param position      the position
	 * @param scale         the scale
	 * @param throughBlocks if rendering can be done through blocks
	 */
	public static void renderText(
			@NotNull WorldRenderContext context,
			@NotNull Text text,
			@NotNull Vec3d position,
			float scale,
			boolean throughBlocks
	) {
		renderText(context, text, position, scale, 0, throughBlocks);
	}

	/**
	 * Draws a {@link Text} in the world with the given {@code scale} and {@code yOffset}.
	 *
	 * @param context       WorldRenderContext
	 * @param text          the text to display
	 * @param position      the position
	 * @param scale         the scale
	 * @param offsetY       the offsetY
	 * @param throughBlocks if rendering can be done through blocks
	 */
	public static void renderText(
			@NotNull WorldRenderContext context,
			@NotNull Text text,
			@NotNull Vec3d position,
			float scale,
			float offsetY,
			boolean throughBlocks
	) {
		renderText(context, text.asOrderedText(), position, scale, offsetY, throughBlocks);
	}

	/**
	 * Draws a {@link OrderedText} in the world.
	 *
	 * @param context       WorldRenderContext
	 * @param text          the text to display
	 * @param position      the position
	 * @param scale         the scale
	 * @param offsetY       the offsetY
	 * @param throughBlocks if rendering can be done through blocks
	 */
	public static void renderText(
			@NotNull WorldRenderContext context,
			@NotNull OrderedText text,
			@NotNull Vec3d position,
			float scale,
			float offsetY,
			boolean throughBlocks
	) {
		Matrix4f matrix4f = new Matrix4f();
		Camera camera = context.camera();
		Vec3d cameraPos = camera.getPos();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		scale *= 0.025f;

		double dx = position.getX() - cameraPos.getX();
		double dy = position.getY() - cameraPos.getY();
		double dz = position.getZ() - cameraPos.getZ();

		matrix4f.translate((float) dx, (float) dy, (float) dz)
				.rotate(camera.getRotation())
				.scale(scale, -scale, scale);

		float xOffset = -textRenderer.getWidth(text) / 2f;

		VertexConsumerProvider.Immediate vertex = VertexConsumerProvider.immediate(ALLOCATOR);

		textRenderer.draw(text, xOffset, offsetY,
				0xFFFFFFFF, false,
				matrix4f, vertex,
				throughBlocks ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL,
				0,
				LightmapTextureManager.MAX_LIGHT_COORDINATE);

		vertex.draw();
	}

	/**
	 * Draws a {@code Texture} with the given {@link Texture} facing to the player.
	 *
	 * @param context       WorldRenderContext
	 * @param position      the position
	 * @param width         the width
	 * @param height        the height
	 * @param textureWidth  the amount of texture width
	 * @param textureHeight the amount of texture height
	 * @param renderOffset  the offset
	 * @param texture       the texture
	 * @param color         the color
	 * @param throughBlocks if rendering can be done through blocks
	 * @see #renderTexture(WorldRenderContext, Vec3d, float, float, float, float, Vec3d, Identifier, Color, float, boolean)
	 */
	public static void renderTexture(
			@NotNull WorldRenderContext context,
			@NotNull Vec3d position,
			float width,
			float height,
			float textureWidth,
			float textureHeight,
			@NotNull Vec3d renderOffset,
			@NotNull Texture texture,
			@NotNull Color color,
			float alpha,
			boolean throughBlocks
	) {
		renderTexture(context, position, width, height, textureWidth, textureHeight, renderOffset, texture.getIdentifier(), color, alpha, throughBlocks);
	}

	/**
	 * Draws a {@code Texture} with the given {@link Identifier} facing to the player.
	 *
	 * @param context       WorldRenderContext
	 * @param position      the position
	 * @param width         the width
	 * @param height        the height
	 * @param textureWidth  the amount of texture width
	 * @param textureHeight the amount of texture height
	 * @param renderOffset  the offset
	 * @param texture       the texture
	 * @param color         the color
	 * @param throughBlocks if rendering can be done through blocks
	 * @see #renderTexture(WorldRenderContext, Vec3d, float, float, float, float, Vec3d, Texture, Color, float, boolean)
	 */
	public static void renderTexture(
			@NotNull WorldRenderContext context,
			@NotNull Vec3d position,
			float width,
			float height,
			float textureWidth,
			float textureHeight,
			@NotNull Vec3d renderOffset,
			@NotNull Identifier texture,
			@NotNull Color color,
			float alpha,
			boolean throughBlocks
	) {
		Matrix4f matrix4f = new Matrix4f();
		Camera camera = context.camera();
		Vec3d cameraPos = camera.getPos();

		double dx = position.getX() - cameraPos.x;
		double dy = position.getY() - cameraPos.y;
		double dz = position.getZ() - cameraPos.z;

		matrix4f.translate((float) dx, (float) dy, (float) dz)
				.rotate(camera.getRotation());

		VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
		RenderLayer layer = throughBlocks ? CustomRenderLayers.getTextureThroughBlocks(texture) : CustomRenderLayers.getTexture(texture);
		assert consumers != null;
		VertexConsumer buffer = consumers.getBuffer(layer);

		float[] colorComponents = color.asFloatComponents();

		buffer.vertex(matrix4f, (float) renderOffset.getX(), (float) renderOffset.getY(), (float) renderOffset.getZ())
				.texture(1, 1 + textureHeight)
				.color(colorComponents[0], colorComponents[1], colorComponents[2], alpha);

		buffer.vertex(matrix4f, (float) renderOffset.getX(), (float) renderOffset.getY() + height, (float) renderOffset.getZ())
				.texture(1, 1)
				.color(colorComponents[0], colorComponents[1], colorComponents[2], alpha);

		buffer.vertex(matrix4f, (float) renderOffset.getX() + width, (float) renderOffset.getY() + height, (float) renderOffset.getZ())
				.texture(1 + textureWidth, 1)
				.color(colorComponents[0], colorComponents[1], colorComponents[2], alpha);

		buffer.vertex(matrix4f, (float) renderOffset.getX() + width, (float) renderOffset.getY(), (float) renderOffset.getZ())
				.texture(1 + textureWidth, 1 + textureHeight)
				.color(colorComponents[0], colorComponents[1], colorComponents[2], alpha);

		consumers.draw(layer);
	}

	/**
	 * Draws a {@code circle} in the world on the plane defined by the specified axis.
	 * <p>
	 * The circle is drawn in outline mode (border).
	 * <p>
	 * <h3>Axis</h3>
	 * Defines the plane in which the circle lies
	 * <ul>
	 *     <li>{@code Axis.X}: Circle in YZ plane (vertical, perpendicular to X axis)</li>
	 *     <li>{@code Axis.Y}: Circle in XZ plane (horizontal, perpendicular to Y axis)</li>
	 *     <li>{@code Axis.Z}: Circle in XY plane (vertical, perpendicular to Z axis)</li>
	 * </ul>
	 * <p>
	 * {@code Dev Note:} To draw the border, a ring is created by drawing two circles (outer and inner)
	 * and connecting them with a TRIANGLE_STRIP.
	 *
	 * @param context          WorldRenderContext
	 * @param center           the center
	 * @param radius           the radius ({@code in blocks ~})
	 * @param segments         number of segments
	 * @param thicknessPercent thickness in {@code %} of radius (0.05 = 5% of radius)
	 * @param color            the color
	 * @param axis             the normal axis of the circle plane
	 * @param throughBlocks    if rendering can be done through blocks
	 */
	public static void renderCircle(
			@NotNull WorldRenderContext context,
			@NotNull Vec3d center,
			@Range(from = 1, to = 32) double radius,
			@Range(from = 8, to = 64) int segments,
			float thicknessPercent,
			@NotNull Color color,
			@NotNull Direction.Axis axis,
			boolean throughBlocks
	) {
		Matrix4f matrix4f = new Matrix4f();
		Vec3d cameraPos = context.camera().getPos();

		matrix4f.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

		VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
		RenderLayer layer = throughBlocks ? CustomRenderLayers.CIRCLES_THROUGH_BLOCKS : CustomRenderLayers.CIRCLES;
		assert consumers != null;
		VertexConsumer buffer = consumers.getBuffer(layer);

		// 5% du rayon (0.05) | min : 0.01f (trop faible) | max : 0.95 (la quasi-totalité du cercle)
		thicknessPercent = Math.max(0.01f, Math.min(thicknessPercent, 0.95f));
		float thickness = (float) (radius * thicknessPercent);

		for (int i = 0; i <= segments; i++) {
			double angle = 2.0 * Math.PI * i / segments;
			float sin = (float) Math.sin(angle);
			float cos = (float) Math.cos(angle);
			// Vertex extérieur
			float outerX = (float) center.x;
			float outerY = (float) center.y;
			float outerZ = (float) center.z;
			// Vertex intérieur
			float innerX = (float) center.x;
			float innerY = (float) center.y;
			float innerZ = (float) center.z;

			switch (axis) {
				case X -> {
					outerY += (float) ((radius + thickness) * cos);
					outerZ += (float) ((radius + thickness) * sin);
					innerY += (float) ((radius - thickness) * cos);
					innerZ += (float) ((radius - thickness) * sin);
				}
				case Y -> {
					outerX += (float) ((radius + thickness) * cos);
					outerZ += (float) ((radius + thickness) * sin);
					innerX += (float) ((radius - thickness) * cos);
					innerZ += (float) ((radius - thickness) * sin);
				}
				case Z -> {
					outerX += (float) ((radius + thickness) * cos);
					outerY += (float) ((radius + thickness) * sin);
					innerX += (float) ((radius - thickness) * cos);
					innerY += (float) ((radius - thickness) * sin);
				}
				default -> {
				}
			}

			buffer.vertex(matrix4f, outerX, outerY, outerZ).color(color.r, color.g, color.b, color.a);
			buffer.vertex(matrix4f, innerX, innerY, innerZ).color(color.r, color.g, color.b, color.a);
		}

		consumers.draw(layer);
	}

	/**
	 * Draw a {@code Thick Disk} in the world, extruding a <b>horizontal</b> circle.
	 * <p>
	 * {@code Dev Note:} Strip design for side walls
	 *
	 * @param context       WorldRenderContext
	 * @param center        the center (bottom circle position)
	 * @param radius        the radius (in blocks ~)
	 * @param thickness     thickness (in blocks ~)
	 * @param segments      number of segments
	 * @param color         the color
	 * @param throughBlocks if rendering can be done through blocks
	 */
	public static void renderThickCircle(
			@NotNull WorldRenderContext context,
			@NotNull Vec3d center,
			@Range(from = 1, to = 32) double radius,
			double thickness,
			@Range(from = 8, to = 64) int segments,
			@NotNull Color color,
			boolean throughBlocks
	) {
		Matrix4f matrix4f = new Matrix4f();
		Vec3d cameraPos = context.camera().getPos();
		Vec3d centerTop = center.add(0, thickness, 0);

		matrix4f.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

		VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
		RenderLayer layer = throughBlocks ? CustomRenderLayers.CIRCLES_THROUGH_BLOCKS : CustomRenderLayers.CIRCLES;
		assert consumers != null;
		VertexConsumer buffer = consumers.getBuffer(layer);

		for (int i = 0; i <= segments; i++) {
			double angle = 2 * Math.PI * i / segments;
			float cos = (float) Math.cos(angle);
			float sin = (float) Math.sin(angle);
			// Vertex inférieur
			float xLower = (float) (center.x + radius * cos);
			float zLower = (float) (center.z + radius * sin);
			// Vertex supérieur
			float xUpper = (float) (center.x + radius * cos);
			float zUpper = (float) (center.z + radius * sin);

			buffer.vertex(matrix4f, xUpper, (float) centerTop.y, zUpper).color(color.r, color.g, color.b, color.a);
			buffer.vertex(matrix4f, xLower, (float) center.y, zLower).color(color.r, color.g, color.b, color.a);
		}

		consumers.draw(layer);
	}

	/**
	 * Draws a {@code Quad} in the world.
	 *
	 * @param context       WorldRenderContext
	 * @param points        the array of points
	 * @param color         the color
	 * @param alpha         the alpha
	 * @param throughBlocks if rendering can be done through blocks
	 */
	public static void renderQuad(
			@NotNull WorldRenderContext context,
			@NotNull Vec3d[] points,
			@NotNull Color color,
			float alpha,
			boolean throughBlocks
	) {
		Matrix4f matrix4f = new Matrix4f();
		Vec3d cameraPos = context.camera().getPos();
		float[] colorComponents = color.asFloatComponents();

		matrix4f.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

		VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
		RenderLayer layer = throughBlocks ? CustomRenderLayers.QUADS_THROUGH_BLOCKS : CustomRenderLayers.QUADS;
		assert consumers != null;
		VertexConsumer buffer = consumers.getBuffer(layer);

		for (int i = 0; i < 4; i++) {
			buffer.vertex(matrix4f, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ())
					.color(colorComponents[0], colorComponents[1], colorComponents[2], alpha);
		}

		consumers.draw(layer);
	}

	/**
	 * Draws a {@code Filled Block} & {@code Beacon Beam} in the world.
	 *
	 * @param context       WorldRenderContext
	 * @param position      the position
	 * @param color         the color
	 * @param alpha         the alpha
	 * @param throughBlocks if rendering can be done through blocks
	 */
	public static void renderFilledWithBeaconBeam(
			@NotNull WorldRenderContext context,
			@NotNull BlockPos position,
			@NotNull Color color,
			float alpha,
			boolean throughBlocks
	) {
		renderFilled(context, position, color, alpha, throughBlocks);
		renderBeaconBeam(context, position, color);
	}

	public static void renderFilled(
			@NotNull WorldRenderContext context,
			@NotNull BlockPos pos,
			@NotNull Color color,
			float alpha,
			boolean throughBlocks
	) {
		renderFilled(context, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, color, alpha, throughBlocks);
	}

	public static void renderFilled(
			@NotNull WorldRenderContext context,
			@NotNull Vec3d pos,
			@NotNull Vec3d dimensions,
			@NotNull Color color,
			float alpha,
			boolean throughBlocks
	) {
		renderFilled(context, pos.x, pos.y, pos.z, pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z, color, alpha, throughBlocks);
	}

	public static void renderFilled(
			@NotNull WorldRenderContext context,
			@NotNull Box box,
			@NotNull Color color,
			float alpha,
			boolean throughBlocks
	) {
		renderFilled(context, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color, alpha, throughBlocks);
	}

	/**
	 * Draws a {@code Filled Box} in the world with the given {@code dimensions}.
	 *
	 * @param context       WorldRenderContext
	 * @param color         the color
	 * @param alpha         the alpha
	 * @param throughBlocks if rendering can be done through blocks
	 */
	public static void renderFilled(
			@NotNull WorldRenderContext context,
			double minX,
			double minY,
			double minZ,
			double maxX,
			double maxY,
			double maxZ,
			@NotNull Color color,
			float alpha,
			boolean throughBlocks
	) {
		if (FrustumUtils.isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
			renderFilledInternal(context, minX, minY, minZ, maxX, maxY, maxZ, color, alpha, throughBlocks);
		}
	}

	private static void renderFilledInternal(
			@NotNull WorldRenderContext context,
			double minX,
			double minY,
			double minZ,
			double maxX,
			double maxY,
			double maxZ,
			@NotNull Color color,
			float alpha,
			boolean throughBlocks
	) {
		MatrixStack matrices = context.matrixStack();
		Vec3d camera = context.camera().getPos();
		boolean rainbow = false;
		if (color == Colors.RAINBOW) {
			rainbow = true;
			int colorInt = AnimationUtils.getCurrentRainbowColor().withAlpha(1f).asInt();
			color = Color.fromInt(colorInt);
		}

		assert matrices != null;
		matrices.push();
		matrices.translate(-camera.x, -camera.y, -camera.z);

		VertexConsumerProvider consumers = context.consumers();
		assert consumers != null;
		VertexConsumer buffer = consumers.getBuffer(throughBlocks ? CustomRenderLayers.FILLED_THROUGH_BLOCKS : CustomRenderLayers.FILLED);

		float[] colorComponents = color.asFloatComponents();
		VertexRendering.drawFilledBox(matrices, buffer, minX, minY, minZ, maxX, maxY, maxZ, colorComponents[0], colorComponents[1], colorComponents[2], rainbow ? 1f : alpha);

		matrices.pop();
	}

	/**
	 * Draws a {@code Beacon Beam} in the world.
	 *
	 * @param context  WorldRenderContext
	 * @param position tre position
	 * @param color    the color
	 */
	public static void renderBeaconBeam(
			@NotNull WorldRenderContext context,
			@NotNull BlockPos position,
			@NotNull Color color
	) {
		if (FrustumUtils.isVisible(position.getX(), position.getY(), position.getZ(), position.getX() + 1, MAX_BUILD_HEIGHT, position.getZ() + 1)) {
			MatrixStack stack = context.matrixStack();
			Vec3d cameraPos = context.camera().getPos();

			assert stack != null;
			stack.push();

			double dx = position.getX() - cameraPos.x;
			double dy = position.getY() - cameraPos.y;
			double dz = position.getZ() - cameraPos.z;

			stack.translate(dx, dy, dz);

			int colorInt;
			if (color == Colors.RAINBOW) {
				colorInt = AnimationUtils.getCurrentRainbowColor().withAlpha(1f).asInt();
			} else {
				colorInt = color.withAlpha(1f).asInt();
			}

			float length = (float) cameraPos.subtract(position.toCenterPos()).horizontalLength();
			float scale = Math.max(1.0f, length / 96.0f);

			// VertexConsumerProvider.Immediate > BufferAllocator de 256? | drawCurrentLayer (bach)
			BeaconBlockEntityRenderer.renderBeam(
					stack,
					Objects.requireNonNull(context.consumers()),
					BeaconBlockEntityRenderer.BEAM_TEXTURE,
					context.tickCounter().getTickProgress(true),
					scale, // 1.0f
					context.world().getTime(), // auto-closable
					0,
					MAX_BUILD_HEIGHT,
					colorInt,
					0.2f, // 0.166f
					0.25f // 0.33f
			);

			stack.pop();
		}
	}

	/**
	 * Draws a {@code Outline Box} in the world.
	 * <p>
	 * {@code Alpha} is set to {@code 1f}.
	 *
	 * @param context       WorldRenderContext
	 * @param box           the box
	 * @param color         the color
	 * @param lineWidth     the line width
	 * @param throughBlocks if rendering can be done through blocks
	 */
	public static void renderOutline(
			@NotNull WorldRenderContext context,
			@NotNull Box box,
			@NotNull Color color,
			float lineWidth,
			boolean throughBlocks
	) {
		renderOutline(context, box, color, 1f, lineWidth, throughBlocks);
	}

	/**
	 * Draws a {@code Outline Box} in the world.
	 *
	 * @param context       WorldRenderContest
	 * @param box           the box
	 * @param color         the color
	 * @param alpha         the alpha
	 * @param lineWidth     the line width
	 * @param throughBlocks if rendering can be done through blocks
	 */
	public static void renderOutline(
			@NotNull WorldRenderContext context,
			@NotNull Box box,
			@NotNull Color color,
			float alpha,
			float lineWidth,
			boolean throughBlocks
	) {
		if (FrustumUtils.isVisible(box)) {
			MatrixStack matrices = context.matrixStack();
			Vec3d camera = context.camera().getPos();
			float[] colorComponents = color.asFloatComponents();

			assert matrices != null;
			matrices.push();
			matrices.translate(-camera.getX(), -camera.getY(), -camera.getZ());

			VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
			RenderLayer layer = throughBlocks ? CustomRenderLayers.getLinesThroughBlocks(lineWidth) : CustomRenderLayers.getLines(lineWidth);

			assert consumers != null;
			VertexConsumer buffer = consumers.getBuffer(layer);
			VertexRendering.drawBox(matrices, buffer, box, colorComponents[0], colorComponents[1], colorComponents[2], alpha);

			consumers.draw(layer);
			matrices.pop();
		}
	}

	/**
	 * Draws multiple {@code Lines} between the given points.
	 *
	 * @param context       WorldRenderContext
	 * @param points        the points
	 * @param color         the color
	 * @param lineWidth     the line width
	 * @param throughBlocks if rendering can be done through blocks
	 */
	public static void renderLinesFromPoints(
			@NotNull WorldRenderContext context,
			Vec3d @NotNull [] points,
			@NotNull Color color,
			float lineWidth,
			boolean throughBlocks
	) {
		Vec3d cameraPos = context.camera().getPos();
		MatrixStack stack = context.matrixStack();

		assert stack != null;
		stack.push();
		stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

		MatrixStack.Entry entry = stack.peek();

		VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
		RenderLayer layer = throughBlocks ? CustomRenderLayers.getLinesThroughBlocks(lineWidth) : CustomRenderLayers.getLines(lineWidth);
		assert consumers != null;
		VertexConsumer buffer = consumers.getBuffer(layer);

		for (int i = 0; i < points.length; i++) {
			Vec3d nextPoint = points[i + 1 == points.length ? i - 1 : i + 1];
			Vector3f normalVec = nextPoint.toVector3f().sub((float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ()).normalize();
			if (i + 1 == points.length) {
				normalVec.negate();
			}

			buffer.vertex(entry, (float) points[i].getX(), (float) points[i].getY(), (float) points[i].getZ())
					.color(color.r, color.g, color.b, color.a)
					.normal(entry, normalVec);
		}

		consumers.draw(layer);
		stack.pop();
	}

	/**
	 * Draws a {@code Line} from the client cursor to the target point
	 *
	 * @param context   WorldRenderContext
	 * @param point     the point to target
	 * @param color     the color
	 * @param lineWidth the line width
	 */
	public static void renderLineFromCursor(
			@NotNull WorldRenderContext context,
			@NotNull Vec3d point,
			@NotNull Color color,
			float lineWidth
	) {
		Vec3d cameraPos = context.camera().getPos();
		MatrixStack stack = context.matrixStack();

		assert stack != null;
		stack.push();
		stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

		MatrixStack.Entry entry = stack.peek();

		VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
		RenderLayer layer = CustomRenderLayers.getLines(lineWidth);
		assert consumers != null;
		VertexConsumer buffer = consumers.getBuffer(layer);

		// Devant la caméra
		Vec3d cameraPoint = cameraPos.add(Vec3d.fromPolar(context.camera().getPitch(), context.camera().getYaw()));

		Vector3f normal = point.toVector3f()
				.sub((float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z)
				.normalize();

		buffer.vertex(entry, (float) cameraPoint.x, (float) cameraPoint.y, (float) cameraPoint.z)
				.color(color.r, color.g, color.b, color.a)
				.normal(entry, normal);

		buffer.vertex(entry, (float) point.getX(), (float) point.getY(), (float) point.getZ())
				.color(color.r, color.g, color.b, color.a)
				.normal(entry, normal);

		consumers.draw(layer);
		stack.pop();
	}

	/**
	 * @deprecated => {@link #renderCircle(WorldRenderContext, Vec3d, double, int, float, Color, Direction.Axis, boolean) renderCircle}
	 */
	@Deprecated
	public static Vec3d @NotNull [] createCircle(
			@NotNull Vec3d center,
			double radius,
			int segments,
			@NotNull Direction.Axis axis
	) {
		Vec3d[] points = new Vec3d[segments + 1]; // +1 pour fermer le cercle

		for (int i = 0; i <= segments; i++) {
			double angle = 2.0 * Math.PI * i / segments;
			float sin = (float) Math.sin(angle);
			float cos = (float) Math.cos(angle);

			float x = (float) center.x;
			float y = (float) center.y;
			float z = (float) center.z;

			switch (axis) {
				case X -> { // Plan YZ (vertical, perpendiculaire à X)
					y += (float) (radius * cos);
					z += (float) (radius * sin);
				}
				case Y -> { // Plan XZ (horizontal)
					x += (float) (radius * cos);
					z += (float) (radius * sin);
				}
				case Z -> { // Plan XY (vertical, perpendiculaire à Z)
					x += (float) (radius * cos);
					y += (float) (radius * sin);
				}
				default -> {
				}
			}

			points[i] = new Vec3d(x, y, z);
		}

		return points;
	}
}
