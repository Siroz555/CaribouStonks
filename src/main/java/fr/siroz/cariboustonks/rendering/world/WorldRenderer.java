package fr.siroz.cariboustonks.rendering.world;

import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.render.Texture;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Provides methods to render various visual elements in the world with
 * the {@link RenderEvents#WORLD_RENDER}.
 */
public interface WorldRenderer {

	/**
	 * Submits a {@link Component} to be rendered.
	 *
	 * @param text          the text
	 * @param position      the position
	 * @param scale         the scale
	 * @param throughBlocks if rendering can be done through blocks
	 */
	default void submitText(@NotNull Component text, @NotNull Vec3 position, float scale, boolean throughBlocks) {
		submitText(text, position, scale, 0, throughBlocks);
	}

	/**
	 * Submits a {@link Component} to be rendered.
	 *
	 * @param text          the text
	 * @param position      the position
	 * @param scale         the scale
	 * @param offsetY       the offsetY
	 * @param throughBlocks if rendering can be done through blocks
	 */
	default void submitText(@NotNull Component text, @NotNull Vec3 position, float scale, float offsetY, boolean throughBlocks) {
		submitText(text.getVisualOrderText(), position, scale, offsetY, throughBlocks);
	}

	/**
	 * Submits a {@link FormattedCharSequence} to be rendered.
	 *
	 * @param text          the text
	 * @param position      the position
	 * @param scale         the scale
	 * @param offsetY       the offsetY
	 * @param throughBlocks if rendering can be done through blocks
	 */
	void submitText(@NotNull FormattedCharSequence text, @NotNull Vec3 position, float scale, float offsetY, boolean throughBlocks);

	/**
	 * Submits a {@code Texture} to be rendered with the given {@link Texture}, facing to the player.
	 *
	 * @param position      the position
	 * @param width         the width
	 * @param height        the height
	 * @param textureWidth  the amount of texture width
	 * @param textureHeight the amount of texture height
	 * @param renderOffset  the offset
	 * @param texture       the texture
	 * @param color         the color
	 * @param alpha         the alpha
	 * @param throughBlocks if rendering can be done through blocks
	 */
	default void submitTexture(@NotNull Vec3 position, float width, float height, float textureWidth, float textureHeight, @NotNull Vec3 renderOffset, @NotNull Texture texture, @NotNull Color color, float alpha, boolean throughBlocks) {
		submitTexture(position, width, height, textureWidth, textureHeight, renderOffset, texture.getIdentifier(), color, alpha, throughBlocks);
	}

	/**
	 * Submits a {@code Texture} to be rendered with the given {@link Identifier}, facing to the player.
	 *
	 * @param position      the position
	 * @param width         the width
	 * @param height        the height
	 * @param textureWidth  the amount of texture width
	 * @param textureHeight the amount of texture height
	 * @param renderOffset  the offset
	 * @param texture       the texture
	 * @param color         the color
	 * @param alpha         the alpha
	 * @param throughBlocks if rendering can be done through blocks
	 */
	void submitTexture(@NotNull Vec3 position, float width, float height, float textureWidth, float textureHeight, @NotNull Vec3 renderOffset, @NotNull Identifier texture, @NotNull Color color, float alpha, boolean throughBlocks);

	/**
	 * Submits a {@code circle} to be rendered on the plane defined by the specified axis.
	 * <p>
	 * The circle is drawn in outline mode (border).
	 * <h3>Axis</h3>
	 * Defines the plane in which the circle lies
	 * <ul>
	 *     <li>{@code Axis.X}: Circle in YZ plane (vertical, perpendicular to X axis)</li>
	 *     <li>{@code Axis.Y}: Circle in XZ plane (horizontal, perpendicular to Y axis)</li>
	 *     <li>{@code Axis.Z}: Circle in XY plane (vertical, perpendicular to Z axis)</li>
	 * </ul>
	 * <p>
	 * DEV-NOTE: To draw the border, a ring is created by drawing two circles (outer and inner)
	 * and connecting them with a TRIANGLE_STRIP.
	 *
	 * @param center           the center
	 * @param radius           the radius ({@code in blocks ~})
	 * @param segments         number of segments
	 * @param thicknessPercent thickness in {@code %} of radius (0.05 = 5% of radius)
	 * @param color            the color
	 * @param axis             the axis
	 * @param throughBlocks    if rendering can be done through blocks
	 */
	void submitCircle(@NotNull Vec3 center, @Range(from = 1, to = 32) double radius, @Range(from = 8, to = 64) int segments, float thicknessPercent, @NotNull Color color, @NotNull Direction.Axis axis, boolean throughBlocks);

	/**
	 * Submits a {@code thick circle (disk)} to be rendered extruding a <b>horizontal</b> circle.
	 *
	 * @param center        the center
	 * @param radius        the radius ({@code in blocks ~})
	 * @param thickness     the thickness (in blocks ~)
	 * @param segments      number of segments
	 * @param color         the color
	 * @param throughBlocks if rendering can be done through blocks
	 */
	void submitThickCircle(@NotNull Vec3 center, @Range(from = 1, to = 32) double radius, double thickness, @Range(from = 8, to = 64) int segments, @NotNull Color color, boolean throughBlocks);

	/**
	 * Submits a {@code quad} to be rendered.
	 *
	 * @param points        the points
	 * @param color         the color
	 * @param throughBlocks if rendering can be done through blocks
	 */
	void submitQuad(@NotNull Vec3[] points, @NotNull Color color, boolean throughBlocks);

	/**
	 * Submits a {@code Filled Box} to be rendered.
	 *
	 * @param pos           the position
	 * @param color         the color
	 * @param throughBlocks if rendering can be done through blocks
	 */
	default void submitFilled(@NotNull BlockPos pos, @NotNull Color color, boolean throughBlocks) {
		submitFilled(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, color, throughBlocks);
	}

	/**
	 * Submits a {@code Filled Box} to be rendered.
	 *
	 * @param box           the box
	 * @param color         the color
	 * @param throughBlocks if rendering can be done through blocks
	 */
	default void submitFilled(@NotNull AABB box, @NotNull Color color, boolean throughBlocks) {
		submitFilled(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color, throughBlocks);
	}

	/**
	 * Submits a {@code Filled Box} to be rendered.
	 *
	 * @param minX          the minimum x
	 * @param minY          the minimum y
	 * @param minZ          the minimum z
	 * @param maxX          the maximum x
	 * @param maxY          the maximum y
	 * @param maxZ          the maximum z
	 * @param color         the color
	 * @param throughBlocks if rendering can be done through blocks
	 */
	void submitFilled(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, @NotNull Color color, boolean throughBlocks);

	/**
	 * Submits a {@code Beacon beam} to be rendered.
	 *
	 * @param position the position
	 * @param color    the color
	 */
	void submitBeaconBeam(@NotNull BlockPos position, @NotNull Color color);

	/**
	 * Submits an {@code Outline Box} to be rendered.
	 *
	 * @param box           the box
	 * @param color         the color
	 * @param lineWidth     the line width
	 * @param throughBlocks if rendering can be done through blocks
	 */
	void submitOutline(@NotNull AABB box, @NotNull Color color, float lineWidth, boolean throughBlocks);

	/**
	 * Submits multiple {@code Lines} to be rendered.
	 *
	 * @param points        the points
	 * @param color         the color
	 * @param lineWidth     the line width
	 * @param throughBlocks if rendering can be done through blocks
	 */
	void submitLines(Vec3 @NotNull [] points, @NotNull Color color, float lineWidth, boolean throughBlocks);

	/**
	 * Submits a {@code Line} from the cursor to the given point.
	 *
	 * @param point     the point
	 * @param color     the color
	 * @param lineWidth the line width
	 */
	void submitLineFromCursor(@NotNull Vec3 point, @NotNull Color color, float lineWidth);

	/**
	 * Submits a cuboid outline.
	 *
	 * @param center      the center
	 * @param depth       the depth
	 * @param size        the size
	 * @param minY        the minY
	 * @param maxY        the maxY
	 * @param lineWidth   the line width
	 * @param mainColor   the main color
	 * @param secondColor the second color
	 */
	void submitCuboidOutline(@NotNull Vec3 center, int depth, int size, int minY, int maxY, float lineWidth, @NotNull Color mainColor, @NotNull Color secondColor);
}
