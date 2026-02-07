package fr.siroz.cariboustonks.rendering.world.renderer;

import net.minecraft.client.renderer.state.CameraRenderState;
import org.jspecify.annotations.NonNull;

/**
 * Represents a command that can be emitted to the renderer.
 *
 * @param <T> the type of the state to render
 */
interface RendererCommand<T> {

	/**
	 * Emits the given state to the renderer.
	 *
	 * @param state  the state to render
	 * @param camera the camera state
	 */
	void emit(@NonNull T state, @NonNull CameraRenderState camera);
}
