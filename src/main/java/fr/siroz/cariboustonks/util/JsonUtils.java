package fr.siroz.cariboustonks.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
}
