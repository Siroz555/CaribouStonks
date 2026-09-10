package fr.siroz.cariboustonks.platform.rendering.world;

import fr.siroz.cariboustonks.events.RenderEvents;
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
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.LevelRenderState;
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
	private static @Nullable RenderDispatcher renderDispatcher = null;

	private CaribouWorldRenderer() {
	}

	/**
	 * Init
	 */
	public static void bootstrap() {
		renderDispatcher = new RenderDispatcher();

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
		if (renderDispatcher == null) return;

		renderDispatcher.begin(levelRenderState, frustum);
		RenderEvents.WORLD_RENDER_EVENT.invoker().onWorldRender(renderDispatcher);
		renderDispatcher.end();
	}

	private static void dispatchSubmits(LevelRenderContext context) {
		if (renderDispatcher == null) return;

		renderDispatcher.dispatch(context.levelState().cameraRenderState, context.submitNodeCollector());
	}
}
