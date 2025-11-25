package fr.siroz.cariboustonks.rendering.world;

import fr.siroz.cariboustonks.rendering.world.renderer.BeaconBeamRendererCommand;
import fr.siroz.cariboustonks.rendering.world.renderer.CircleRendererCommand;
import fr.siroz.cariboustonks.rendering.world.renderer.CuboidOutlineRendererCommand;
import fr.siroz.cariboustonks.rendering.world.renderer.CursorLineRendererCommand;
import fr.siroz.cariboustonks.rendering.world.renderer.FilledBoxRendererCommand;
import fr.siroz.cariboustonks.rendering.world.renderer.LinesRendererCommand;
import fr.siroz.cariboustonks.rendering.world.renderer.OutlineBoxRendererCommand;
import fr.siroz.cariboustonks.rendering.world.renderer.QuadRendererCommand;
import fr.siroz.cariboustonks.rendering.world.renderer.TextRendererCommand;
import fr.siroz.cariboustonks.rendering.world.renderer.TextureRendererCommand;
import fr.siroz.cariboustonks.rendering.world.renderer.ThickCircleRendererCommand;
import fr.siroz.cariboustonks.rendering.world.state.BeaconBeamRenderState;
import fr.siroz.cariboustonks.rendering.world.state.CircleRenderState;
import fr.siroz.cariboustonks.rendering.world.state.CuboidOutlineRenderState;
import fr.siroz.cariboustonks.rendering.world.state.CursorLineRenderState;
import fr.siroz.cariboustonks.rendering.world.state.FilledBoxRenderState;
import fr.siroz.cariboustonks.rendering.world.state.LinesRenderState;
import fr.siroz.cariboustonks.rendering.world.state.OutlineBoxRenderState;
import fr.siroz.cariboustonks.rendering.world.state.QuadRenderState;
import fr.siroz.cariboustonks.rendering.world.state.TextRenderState;
import fr.siroz.cariboustonks.rendering.world.state.TextureRenderState;
import fr.siroz.cariboustonks.rendering.world.state.ThickCircleRenderState;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.FrustumUtils;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import fr.siroz.cariboustonks.util.render.animation.AnimationUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link WorldRenderer}.
 */
@ApiStatus.Internal
public final class WorldRendererImpl implements WorldRenderer {
	// Commands
	private final TextRendererCommand textRendererCommand = new TextRendererCommand();
	private final TextureRendererCommand textureRendererCommand = new TextureRendererCommand();
	private final CircleRendererCommand circleRendererCommand = new CircleRendererCommand();
	private final ThickCircleRendererCommand thickCircleRendererCommand = new ThickCircleRendererCommand();
	private final QuadRendererCommand quadRendererCommand = new QuadRendererCommand();
	private final FilledBoxRendererCommand filledBoxRendererCommand = new FilledBoxRendererCommand();
	private final BeaconBeamRendererCommand beaconBeamRendererCommand = new BeaconBeamRendererCommand();
	private final OutlineBoxRendererCommand outlineBoxRendererCommand = new OutlineBoxRendererCommand();
	private final LinesRendererCommand linesRendererCommand = new LinesRendererCommand();
	private final CursorLineRendererCommand cursorLineRendererCommand = new CursorLineRendererCommand();
	private final CuboidOutlineRendererCommand cuboidOutlineRendererCommand = new CuboidOutlineRendererCommand();
	// States
	private final List<TextRenderState> textRenderStates = new ArrayList<>();
	private final List<TextureRenderState> textureRenderStates = new ArrayList<>();
	private final List<CircleRenderState> circleRenderStates = new ArrayList<>();
	private final List<ThickCircleRenderState> thickCircleRenderStates = new ArrayList<>();
	private final List<QuadRenderState> quadRenderStates = new ArrayList<>();
	private final List<FilledBoxRenderState> filledBoxRenderStates = new ArrayList<>();
	private final List<BeaconBeamRenderState> beaconBeamRenderStates = new ArrayList<>();
	private final List<OutlineBoxRenderState> outlineBoxRenderStates = new ArrayList<>();
	private final List<LinesRenderState> linesRenderStates = new ArrayList<>();
	private final List<CursorLineRenderState> cursorLineRenderStates = new ArrayList<>();
	private final List<CuboidOutlineRenderState> cuboidOutlineRenderStates = new ArrayList<>();

	private boolean frozen = false;
	private Frustum frustum = null;

	public WorldRendererImpl() {
	}

	@Override
	public void submitText(@NotNull FormattedCharSequence text, @NotNull Vec3 position, float scale, float offsetY, boolean throughBlocks) {
		if (frozen) return;

		Font textRenderer = Minecraft.getInstance().font;
		float offsetX = -textRenderer.width(text) / 2f;
		Font.PreparedText preparedText = textRenderer.prepareText(text, offsetX, offsetY, 0xFFFFFFFF, false, false, 0);

		TextRenderState state = new TextRenderState(preparedText, position, scale * 0.025f, offsetY, throughBlocks);
		textRenderStates.add(state);
	}

	@Override
	public void submitTexture(@NotNull Vec3 position, float width, float height, float textureWidth, float textureHeight, @NotNull Vec3 renderOffset, @NotNull Identifier texture, @NotNull Color color, float alpha, boolean throughBlocks) {
		if (frozen) return;

		TextureRenderState state = new TextureRenderState(position, width, height, textureWidth, textureHeight, renderOffset, texture, color, alpha, throughBlocks);
		textureRenderStates.add(state);
	}

	@Override
	public void submitCircle(@NotNull Vec3 center, double radius, int segments, float thicknessPercent, @NotNull Color color, Direction.@NotNull Axis axis, boolean throughBlocks) {
		if (frozen) return;

		CircleRenderState state = new CircleRenderState(center, radius, segments, thicknessPercent, color, axis, throughBlocks);
		circleRenderStates.add(state);
	}

	@Override
	public void submitThickCircle(@NotNull Vec3 center, double radius, double thickness, int segments, @NotNull Color color, boolean throughBlocks) {
		if (frozen) return;

		ThickCircleRenderState state = new ThickCircleRenderState(center, radius, thickness, segments, color, throughBlocks);
		thickCircleRenderStates.add(state);
	}

	@Override
	public void submitQuad(@NotNull Vec3[] points, @NotNull Color color, boolean throughBlocks) {
		if (frozen) return;

		QuadRenderState state = new QuadRenderState(points, color, throughBlocks);
		quadRenderStates.add(state);
	}

	@Override
	public void submitFilled(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, @NotNull Color color, boolean throughBlocks) {
		if (frozen) return;
		if (!FrustumUtils.isVisible(frustum, minX, minY, minZ, maxX, maxY, maxZ)) return;

		if (color == Colors.RAINBOW) {
			int colorInt = AnimationUtils.getCurrentRainbowColor().withAlpha(1f).asInt();
			color = Color.fromInt(colorInt);
		}

		FilledBoxRenderState state = new FilledBoxRenderState(minX, minY, minZ, maxX, maxY, maxZ, color, throughBlocks);
		filledBoxRenderStates.add(state);
	}

	@Override
	public void submitBeaconBeam(@NotNull BlockPos position, @NotNull Color color) {
		if (frozen) return;
		if (!FrustumUtils.isVisible(frustum, position.getX(), position.getY(), position.getZ(), position.getX() + 1, RenderUtils.MAX_BUILD_HEIGHT, position.getZ() + 1)) return;

		int colorInt;
		if (color == Colors.RAINBOW) {
			colorInt = AnimationUtils.getCurrentRainbowColor().withAlpha(1f).asInt();
		} else {
			colorInt = color.withAlpha(1f).asInt();
		}

		float length = (float) RenderUtils.getCamera().position().subtract(position.getCenter()).horizontalDistance();
		float scale = Math.max(1.0f, length / 96.0f);
		float beamRotationDegrees = Math.floorMod(Client.getWorldTime(), 40) + RenderUtils.getTickCounter().getGameTimeDeltaPartialTick(true);

		BeaconBeamRenderState state = new BeaconBeamRenderState(position, colorInt, scale, beamRotationDegrees);
		beaconBeamRenderStates.add(state);
	}

	@Override
	public void submitOutline(@NotNull AABB box, @NotNull Color color, float lineWidth, boolean throughBlocks) {
		if (frozen) return;
		if (!FrustumUtils.isVisible(frustum, box)) return;

		OutlineBoxRenderState state = new OutlineBoxRenderState(box, color, lineWidth, throughBlocks);
		outlineBoxRenderStates.add(state);
	}

	@Override
	public void submitLines(Vec3 @NotNull [] points, @NotNull Color color, float lineWidth, boolean throughBlocks) {
		if (frozen) return;
		if (points.length < 2) return;

		LinesRenderState state = new LinesRenderState(points, color, lineWidth, throughBlocks);
		linesRenderStates.add(state);
	}

	@Override
	public void submitLineFromCursor(@NotNull Vec3 point, @NotNull Color color, float lineWidth) {
		if (frozen) return;

		CursorLineRenderState state = new CursorLineRenderState(point, color, lineWidth);
		cursorLineRenderStates.add(state);
	}

	@Override
	public void submitCuboidOutline(@NotNull Vec3 center, int depth, int size, int minY, int maxY, float lineWidth, @NotNull Color mainColor, @NotNull Color secondColor) {
		if (frozen) return;

		CuboidOutlineRenderState state = new CuboidOutlineRenderState(center, depth, size, minY, maxY, lineWidth, mainColor, secondColor);
		cuboidOutlineRenderStates.add(state);
	}

	/**
	 * Resets the renderer.
	 */
	public void begin(Frustum frustumExtracted) {
		frozen = false;
		frustum = frustumExtracted;
		textRenderStates.clear();
		textureRenderStates.clear();
		circleRenderStates.clear();
		thickCircleRenderStates.clear();
		quadRenderStates.clear();
		filledBoxRenderStates.clear();
		beaconBeamRenderStates.clear();
		outlineBoxRenderStates.clear();
		linesRenderStates.clear();
		cursorLineRenderStates.clear();
		cuboidOutlineRenderStates.clear();
	}

	/**
	 * Freezes the renderer
	 */
	public void end() {
		frozen = true;
	}

	/**
	 * Flush all renderer commands.
	 *
	 * @param cameraState the camera state
	 */
	public void flush(CameraRenderState cameraState) {
		if (!frozen) return;
		// Circles
		for (CircleRenderState state : circleRenderStates) {
			circleRendererCommand.emit(state, cameraState);
		}
		// Thick circles
		for (ThickCircleRenderState state : thickCircleRenderStates) {
			thickCircleRendererCommand.emit(state, cameraState);
		}
		// Quads
		for (QuadRenderState state : quadRenderStates) {
			quadRendererCommand.emit(state, cameraState);
		}
		// Filled
		for (FilledBoxRenderState state : filledBoxRenderStates) {
			filledBoxRendererCommand.emit(state, cameraState);
		}
		// Beacon beams
		for (BeaconBeamRenderState state : beaconBeamRenderStates) {
			beaconBeamRendererCommand.emit(state, cameraState);
		}
		// Outline boxes
		for (OutlineBoxRenderState state : outlineBoxRenderStates) {
			outlineBoxRendererCommand.emit(state, cameraState);
		}
		// Lines
		for (LinesRenderState state : linesRenderStates) {
			linesRendererCommand.emit(state, cameraState);
		}
		// Cursor lines
		for (CursorLineRenderState state : cursorLineRenderStates) {
			cursorLineRendererCommand.emit(state, cameraState);
		}
		// Text
		for (TextRenderState state : textRenderStates) {
			textRendererCommand.emit(state, cameraState);
		}
		// Textures
		for (TextureRenderState state : textureRenderStates) {
			textureRendererCommand.emit(state, cameraState);
		}
		// Cuboid Outline
		for (CuboidOutlineRenderState state : cuboidOutlineRenderStates) {
			cuboidOutlineRendererCommand.emit(state, cameraState);
		}
	}
}
