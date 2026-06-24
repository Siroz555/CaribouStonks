package fr.siroz.cariboustonks.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
	public static @Nullable JsonObject getObject(@Nullable JsonObject parent, @NonNull String key) {
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
	public static @Nullable JsonArray getArray(@Nullable JsonObject parent, @NonNull String key) {
		if (parent == null) return null;
		JsonElement element = parent.get(key);
		return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
	}

	/**
	 * Checks if at least one of the keys exists and is not {@link JsonNull}.
	 *
	 * @param parent the parent
	 * @param keys   the keys
	 * @return {@code true} if at least one of the keys exists and is not JsonNull.
	 */
	public static boolean has(@Nullable JsonObject parent, @NonNull String... keys) {
		if (parent == null) return false;
		if (keys.length == 0) return false;

		if (keys.length == 1) {
			JsonElement element = parent.get(keys[0]);
			return element != null && !element.isJsonNull();
		}

		for (String key : keys) {
			JsonElement element = parent.get(key);
			if (element != null && !element.isJsonNull()) return true;
		}
		return false;
	}

	/**
	 * Returns the {@link String} value for the given key
	 *
	 * @param parent the parent
	 * @param key    the key
	 * @return the {@link String} value, or {@code null} if absent.
	 */
	public static @Nullable Instant getInstant(@Nullable JsonObject parent, @NonNull String key) {
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
	public static @Nullable String getString(@Nullable JsonObject parent, @NonNull String key) {
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
	public static @NonNull String getStringOrDefault(@Nullable JsonObject parent, @NonNull String key, @NonNull String defaultValue) {
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
	public static @NonNull OptionalInt getOptionalInt(@Nullable JsonObject parent, @NonNull String key) {
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
	public static @NonNull OptionalDouble getOptionalDouble(@Nullable JsonObject parent, @NonNull String key) {
		if (parent == null) return OptionalDouble.empty();
		JsonElement element = parent.get(key);
		return element != null && element.isJsonPrimitive() ? OptionalDouble.of(element.getAsInt()) : OptionalDouble.empty();
	}
}
