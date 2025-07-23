package fr.siroz.cariboustonks.util.render.world;

import fr.siroz.cariboustonks.util.render.WorldRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

/**
 * Une commande de rendu unit une série de paramètres et sait s'exécuter dans un contexte donné.
 */
public interface RenderCommand {

	/**
	 * Exécuté par {@link WorldRenderer#flush()}
	 *
	 * @param ctx le WorldRenderContext
	 */
	void execute(WorldRenderContext ctx);
}
