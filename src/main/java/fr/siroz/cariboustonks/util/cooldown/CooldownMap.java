package fr.siroz.cariboustonks.util.cooldown;

import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * A self-populating map of cooldown instances
 *
 * @param <T> the type
 */
public interface CooldownMap<T> {

	/**
	 * Creates a new collection with the given cooldown defined by the base instance.
	 *
	 * @param base the cooldown to base off
	 * @return a new collection
	 */
	@NonNull
	static <T> CooldownMap<T> create(@NonNull Cooldown base) {
		return new CooldownMapImpl<>(base);
	}

	/**
	 * Gets the base cooldown.
	 *
	 * @return the base cooldown
	 */
	@NonNull Cooldown getBase();

	/**
	 * Gets the internal cooldown instance associated with the given key.
	 *
	 * @param key the key
	 * @return a cooldown instance
	 */
	@NonNull Cooldown get(@NonNull T key);

	/**
	 * Puts the given {@link T} type with the associated cooldown in the collection.
	 *
	 * @param key      the key
	 * @param cooldown the cooldown
	 */
	void put(@NonNull T key, @NonNull Cooldown cooldown);

	/**
	 * Gets all cooldowns contained in the collection.
	 *
	 * @return the backing map
	 */
	@NonNull Map<T, Cooldown> getAll();

	default boolean test(@NonNull T key) {
		return get(key).test();
	}

	default boolean testSilently(@NonNull T key) {
		return get(key).testSilently();
	}

	default void reset(@NonNull T key) {
		get(key).reset();
	}
}
