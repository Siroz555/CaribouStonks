package fr.siroz.cariboustonks.platform.api;

import fr.siroz.cariboustonks.platform.MinecraftService;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides a view of the current client-side world.
 * <p>
 * This interface abstracts all access to the active world related data within the mod.
 * Features must never import or reference Minecraft's {@code ClientLevel} directly.
 * <p>
 * Implementations are responsible for handling the case where the world is not yet available.
 * Every method is guaranteed to return a safe default or return {@code null}.
 *
 * @see MinecraftService#world()
 */
public interface WorldContext {

	/**
	 * Checks if the {@code ClientLevel} is available.
	 * <p>
	 * Identified with:
	 * <pre>
	 *     Minecraft.getInstance().level != null
	 * </pre>
	 *
	 * @return {@code true} if available
	 */
	boolean isAvailable();

	/**
	 * Retrieves the {@code Day} of the current world.
	 * <p>
	 * {@code Safe World null}
	 *
	 * @return the day of the current world
	 */
	long getWorldDay();

	/**
	 * Retrieves the {@code World Time} of the current world.
	 * <p>
	 * {@code Safe World null}
	 *
	 * @return the world time of the current world
	 */
	long getWorldTime();

	/**
	 * Returns a {@link Iterable} view of {@link Entity} currently rendered in the world
	 *
	 * @return an Iterable view
	 */
	@NonNull Iterable<Entity> getEntities();

	/**
	 * Returns a List of {@link Entity}s close to the player, according to the given conditions.
	 * <p>
	 * {@code Safe Client null} & {@code Safe World null}
	 *
	 * @param entity           the entity class type
	 * @param distanceInBlocks the distance in blocks from the player's position
	 * @param entityPredicate  the entity predicate (e.g. {@code Entity::hasCustomName})
	 * @param <T>              the entity class type
	 * @return a List of {@link Entity}s close to the player if the conditions are met, otherwise an empty list
	 */
	<T extends Entity> List<T> findClosestEntities(
			@NonNull Class<T> entity,
			double distanceInBlocks,
			@NonNull Predicate<? super T> entityPredicate
	);

	/**
	 * Returns the {@link Entity} closest to the player, according to the given conditions.
	 * <p>
	 * {@code Safe Client null} & {@code Safe World null}
	 *
	 * @param entity           the entity class type
	 * @param distanceInBlocks the distance in blocks from the player's position
	 * @param entityPredicate  the entity predicate (e.g. {@code Entity::hasCustomName})
	 * @param filterPredicate  the filter predicate (e.g. {@code e -> "King Minos".equals(e.getName().getString())})
	 * @param <T>              the entity class type
	 * @return the closest entity to the player if the conditions are met, otherwise null
	 */
	<T extends Entity> @Nullable T findClosestEntity(
			@NonNull Class<T> entity,
			double distanceInBlocks,
			@NonNull Predicate<? super T> entityPredicate,
			@NonNull Predicate<? super T> filterPredicate
	);

	/**
	 * Returns the {@link Entity} closest to the player, according to the given conditions.
	 * <p>
	 * {@code Safe Client null} & {@code Safe World null}
	 *
	 * @param entities        the entities list
	 * @param filterPredicate the filter predicate (e.g. {@code e -> "King Minos".equals(e.getName().getString())})
	 * @param <T>             the entity class type
	 * @return the closest entity to the player if the conditions are met, otherwise null
	 */
	<T extends Entity> @Nullable T findClosestEntity(
			@NonNull List<T> entities,
			@NonNull Predicate<? super T> filterPredicate
	);
}
