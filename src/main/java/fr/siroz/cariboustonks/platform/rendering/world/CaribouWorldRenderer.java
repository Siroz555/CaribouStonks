package fr.siroz.cariboustonks.platform.rendering.world;

import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.platform.context.WorldContext;
import fr.siroz.cariboustonks.platform.mixin.accessors.BlockEntityRenderStateAccessor;
import fr.siroz.cariboustonks.platform.rendering.world.renderer.BeamFeatureRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.renderer.CircleFeatureRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.renderer.CuboidOutlineFeatureRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.renderer.CursorLineFeatureRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.renderer.FilledBoxFeatureRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.renderer.LinesFeatureRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.renderer.OutlineBoxFeatureRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.renderer.QuadFeatureRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.renderer.TextFeatureRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.renderer.TextureFeatureRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.renderer.ThickCircleFeatureRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.state.BeamRenderState;
import fr.siroz.cariboustonks.platform.rendering.world.state.CircleRenderState;
import fr.siroz.cariboustonks.platform.rendering.world.state.CuboidOutlineRenderState;
import fr.siroz.cariboustonks.platform.rendering.world.state.CursorLineRenderState;
import fr.siroz.cariboustonks.platform.rendering.world.state.FilledBoxRenderState;
import fr.siroz.cariboustonks.platform.rendering.world.state.LinesRenderState;
import fr.siroz.cariboustonks.platform.rendering.world.state.OutlineBoxRenderState;
import fr.siroz.cariboustonks.platform.rendering.world.state.QuadRenderState;
import fr.siroz.cariboustonks.platform.rendering.world.state.TextRenderState;
import fr.siroz.cariboustonks.platform.rendering.world.state.TextureRenderState;
import fr.siroz.cariboustonks.platform.rendering.world.state.ThickCircleRenderState;
import fr.siroz.cariboustonks.util.render.AnimationUtils;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhase;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Point d'entrée unique pour gérer les rendu du Mod, sous facade static.
 * Permettant le câblage des hooks Fabric/Mixin et de "gérer" le coté worker stateful du dispatcher.
 * <p>
 * L'Event Fabric LevelRenderEvents.COLLECT_SUBMITS est utilisé pour dispatch les
 * FeatureRenderer du Mod, pas d'utilisation de LevelExtractionEvents pour éviter
 * une surcouche de listeners qui ne sert à rien, tant que le Mixin suit entre version de MC.
 */
public final class CaribouWorldRenderer {
	private static final WorldRendererImpl worldRenderer = new WorldRendererImpl();

	private static final SubmitRenderPhase<SubmitNode> NORMAL_PHASE
			= new SubmitRenderPhase<>(x -> x.afterTerrain);

	private static final SubmitRenderPhase<TranslucentSubmit> SEE_THROUGH_PHASE
			= new SubmitRenderPhase<>(x -> x.seeThrough);

	private static final SubmitRenderPhase<SubmitNode> TEXTS_PHASE
			= new SubmitRenderPhase<>(x -> x.texts);

	private CaribouWorldRenderer() {
	}

	/**
	 * Init
	 */
	public static void bootstrap() {
		LevelRenderEvents.COLLECT_SUBMITS.register(CaribouWorldRenderer::dispatchSubmits);

		FeatureRendererRegistry.register(BeamFeatureRenderer.TYPE, BeamFeatureRenderer::new);
		FeatureRendererRegistry.register(CircleFeatureRenderer.TYPE, CircleFeatureRenderer::new);
		FeatureRendererRegistry.register(CuboidOutlineFeatureRenderer.TYPE, CuboidOutlineFeatureRenderer::new);
		FeatureRendererRegistry.register(CursorLineFeatureRenderer.TYPE, CursorLineFeatureRenderer::new);
		FeatureRendererRegistry.register(FilledBoxFeatureRenderer.TYPE, FilledBoxFeatureRenderer::new);
		FeatureRendererRegistry.register(LinesFeatureRenderer.TYPE, LinesFeatureRenderer::new);
		FeatureRendererRegistry.register(OutlineBoxFeatureRenderer.TYPE, OutlineBoxFeatureRenderer::new);
		FeatureRendererRegistry.register(QuadFeatureRenderer.TYPE, QuadFeatureRenderer::new);
		FeatureRendererRegistry.register(TextFeatureRenderer.TYPE, TextFeatureRenderer::new);
		FeatureRendererRegistry.register(TextureFeatureRenderer.TYPE, TextureFeatureRenderer::new);
		FeatureRendererRegistry.register(ThickCircleFeatureRenderer.TYPE, ThickCircleFeatureRenderer::new);
	}

	/**
	 * >>> <b>MIXIN</b> <<<
	 *
	 * @param levelRenderState levelRenderState
	 * @param frustum          frustum
	 */
	public static void extract(LevelRenderState levelRenderState, Frustum frustum) {
		worldRenderer.begin(levelRenderState, frustum);
		RenderEvents.WORLD_RENDER_EVENT.invoker().onWorldRender(worldRenderer);
		worldRenderer.end();
	}

	private static void dispatchSubmits(LevelRenderContext context) {
		worldRenderer.dispatch(context.levelState().cameraRenderState, context.submitNodeCollector());
	}

	/**
	 * Implementation of {@link WorldRenderer}.
	 */
	static final class WorldRendererImpl implements WorldRenderer {
		private final RenderBucket<BeamRenderState> beamStates = new RenderBucket<>();
		private final RenderBucket<TextRenderState> textStates = new RenderBucket<>();
		private final RenderBucket<TextureRenderState> textureStates = new RenderBucket<>();
		private final RenderBucket<CircleRenderState> circleStates = new RenderBucket<>();
		private final RenderBucket<ThickCircleRenderState> thickCircleStates = new RenderBucket<>();
		private final RenderBucket<QuadRenderState> quadStates = new RenderBucket<>();
		private final RenderBucket<FilledBoxRenderState> filledBoxStates = new RenderBucket<>();
		private final RenderBucket<OutlineBoxRenderState> outlineBoxStates = new RenderBucket<>();
		private final RenderBucket<LinesRenderState> linesStates = new RenderBucket<>();
		private final List<CursorLineRenderState> cursorLineStates = new ArrayList<>();
		private final List<CuboidOutlineRenderState> cuboidOutlineStates = new ArrayList<>();

		private @Nullable LevelRenderState levelRenderState = null;
		private @Nullable Frustum frustum = null;
		private boolean frozen = false;

		@Override
		public void submitVanillaBeaconBeam(@NonNull BlockPos position, @NonNull Color color) {
			if (frozen) return;
			if (levelRenderState == null) return;
			if (!RenderUtils.isVisible(frustum, position.getX(), position.getY(), position.getZ(), position.getX() + 1, RenderUtils.MAX_BUILD_HEIGHT, position.getZ() + 1)) return;

			int colorInt;
			if (color == Colors.RAINBOW) {
				colorInt = AnimationUtils.getCurrentRainbowColor().withAlpha(1f).asInt();
			} else {
				colorInt = color.withAlpha(1f).asInt();
			}

			float length = (float) RenderUtils.getCamera().position().subtract(Vec3.atCenterOf(position)).horizontalDistance();
			float animationTime = Math.floorMod(WorldContext.getWorldTime(), 40) + RenderUtils.getTickCounter().getGameTimeDeltaPartialTick(true);

			BeaconRenderState state = new BeaconRenderState();
			state.blockPos = position;
			((BlockEntityRenderStateAccessor) state).setBlockState(Blocks.BEACON.defaultBlockState());
			state.blockEntityType = BlockEntityTypes.BEACON;
			state.lightCoords = RenderUtils.FULL_BRIGHT;
			state.breakProgress = null;
			state.animationTime = animationTime;
			state.sections.add(new BeaconRenderState.Section(colorInt, RenderUtils.MAX_BUILD_HEIGHT));
			state.beamRadiusScale = Math.max(1.0F, length / 96.0F);
			// Vanilla Block Entity States
			levelRenderState.blockEntityRenderStates.add(state);
		}

		@Override
		public void submitBeam(@NonNull Vec3 pos, @NonNull Color color, float height, float widthScale, boolean throughBlocks) {
			if (frozen) return;

			if (color == Colors.RAINBOW) {
				color = AnimationUtils.getCurrentRainbowColor();
			}

			BeamRenderState state = new BeamRenderState(pos, color, height, widthScale, throughBlocks);
			beamStates.add(state, throughBlocks);
		}

		@Override
		public void submitText(@NonNull FormattedCharSequence text, @NonNull Vec3 position, float scale, float offsetY, boolean throughBlocks) {
			if (frozen) return;

			Font textRenderer = Minecraft.getInstance().font;
			float offsetX = -textRenderer.width(text) / 2f;
			Font.PreparedText preparedText = textRenderer.prepareText(text, offsetX, offsetY, 0xFFFFFFFF, false, false, 0);

			TextRenderState state = new TextRenderState(preparedText, position, scale * 0.025f, offsetY, throughBlocks);
			textStates.add(state, throughBlocks);
		}

		@Override
		public void submitTexture(@NonNull Vec3 position, float width, float height, float u, float v, float textureWidth, float textureHeight, @NonNull Vec3 renderOffset, @NonNull Identifier texture, @NonNull Color color, float alpha, boolean throughBlocks) {
			if (frozen) return;

			TextureRenderState state = new TextureRenderState(position, width, height, u, v, textureWidth, textureHeight, renderOffset, texture, color, alpha, throughBlocks);
			textureStates.add(state, throughBlocks);
		}

		@Override
		public void submitCircle(@NonNull Vec3 center, double radius, int segments, float thicknessPercent, @NonNull Color color, Direction.@NonNull Axis axis, boolean throughBlocks) {
			if (frozen) return;

			CircleRenderState state = new CircleRenderState(center, radius, segments, thicknessPercent, color, axis, throughBlocks);
			circleStates.add(state, throughBlocks);
		}

		@Override
		public void submitThickCircle(@NonNull Vec3 center, double radius, double thickness, int segments, @NonNull Color color, boolean throughBlocks) {
			if (frozen) return;

			ThickCircleRenderState state = new ThickCircleRenderState(center, radius, thickness, segments, color, throughBlocks);
			thickCircleStates.add(state, throughBlocks);
		}

		@Override
		public void submitQuad(@NonNull Vec3[] points, @NonNull Color color, boolean throughBlocks) {
			if (frozen) return;

			QuadRenderState state = new QuadRenderState(points, color, throughBlocks);
			quadStates.add(state, throughBlocks);
		}

		@Override
		public void submitFilled(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, @NonNull Color color, boolean throughBlocks) {
			if (frozen) return;
			if (!RenderUtils.isVisible(frustum, minX, minY, minZ, maxX, maxY, maxZ)) return;

			if (color == Colors.RAINBOW) {
				int colorInt = AnimationUtils.getCurrentRainbowColor().withAlpha(1f).asInt();
				color = Color.fromInt(colorInt);
			}

			FilledBoxRenderState state = new FilledBoxRenderState(minX, minY, minZ, maxX, maxY, maxZ, color, throughBlocks);
			filledBoxStates.add(state, throughBlocks);
		}

		@Override
		public void submitOutline(@NonNull AABB box, @NonNull Color color, float lineWidth, boolean throughBlocks) {
			if (frozen) return;
			if (!RenderUtils.isVisible(frustum, box)) return;

			OutlineBoxRenderState state = new OutlineBoxRenderState(box, color, lineWidth, throughBlocks);
			outlineBoxStates.add(state, throughBlocks);
		}

		@Override
		public void submitLines(Vec3 @NonNull [] points, @NonNull Color color, float lineWidth, boolean throughBlocks) {
			if (frozen) return;
			if (points.length < 2) return;

			LinesRenderState state = new LinesRenderState(points, color, lineWidth, throughBlocks);
			linesStates.add(state, throughBlocks);
		}

		@Override
		public void submitLineFromCursor(@NonNull Vec3 point, @NonNull Color color, float lineWidth) {
			if (frozen) return;

			CursorLineRenderState state = new CursorLineRenderState(point, color, lineWidth);
			cursorLineStates.add(state);
		}

		@Override
		public void submitCuboidOutline(@NonNull Vec3 center, int depth, int size, int minY, int maxY, float lineWidth, @NonNull Color mainColor, @NonNull Color secondColor) {
			if (frozen) return;

			CuboidOutlineRenderState state = new CuboidOutlineRenderState(center, depth, size, minY, maxY, lineWidth, mainColor, secondColor);
			cuboidOutlineStates.add(state);
		}

		/**
		 * Resets the renderer.
		 */
		public void begin(LevelRenderState levelRenderStateContext, Frustum frustumContext) {
			frozen = false;
			levelRenderState = levelRenderStateContext;
			frustum = frustumContext;

			beamStates.clear();
			circleStates.clear();
			filledBoxStates.clear();
			linesStates.clear();
			outlineBoxStates.clear();
			quadStates.clear();
			textStates.clear();
			textureStates.clear();
			thickCircleStates.clear();
			cursorLineStates.clear();
			cuboidOutlineStates.clear();
		}

		/**
		 * Freezes the renderer
		 */
		public void end() {
			frozen = true;
		}

		/**
		 * Dispatch all submits.
		 *
		 * @param camera    the camera state
		 * @param collector the submitNoteCollector
		 */
		public void dispatch(CameraRenderState camera, SubmitNodeCollector collector) {
			if (!frozen) return;

			// States avec 2 flags (normal && throughBlocks)

			submitPair(collector, beamStates, camera, NORMAL_PHASE, BeamFeatureRenderer.Submit::new);
			submitPair(collector, circleStates, camera, NORMAL_PHASE, CircleFeatureRenderer.Submit::new);
			submitPair(collector, thickCircleStates, camera, NORMAL_PHASE, ThickCircleFeatureRenderer.Submit::new);
			submitPair(collector, quadStates, camera, NORMAL_PHASE, QuadFeatureRenderer.Submit::new);
			submitPair(collector, filledBoxStates, camera, NORMAL_PHASE, FilledBoxFeatureRenderer.Submit::new);
			submitPair(collector, outlineBoxStates, camera, NORMAL_PHASE, OutlineBoxFeatureRenderer.Submit::new);
			submitPair(collector, linesStates, camera, NORMAL_PHASE, LinesFeatureRenderer.Submit::new);
			submitPair(collector, textStates, camera, TEXTS_PHASE, TextFeatureRenderer.Submit::new);
			submitPair(collector, textureStates, camera, NORMAL_PHASE, TextureFeatureRenderer.Submit::new);

			// States avec 1 flag (normal || throughBlocks)

			if (!cursorLineStates.isEmpty()) {
				collector.submitCustom(SEE_THROUGH_PHASE, new CursorLineFeatureRenderer.Submit(cursorLineStates, camera));
			}
			if (!cuboidOutlineStates.isEmpty()) {
				collector.submitCustom(NORMAL_PHASE, new CuboidOutlineFeatureRenderer.Submit(cuboidOutlineStates, camera));
			}
		}

		private <S, T extends TranslucentSubmit> void submitPair(
				@NonNull SubmitNodeCollector collector,
				@NonNull RenderBucket<S> bucket,
				@NonNull CameraRenderState cameraState,
				@NonNull SubmitRenderPhase<? super T> normalPhase,
				@NonNull SubmitFactory<S, T> factory
		) {
			if (!bucket.normal.isEmpty()) {
				collector.submitCustom(normalPhase, factory.create(bucket.normal, cameraState, false));
			}
			if (!bucket.throughBlocks.isEmpty()) {
				collector.submitCustom(SEE_THROUGH_PHASE, factory.create(bucket.throughBlocks, cameraState, true));
			}
		}

		/**
		 * Represents a Bucket of normal/throughBlocks render States
		 *
		 * @param <S> the rendering state type
		 */
		private static final class RenderBucket<S> {
			final List<S> normal = new ArrayList<>();
			final List<S> throughBlocks = new ArrayList<>();

			void add(@NonNull S state, boolean throughBlocksFlag) {
				if (throughBlocksFlag) throughBlocks.add(state);
				else normal.add(state);
			}

			void clear() {
				normal.clear();
				throughBlocks.clear();
			}
		}
	}
}
