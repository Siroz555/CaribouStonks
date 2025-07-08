package fr.siroz.cariboustonks.util.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

/**
 * An interface representing a world renderer that allows implementing classes to perform custom rendering logic
 * during the world rendering process.
 */
public interface WorldRenderer {

    /**
	 * Performs custom rendering logic during the world rendering process.
	 *
	 * @param context WorldRenderContext
	 */
	void render(WorldRenderContext context);
}
