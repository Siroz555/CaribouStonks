package fr.siroz.cariboustonks.rendering;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRendererImpl;
import fr.siroz.cariboustonks.rendering.world.state.CameraRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

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
		// TODO - remove - 1.21.10
		registerListeners();
	}

	private void registerListeners() {
		// Fabric API -> Mod Rendering
		WorldRenderEvents.AFTER_SETUP.register(this::startExtraction);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(this::executeDraws);
	}

	public void startExtraction(WorldRenderContext context) {
		if (worldRenderer == null) return;

		worldRenderer.begin();
		RenderEvents.WORLD_RENDER.invoker().onWorldRender(worldRenderer);
		worldRenderer.end();
	}

	public void executeDraws(@NotNull WorldRenderContext context) {
		if (worldRenderer == null) return;

		CameraRenderState cameraState = new CameraRenderState(
				context.camera().getPos(),
				new Quaternionf(context.camera().getRotation()),
				context.camera().getPitch(),
				context.camera().getYaw()
		);

		worldRenderer.flush(cameraState);

		Renderer.getInstance().executeDraws();
	}
}
