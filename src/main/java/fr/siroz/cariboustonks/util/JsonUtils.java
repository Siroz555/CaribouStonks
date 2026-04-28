package fr.siroz.cariboustonks.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Safe Json {@code Gson} utility.
 */
public final class JsonUtils {

	private JsonUtils() {
	}

	/**
	 * Returns the nested {@link JsonObject} for the given key
	 *
	 * @param parent the parent
	 * @param key    the key
	 * @return the nested {@link JsonObject}, or {@code null} if absent or not an object.
	 */
	public static @Nullable JsonObject getObject(@Nullable JsonObject parent, @NotNull String key) {
		if (parent == null) return null;
		JsonElement element = parent.get(key);
		return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
	}

	/**
	 * Returns the {@link JsonArray} for the given key
	 *
	 * @param parent the parent
	 * @param key    the key
	 * @return the {@link JsonArray}, or {@code null} if absent or not an array.
	 */
	public static @Nullable JsonArray getArray(@Nullable JsonObject parent, @NotNull String key) {
		if (parent == null) return null;
		JsonElement element = parent.get(key);
		return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
	}

	/**
	 * Returns the {@link String} value for the given key
	 *
	 * @param parent the parent
	 * @param key    the key
	 * @return the {@link String} value, or {@code null} if absent.
	 */
	public static @Nullable Instant getInstant(@Nullable JsonObject parent, @NotNull String key) {
		if (parent == null) return null;
		JsonElement element = parent.get(key);
		return element != null && element.isJsonPrimitive() ? Instant.parse(element.getAsString()) : null;
	}

	/**
	 * Returns the {@link String} value for the given key
	 *
	 * @param parent the parent
	 * @param key    the key
	 * @return the {@link String} value, or {@code null} if absent.
	 */
	public static @Nullable String getString(@Nullable JsonObject parent, @NotNull String key) {
		if (parent == null) return null;
		JsonElement element = parent.get(key);
		return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
	}

	/**
	 * Returns the {@link String} value for the given key or the default given value
	 *
	 * @param parent       the parent
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the {@link String} value, or {@code defaultValue} if absent.
	 */
	public static @NotNull String getStringOrDefault(@Nullable JsonObject parent, @NotNull String key, @NotNull String defaultValue) {
		String value = getString(parent, key);
		return value != null ? value : defaultValue;
	}

	/**
	 * Returns an {@link OptionalInt} for the given key
	 *
	 * @param parent the parent
	 * @param key    the key
	 * @return an {@link OptionalInt}, or empty if absent.
	 */
	public static @NotNull OptionalInt getOptionalInt(@Nullable JsonObject parent, @NotNull String key) {
		if (parent == null) return OptionalInt.empty();
		JsonElement element = parent.get(key);
		return element != null && element.isJsonPrimitive() ? OptionalInt.of(element.getAsInt()) : OptionalInt.empty();
	}

	/**
	 * Returns an {@link OptionalDouble} for the given key
	 *
	 * @param parent the parent
	 * @param key    the key
	 * @return an {@link OptionalInt}, or empty if absent.
	 */
	public static @NotNull OptionalDouble getOptionalDouble(@Nullable JsonObject parent, @NotNull String key) {
		if (parent == null) return OptionalDouble.empty();
		JsonElement element = parent.get(key);
		return element != null && element.isJsonPrimitive() ? OptionalDouble.of(element.getAsInt()) : OptionalDouble.empty();
	}
}
