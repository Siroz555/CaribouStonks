package fr.siroz.cariboustonks.platform;

import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.platform.context.WorldContext;
import fr.siroz.cariboustonks.platform.rendering.CaribouRenderPipelines;
import fr.siroz.cariboustonks.platform.rendering.gui.GuiRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.CaribouWorldRenderer;

/**
 * Central access point for Minecraft-facing runtime contexts.
 */
public final class MinecraftService {

	// SIROZ-NOTE: Avoir GuiRenderer et WorldRenderer en accès final
	//  au lieu d'avoir des static actuellement.
	//  Remettre le system de contexts entre client/player/world maybe
	//  mais coté feature c'est le bordel, redesign une arch plus tard

	private MinecraftService() {
	}

	/**
	 * Init
	 */
	public static void bootstrap() {
		// Bootstrap contexts
		ClientContext.bootstrap();
		PlayerContext.bootstrap();
		WorldContext.bootstrap();
		// Bootstrap rendering
		CaribouWorldRenderer.bootstrap();
		GuiRenderer.bootstrap();
		CaribouRenderPipelines.bootstrap();
	}
}
