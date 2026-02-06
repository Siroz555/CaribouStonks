package fr.siroz.cariboustonks.config;

import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;

/**
 * Lazy configuration supplier that provides type-safe config access.
 *
 * @param <T> the configuration value type
 */
public final class ConfigValue<T> {

	private final Supplier<T> supplier;

	private ConfigValue(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	/**
	 * Gets the current configuration value.
	 *
	 * @return the current value
	 */
	public T get() {
		return supplier.get();
	}

	/**
	 * Gets the current value or returns a default if null.
	 *
	 * @param defaultValue the default value
	 * @return the current value or default
	 */
	public T getOrDefault(T defaultValue) {
		T value = get();
		return value != null ? value : defaultValue;
	}

	/**
	 * Creates a config value supplier.
	 *
	 * @param supplier the value supplier
	 * @param <T>      the value type
	 * @return a new config value
	 */
	@NonNull
	public static <T> ConfigValue<T> of(Supplier<T> supplier) {
		return new ConfigValue<>(supplier);
	}
}
