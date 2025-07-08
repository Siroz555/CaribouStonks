package fr.siroz.cariboustonks.core.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import fr.siroz.cariboustonks.CaribouStonks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * The {@code JsonFileService} class provides utility methods to save and load objects to/from JSON files.
 */
public final class JsonFileService {

	private final Gson gson;

	@ApiStatus.Internal
	public JsonFileService() {
		this.gson = GsonProvider.prettyPrinting();
	}

	/**
	 * {@code Tests}
	 *
	 * @param gson gson
	 */
	@ApiStatus.Internal
	public JsonFileService(@NotNull Gson gson) {
		this.gson = gson;
	}

	/**
	 * Loads an object from a JSON file.
	 * <p>
	 * This method deserializes a JSON file into an object of the specified type.
	 * The object must be simple types (e.g., String, Integer) or have a registered
	 * {@code TypeAdapter} to be processed correctly.
	 *
	 * @param path  the path to the JSON file to load the object from
	 * @param clazz the class type of the object to load
	 * @param <T>   the type of the object to load
	 * @return the loaded object of the specified type, or {@code null} if the file does not exist
	 */
	public <T> @Nullable T load(@NotNull Path path, @NotNull Class<T> clazz) {
		if (Files.notExists(path)) {
			return null;
		}

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			return gson.fromJson(reader, clazz);
		} catch (JsonParseException | IOException ex) {
			CaribouStonks.LOGGER.error("[JsonFileService] Unable to load file: {}", path, ex);
			return null;
		}
	}

	/**
	 * Saves an object to a JSON file.
	 * <p>
	 * This method serializes an object to a JSON file.
	 * The object must be simple types (e.g., String, Integer) or have a registered
	 * {@code TypeAdapter} to be processed correctly.
	 *
	 * @param path   the path where the object should be saved
	 * @param object the object to save to the file
	 */
	public void save(@NotNull Path path, @NotNull Object object) {
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			gson.toJson(object, writer);
		} catch (IOException ex) {
			CaribouStonks.LOGGER.error("[JsonFileService] Unable to save file: {}", path, ex);
			throw new JsonProcessingException("Error occurred while saving the list to file: " + path, ex);
		}
	}

	/**
	 * @deprecated {@link #loadList(Path, Class)}
	 */
	@Deprecated
	public <T> @NotNull List<T> loadList(@NotNull Path path, @NotNull Function<JsonObject, T> deserializer) {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
			List<T> objects = new ArrayList<>();
			for (JsonElement element : jsonArray) {
				JsonObject objJson = element.getAsJsonObject();
				T obj = deserializer.apply(objJson);
				if (obj != null) {
					objects.add(obj);
				}
			}
			return objects;
		} catch (NoSuchFileException ignored) {
		} catch (JsonParseException | IOException ex) {
			CaribouStonks.LOGGER.error("[JsonFileService] Unable to load list from file: {}", path, ex);
		}
		return Collections.emptyList();
	}

	/**
	 * Loads a list of objects from a JSON file.
	 * <p>
	 * This method deserializes a JSON file into an object list of the specified type.
	 * The objects in the list must either be simple types (e.g., String, Integer)
	 * or have a registered {@code TypeAdapter} to be processed correctly.
	 *
	 * @param path  the path to the JSON file to load the list from
	 * @param clazz the class type of the objects in the list to load
	 * @param <T>   the type of the objects in the list
	 * @return a loaded objects list of the specified type, or an empty list if the file does not exist
	 */
	public <T> @NotNull List<T> loadList(@NotNull Path path, @NotNull Class<T> clazz) {
		if (Files.notExists(path)) {
			return Collections.emptyList();
		}

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			Type listType = TypeToken.getParameterized(List.class, clazz).getType();
			return gson.fromJson(reader, listType);
		} catch (IOException | JsonSyntaxException ex) {
			CaribouStonks.LOGGER.error("[JsonFileService] Unable to load list from file: {}", path, ex);
			throw new JsonProcessingException("Error occurred while loading the list from file: " + path, ex);
		}
	}

	/**
	 * Loads a map of objects from a JSON file.
	 * <p>
	 * Have a registered {@code TypeAdapter} to be processed correctly.
	 *
	 * @param path      the path to the JSON file to load the map from
	 * @param typeOfMap the typeOfMap
	 * @param <K>       the type of the keys in the map
	 * @param <V>       the type of the values in the map
	 * @return a loaded objects map of the specified key and value types, or an empty map if the file does not exist
	 */
	public <K, V> @NotNull Map<K, V> loadMap(@NotNull Path path, @NotNull Type typeOfMap) {
		if (Files.notExists(path)) {
			return Collections.emptyMap();
		}

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			return gson.fromJson(reader, typeOfMap);
		} catch (IOException | JsonSyntaxException ex) {
			CaribouStonks.LOGGER.error("[JsonFileService] Unable to load map from file: {}", path, ex);
			throw new JsonProcessingException("Error occurred while loading the map from file: " + path, ex);
		}
	}

	/**
	 * Loads a map of objects from a JSON file.
	 * <p>
	 * Both the key and value types must either be simple types (e.g., String, Integer)
	 * or have a registered {@code TypeAdapter} to be processed correctly.
	 *
	 * @param path       the path to the JSON file to load the map from
	 * @param keyClass   the class type of the keys in the map
	 * @param valueClass the class type of the values in the map
	 * @param <K>        the type of the keys in the map
	 * @param <V>        the type of the values in the map
	 * @return a loaded objects map of the specified key and value types, or an empty map if the file does not exist
	 */
	public <K, V> @NotNull Map<K, V> loadMap(@NotNull Path path, @NotNull Class<K> keyClass, @NotNull Class<V> valueClass) {
		if (Files.notExists(path)) {
			return Collections.emptyMap();
		}

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			Type mapType = TypeToken.getParameterized(Map.class, keyClass, valueClass).getType();
			return gson.fromJson(reader, mapType);
		} catch (IOException | JsonSyntaxException ex) {
			CaribouStonks.LOGGER.error("[JsonFileService] Unable to load map from file: {}", path, ex);
			throw new JsonProcessingException("Error occurred while loading the map from file: " + path, ex);
		}
	}
}
