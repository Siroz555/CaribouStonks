package fr.siroz.cariboustonks.platform.context;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides a view of the current client-side ({@code ClientLevel}) world.
 * <p>
 * Implementations are responsible for handling the case where the world is not yet available.
 * Every method is guaranteed to return a safe default or return {@code null}.
 */
public final class WorldContext {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private WorldContext() {
	}

	/**
	 * Init
	 */
	public static void bootstrap() {
	}

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
	public static boolean isAvailable() {
		return CLIENT.level != null;
	}

	/**
	 * Retrieves the {@code Day} of the current world.
	 * <p>
	 * {@code Safe World null}
	 *
	 * @return the day of the current world
	 */
	public static long getWorldDay() {
		return isAvailable() ? CLIENT.level.getOverworldClockTime() / 24000 : 0L;
	}

	/**
	 * Retrieves the {@code World Time} of the current world.
	 * <p>
	 * {@code Safe World null}
	 *
	 * @return the world time of the current world
	 */
	public static long getWorldTime() {
		return isAvailable() ? CLIENT.level.getGameTime() : 0;
	}

	/**
	 * Returns a {@link Iterable} view of {@link Entity} currently rendered in the world
	 *
	 * @return an Iterable view
	 */
	public static @NonNull Iterable<Entity> getEntities() {
		return isAvailable() ? CLIENT.level.entitiesForRendering() : Collections.emptyList();
	}

	/**
	 * Returns a List of {@link Entity}s close to the except, according to the given conditions.
	 * <p>
	 * {@code Safe Client null} & {@code Safe World null}
	 *
	 * @param except          the entity except
	 * @param boundingBox     the bounding box
	 * @param entityPredicate the entity predicate (e.g. {@code Entity::hasCustomName})
	 * @return a List of {@link Entity}s close to the target if the conditions are met, otherwise an empty list
	 */
	public static List<Entity> getEntities(@Nullable Entity except, @NonNull AABB boundingBox, @NonNull Predicate<? super Entity> entityPredicate) {
		if (!isAvailable()) return Collections.emptyList();

		return CLIENT.level.getEntities(except, boundingBox, entityPredicate);
	}

	/**
	 * Returns a List of {@link Entity}s according to the given conditions.
	 *
	 * @param type            the entityType test
	 * @param boundingBox     the bounding box
	 * @param entityPredicate the entity predicate (e.g. {@code Entity::hasCustomName})
	 * @param <T>             the entityTyPE
	 * @return a List of {@link Entity}s if the conditions are met, otherwise an empty list
	 */
	public static <T extends Entity> List<T> getEntities(@NonNull EntityTypeTest<Entity, T> type, @NonNull AABB boundingBox, @NonNull Predicate<? super T> entityPredicate) {
		if (!isAvailable()) return Collections.emptyList();

		return CLIENT.level.getEntities(type, boundingBox, entityPredicate);
	}

	/**
	 * Returns a List of {@link Entity}s close to the player, according to the given conditions.
	 *
	 * @param entity           the entity class type
	 * @param distanceInBlocks the distance in blocks from the player's position
	 * @param entityPredicate  the entity predicate (e.g. {@code Entity::hasCustomName})
	 * @param <T>              the entity class type
	 * @return a List of {@link Entity}s close to the player if the conditions are met, otherwise an empty list
	 */
	public static <T extends Entity> List<T> findClosestEntities(@NonNull Class<T> entity, double distanceInBlocks, @NonNull Predicate<? super T> entityPredicate) {
		if (CLIENT.player == null || !isAvailable()) return Collections.emptyList();

		return CLIENT.level.getEntitiesOfClass(entity, CLIENT.player.getBoundingBox().inflate(distanceInBlocks), entityPredicate);
	}

	/**
	 * Returns the {@link Entity} closest to the player, according to the given conditions.
	 *
	 * @param entity           the entity class type
	 * @param distanceInBlocks the distance in blocks from the player's position
	 * @param entityPredicate  the entity predicate (e.g. {@code Entity::hasCustomName})
	 * @param filterPredicate  the filter predicate (e.g. {@code e -> "King Minos".equals(e.getName().getString())})
	 * @param <T>              the entity class type
	 * @return the closest entity to the player if the conditions are met, otherwise null
	 */
	public static <T extends Entity> @Nullable T findClosestEntity(@NonNull Class<T> entity, double distanceInBlocks, @NonNull Predicate<? super T> entityPredicate, @NonNull Predicate<? super T> filterPredicate) {
		if (CLIENT.player == null || !isAvailable()) return null;

		List<T> entities = findClosestEntities(entity, distanceInBlocks, entityPredicate);
		return findClosestEntity(entities, filterPredicate);
	}

	/**
	 * Returns the {@link Entity} closest to the player, according to the given conditions.
	 *
	 * @param entities        the entities list
	 * @param filterPredicate the filter predicate (e.g. {@code e -> "King Minos".equals(e.getName().getString())})
	 * @param <T>             the entity class type
	 * @return the closest entity to the player if the conditions are met, otherwise null
	 */
	public static <T extends Entity> @Nullable T findClosestEntity(@NonNull List<T> entities, @NonNull Predicate<? super T> filterPredicate) {
		if (CLIENT.player == null || !isAvailable()) return null;
		if (entities.isEmpty()) return null;

		return entities.stream()
				.filter(filterPredicate)
				.min(Comparator.comparingDouble(as -> as.distanceToSqr(CLIENT.player)))
				.orElse(null);
	}
}
