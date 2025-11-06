package fr.siroz.cariboustonks.rendering;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRendererImpl;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.state.WorldRenderState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class CaribouRenderer {

	private final WorldRendererImpl worldRenderer;

	@ApiStatus.Internal
	public CaribouRenderer() {
		if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != CaribouStonks.class) {
			throw new RuntimeException("Noo noo and noo");
		}
		// Mod Implementation
		CaribouRenderPipelines.init();
		this.worldRenderer = new WorldRendererImpl();
	}

	public void startExtraction(Frustum frustum) {
		if (worldRenderer == null) return;

		worldRenderer.begin(frustum);
		RenderEvents.WORLD_RENDER.invoker().onWorldRender(worldRenderer);
		worldRenderer.end();
	}

	public void executeDraws(WorldRenderState worldRenderState) {
		if (worldRenderer == null) return;

		worldRenderer.flush(worldRenderState.cameraRenderState);

		Renderer.getInstance().executeDraws();
	}
}
