package fr.siroz.cariboustonks.system.glowing;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to be implemented by features that provide a glow color for {@link Entity}.
 */
public interface EntityGlowProvider {

	/**
	 * The default glow color, used when no custom provider is registered
	 * or when the provider explicitly returns this value.
	 */
	int DEFAULT = 0;

	/**
	 * Computes the glow color for the specified entity.
	 *
	 * @param entity the {@link Entity} for which to determine the glow color
	 * @return an integer representing the glow color to apply, or {@link #DEFAULT}
	 */
	int getEntityGlowColor(@NotNull Entity entity);
}
