package fr.siroz.cariboustonks.util.http;

import fr.siroz.cariboustonks.CaribouStonks;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utilitaire pour manipuler les requests {@code HTTP} avec l'API {@code java.net.http}.
 */
public final class Http {

	private static final String USER_AGENT = "CaribouStonks/"
			+ CaribouStonks.VERSION.getFriendlyString()
			+ " (" + SharedConstants.getCurrentVersion().name() + ")";

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	private Http() {
	}

	/**
	 * {@code GET} -> {@code url} avec {@link HttpResponse}.
	 * <p>
	 * <ul><li>Accept : application/json</li></ul>
	 * <ul><li>Accept-Encoding : gzip, deflate</li></ul>
	 * <ul><li>User-Agent : {@link #USER_AGENT}</li></ul>
	 * <ul><li>HTTP Version : 2</li></ul>
	 *
	 * @param url url
	 * @return {@link HttpResponse}
	 * @throws RuntimeException pour toute erreur HTTP (hors réponse) ou IO
	 */
	@Contract("_ -> new")
	public static @NotNull HttpResponse request(@NotNull String url) throws RuntimeException {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.GET()
					.header("Accept", "application/json")
					.header("Accept-Encoding", "gzip, deflate")
					.header("User-Agent", USER_AGENT)
					.version(HttpClient.Version.HTTP_2)
					.uri(URI.create(url))
					.build();

			java.net.http.HttpResponse<InputStream> response = HTTP_CLIENT.send(request, BodyHandlers.ofInputStream());
			InputStream decodedInputStream = getDecodedInputStream(response);
			String body = new String(decodedInputStream.readAllBytes());

			return new HttpResponse(response.statusCode(), body);
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * {@code GET} -> {@code url} avec {@link InputStream}.
	 * <p>
	 * <ul><li>Accept : * / *</li></ul>
	 * <ul><li>Accept-Encoding : gzip, deflate</li></ul>
	 * <ul><li>User-Agent : {@link #USER_AGENT}</li></ul>
	 * <ul><li>HTTP Version : 2</li></ul>
	 *
	 * @param url                  url
	 * @param expectedContentTypes les {@code Content-Type} attendus
	 * @return {@link InputStream}
	 * @throws RuntimeException pour toute erreur HTTP ou IO
	 */
	public static InputStream requestGeneric(
			@NotNull String url,
			@NotNull Set<String> expectedContentTypes
	) throws RuntimeException {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.GET()
					.header("Accept", "*/*")
					.header("Accept-Encoding", "gzip, deflate")
					.header("User-Agent", USER_AGENT)
					.version(HttpClient.Version.HTTP_2)
					.uri(URI.create(url))
					.build();

			java.net.http.HttpResponse<InputStream> response = HTTP_CLIENT.send(request, BodyHandlers.ofInputStream());

			int statusCode = response.statusCode();
			if (statusCode != 200) {
				throw new IllegalStateException("Bad response received! Code: " + statusCode);
			}

			String contentType = response.headers().firstValue("Content-Type").orElse("");
			if (!expectedContentTypes.contains(contentType)) {
				throw new IllegalStateException("Unexpected content type received! Content Type: " + contentType);
			}

			return getDecodedInputStream(response);
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Décode une {@link java.net.http.HttpResponse} en un {@link InputStream} selon le {@code Content-Encoding} reçu.
	 *
	 * @param response {@link java.net.http.HttpResponse}
	 * @return {@link InputStream}
	 */
	private static InputStream getDecodedInputStream(java.net.http.@NotNull HttpResponse<InputStream> response) {
		String encoding = response.headers().firstValue("Content-Encoding").orElse("");

		try {
			return switch (encoding) {
				case "" -> response.body();
				case "gzip" -> new GZIPInputStream(response.body());
				case "deflate" -> new InflaterInputStream(response.body());
				default -> throw new UnsupportedOperationException(
						"The server sent content in an unexpected encoding: " + encoding);
			};
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
