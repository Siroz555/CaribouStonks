package fr.siroz.cariboustonks.system;

import fr.siroz.cariboustonks.core.component.EntityGlowComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.system.System;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the registration and application of glow colors for entities (and blocks?).
 * <p>
 * This manager collects {@link EntityGlowComponent} (for now) implementations provided
 * by features and uses them to compute and cache glow colors.
 */
public final class GlowingSystem implements System {

	private final Map<Feature, EntityGlowComponent> registeredComponents = new HashMap<>();
	private final Object2IntMap<Entity> cachedEntities = new Object2IntOpenHashMap<>();

	@ApiStatus.Internal
	public GlowingSystem() {
		TickScheduler.getInstance().runRepeating(this.cachedEntities::clear, 1, TimeUnit.SECONDS);
	}

	@Override
	public void register(@NotNull Feature feature) {
		feature.getComponent(EntityGlowComponent.class)
				.ifPresent(component ->  registeredComponents.put(feature, component));
	}

	/**
	 * Retrieves the cached glow color for the given entity,
	 * or returns the provided default color if none is cached.
	 *
	 * @param entity       the entity
	 * @param defaultColor the color to return if no color is cached
	 * @return the cached glow color or {@link fr.siroz.cariboustonks.core.component.EntityGlowComponent.EntityGlowStrategy#DEFAULT} if not available
	 */
	public int getEntityColorOrDefault(@Nullable Entity entity, int defaultColor) {
		if (entity == null) return defaultColor;
		return cachedEntities.getOrDefault(entity, defaultColor);
	}

	/**
	 * Checks whether a glow color for the given entity is already cached or attempts to compute
	 * and cache it if not. A non-default color will be cached and cause this method to return {@code true}.
	 *
	 * @param entity the entity to check or compute for
	 * @return {@code true} if a non-default glow color is cached or computed
	 */
	public boolean hasOrComputeEntity(@Nullable Entity entity) {
		if (entity == null) return false;
		if (cachedEntities.containsKey(entity)) return true;

		int color = computeEntity(entity);
		if (color != EntityGlowComponent.EntityGlowStrategy.DEFAULT) {
			cachedEntities.put(entity, color);
			return true;
		}

		return false;
	}

	/**
	 * Iterates through all registered and enabled providers to compute the glow color for the given entity.
	 * The first non-default color returned by a provider is used.
	 */
	private int computeEntity(@NotNull Entity entity) {
		for (Map.Entry<Feature, EntityGlowComponent> entry : registeredComponents.entrySet()) {
			if (entry.getKey().isEnabled()) {
				int glowColor = entry.getValue().getGlowColor(entity);
				if (glowColor != EntityGlowComponent.EntityGlowStrategy.DEFAULT) {
					return glowColor;
				}
			}
		}
		return EntityGlowComponent.EntityGlowStrategy.DEFAULT;
	}
}
