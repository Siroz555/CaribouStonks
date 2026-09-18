package fr.siroz.cariboustonks.platform.rendering.world;

import fr.siroz.cariboustonks.util.render.RenderUtils;
import java.util.List;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;

// SIROZ-NOTE: Pour le futur Vulkan/OpenGL (Actuellement les 2 Graphics API sont supportés)
//
// Pas de condition sur l'API graphique dans RenderDispatcher.
// dispatch() -> XRendererCommand.FACTORY a la place de XRendererCommand.Submit::new
//
// private static final SubmitFactory<FilledBoxRenderState> FILLED_BOX_FACTORY =
// 		SubmitFactory.resolve(FilledBoxInstancedRendererCommand.Submit::new, FilledBoxRendererCommand.Submit::new);
// .

/**
 * Factory of {@link SubmitNode}
 *
 * @param <S> the type of render states
 * @param <N> the type of submit node
 */
@FunctionalInterface
public interface SubmitFactory<S, N extends SubmitNode> {
	N create(List<S> states, CameraRenderState cameraState, boolean throughBlocks);

	/**
	 * Resolve the correct SubmitFactory Node depending on the Graphics API Backend.
	 *
	 * @param vulkanFactory the Vulkan Submit implementation
	 * @param openGlFactory the OpenGL Submit implementation
	 * @param <S>           the type of render States
	 * @param <N>           the type of SubmitNode
	 * @return the SubmitFactory Node instance
	 */
	@Deprecated
	static <S, N extends SubmitNode> SubmitFactory<S, N> resolve(SubmitFactory<S, N> vulkanFactory, SubmitFactory<S, N> openGlFactory) {
		return RenderUtils.isVulkanBackend() ? vulkanFactory : openGlFactory;
	}
}
