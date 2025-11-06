package fr.siroz.cariboustonks.rendering.world.renderer;

import net.minecraft.client.render.state.CameraRenderState;
import org.jetbrains.annotations.NotNull;

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
	void emit(@NotNull T state, @NotNull CameraRenderState camera);
}
