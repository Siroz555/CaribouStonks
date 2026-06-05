package fr.siroz.cariboustonks.platform.rendering.world.renderer;

import java.util.List;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jspecify.annotations.NonNull;

/**
 * Represents a command that can be emitted to the renderer using Vulkan Backend
 *
 * @param <T> the type of the state to render
 */
@Deprecated // SIROZ-NOTE :: Vulkan GPU Backend + RenderDispatcher single/multi handler?
interface VulkanRendererCommand<T> {

	/**
	 * Emits the given state to the renderer.
	 *
	 * @param states the states to render
	 * @param camera the camera state
	 */
	void emit(@NonNull List<T> states, @NonNull CameraRenderState camera);
}
