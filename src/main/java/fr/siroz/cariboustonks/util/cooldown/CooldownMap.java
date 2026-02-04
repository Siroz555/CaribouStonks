package fr.siroz.cariboustonks.util.cooldown;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

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
	@NotNull
	static <T> CooldownMap<T> create(@NotNull Cooldown base) {
		return new CooldownMapImpl<>(base);
	}

	/**
	 * Gets the base cooldown.
	 *
	 * @return the base cooldown
	 */
	@NotNull Cooldown getBase();

	/**
	 * Gets the internal cooldown instance associated with the given key.
	 *
	 * @param key the key
	 * @return a cooldown instance
	 */
	@NotNull Cooldown get(@NotNull T key);

	/**
	 * Puts the given {@link T} type with the associated cooldown in the collection.
	 *
	 * @param key      the key
	 * @param cooldown the cooldown
	 */
	void put(@NotNull T key, @NotNull Cooldown cooldown);

	/**
	 * Gets all cooldowns contained in the collection.
	 *
	 * @return the backing map
	 */
	@NotNull Map<T, Cooldown> getAll();

	default boolean test(@NotNull T key) {
		return get(key).test();
	}

	default boolean testSilently(@NotNull T key) {
		return get(key).testSilently();
	}

	default void reset(@NotNull T key) {
		get(key).reset();
	}
}
