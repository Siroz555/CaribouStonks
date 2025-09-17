package fr.siroz.cariboustonks.core.json;

/**
 * Exception thrown when an error occurs during JSON processing from {@link JsonFileService}.
 */
public class JsonProcessingException extends Exception {

	/**
	 * New instance of {@link JsonProcessingException}.
	 *
	 * @param message the error message
	 * @param cause   the cause of the error
	 */
	public JsonProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
}
