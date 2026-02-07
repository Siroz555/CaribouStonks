package fr.siroz.cariboustonks.core.service.json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The {@code JsonFileService} class provides utility methods to save and load objects to/from JSON files.
 */
public final class JsonFileService {

	private static JsonFileService instance;

	private final Gson gson;

	private JsonFileService() {
		this.gson = GsonProvider.prettyPrinting();
	}

	/**
	 * Returns the singleton instance of the {@code JsonFileService} class.
	 *
	 * @return the singleton instance of {@code JsonFileService}
	 */
	public static JsonFileService get() {
		return instance == null ? instance = new JsonFileService() : instance;
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
	 * @throws JsonProcessingException if an error occurs while loading the object
	 */
	public <T> @Nullable T load(@NonNull Path path, @NonNull Class<T> clazz) throws JsonProcessingException {
		if (Files.notExists(path)) {
			return null;
		}

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			return gson.fromJson(reader, clazz);
		} catch (JsonParseException | IOException ex) {
			throw new JsonProcessingException("Error occurred while loading from file", ex);
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
	 * @throws JsonProcessingException if an error occurs while saving the object
	 */
	public void save(@NonNull Path path, @NonNull Object object) throws JsonProcessingException {
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			gson.toJson(object, writer);
		} catch (IOException ex) {
			throw new JsonProcessingException("Error occurred while saving the list to file: " + path, ex);
		}
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
	 * @throws JsonProcessingException if an error occurs while loading the list
	 */
	public <T> @NonNull List<T> loadList(@NonNull Path path, @NonNull Class<T> clazz) throws JsonProcessingException {
		if (Files.notExists(path)) {
			return Collections.emptyList();
		}

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			Type listType = TypeToken.getParameterized(List.class, clazz).getType();
			return gson.fromJson(reader, listType);
		} catch (IOException | JsonSyntaxException ex) {
			throw new JsonProcessingException("Error occurred while loading the list from file", ex);
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
	 * @throws JsonProcessingException if an error occurs while loading the map
	 */
	public <K, V> @NonNull Map<K, V> loadMap(@NonNull Path path, @NonNull Type typeOfMap) throws JsonProcessingException {
		if (Files.notExists(path)) {
			return Collections.emptyMap();
		}

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			return gson.fromJson(reader, typeOfMap);
		} catch (IOException | JsonSyntaxException ex) {
			throw new JsonProcessingException("Error occurred while loading the map from file", ex);
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
	 * @throws JsonProcessingException if an error occurs while loading the map
	 */
	public <K, V> @NonNull Map<K, V> loadMap(@NonNull Path path, @NonNull Class<K> keyClass, @NonNull Class<V> valueClass) throws JsonProcessingException {
		if (Files.notExists(path)) {
			return Collections.emptyMap();
		}

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			Type mapType = TypeToken.getParameterized(Map.class, keyClass, valueClass).getType();
			return gson.fromJson(reader, mapType);
		} catch (IOException | JsonSyntaxException ex) {
			throw new JsonProcessingException("Error occurred while loading the map from file", ex);
		}
	}
}
