package fr.siroz.cariboustonks.core.skyblock.data.hypixel.fetcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.service.json.GsonProvider;
import fr.siroz.cariboustonks.core.service.scheduler.AsyncScheduler;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.ElectionResult;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.Mayor;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.Perk;
import fr.siroz.cariboustonks.util.http.Http;
import fr.siroz.cariboustonks.util.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Fetches and caches {@code Hypixel SkyBlock Election resource}.
 * <p>
 * <a href="hhttps://api.hypixel.net/v2/resources/skyblock/election">Hypixel API - Resources - Election</a>
 * <p>
 * Periodically triggers an asynchronous fetch of the election endpoint and stores the parsed {@link ElectionResult}.
 */
public final class ElectionFetcher {

	private static final String ELECTION_URL = "https://api.hypixel.net/v2/resources/skyblock/election";

	private static final Duration FIRST_RETRY_DELAY = Duration.ofMinutes(1);
	private static final int MAX_RETRIES = 10;

	private final AtomicBoolean fetchInProgress;
	private final AtomicInteger retryAttempts;
	@Nullable
	private volatile ElectionResult cachedElection;

	public ElectionFetcher() {
		this.fetchInProgress = new AtomicBoolean(false);
		this.retryAttempts = new AtomicInteger(0);
		this.cachedElection = null;
	}

	/**
	 * Trigger an initial fetch and attach a post-fetch logging callback.
	 */
	public void start() {
		triggerFetch(false).thenRun(afterFetch());
	}

	/**
	 * Return the last cached {@link ElectionResult} or {@code null} if none.
	 *
	 * @return the cached ElectionResult or null
	 */
	public @Nullable ElectionResult getCachedElection() {
		return cachedElection;
	}

	/**
	 * Trigger an asynchronous fetch of the SkyBlock election resource.
	 * <p>
	 * On failure a retry is scheduled with an exponential-ish backoff until {@code MAX_RETRIES} is reached.
	 *
	 * @param force if true, force a fetch even if one is already in progress
	 * @return a CompletableFuture that completes when the attempt finishes; the future completes normally
	 * whether the attempt succeeded or failed (failure triggers retry scheduling).
	 */
	private CompletableFuture<Void> triggerFetch(boolean force) {
		if (!force && !fetchInProgress.compareAndSet(false, true)) {
			CaribouStonks.LOGGER.warn("[ElectionFetcher] Skipping fetch, already in progress");
			return CompletableFuture.completedFuture(null);
		}

		CompletableFuture<Void> promise = CompletableFuture.runAsync(
				this::executeFetch,
				AsyncScheduler.getInstance().blockingExecutor()
		);

		promise = promise.exceptionallyCompose(throwable -> {
			Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
			CaribouStonks.LOGGER.error("[ElectionFetcher] Fetch mayor failed (attempt {}). Cause: {}", retryAttempts.get(), cause);

			int attemptsSoFar = retryAttempts.getAndIncrement();
			if (attemptsSoFar >= MAX_RETRIES) {
				CaribouStonks.LOGGER.error("[ElectionFetcher] Max retries reached, aborting fetch");
				fetchInProgress.set(false);
			} else {
				long minutes = FIRST_RETRY_DELAY.toMinutes() << attemptsSoFar;
				CaribouStonks.LOGGER.warn("[ElectionFetcher] Retrying mayor fetch in {} minutes (attempt {}/{})", minutes, attemptsSoFar + 1, MAX_RETRIES);
				TickScheduler.getInstance().runLater(() -> triggerFetch(true).thenRun(afterFetch()), (int) minutes, TimeUnit.MINUTES);
			}
			return CompletableFuture.completedFuture(null);
		});

		promise = promise.whenComplete((v, t) -> fetchInProgress.set(false));
		return promise;
	}

	/**
	 * Perform the blocking HTTP request and parse the election resource reply.
	 *
	 * @throws RuntimeException if the request fails or the reply is invalid
	 */
	private void executeFetch() {
		try (HttpResponse response = Http.request(ELECTION_URL)) {
			if (!response.success()) {
				throw new RuntimeException("Hypixel API returned an error code: " + response.statusCode() + " cause: " + response.content());
			}

			String body = response.content();
			if (body == null || body.isBlank()) {
				throw new RuntimeException("SkyBlock API Election returned null or blank reply");
			}

			JsonObject root = JsonParser.parseString(body).getAsJsonObject();
			if (!root.has("success") || !root.get("success").getAsBoolean()) {
				throw new RuntimeException("SkyBlock API Election failed");
			}

			JsonObject mayorJson = GsonProvider.safeGetAsObject(root, "mayor");
			// Le minister n'est pas dans le root mais bien dans le mayor object
			JsonObject ministerJson = GsonProvider.safeGetAsObject(mayorJson, "minister");

			Mayor mayor = parseMayor(mayorJson);
			Mayor minister = parseMayor(ministerJson);
			Set<Perk> mayorPerks = parseMayorPerks(mayorJson);
			Optional<Perk> ministerPerk = parseMinisterPerk(ministerJson);

			cachedElection = new ElectionResult(mayor, minister, mayorPerks, ministerPerk, Instant.now());
			retryAttempts.set(0);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private @NonNull Mayor parseMayor(@Nullable JsonObject mayorObject) {
		if (mayorObject == null) return Mayor.UNKNOWN;
		try {
			String key = mayorObject.has("key") ? mayorObject.get("key").getAsString() : "";
			return Mayor.fromId(key);
		} catch (Exception ignored) {
			return Mayor.UNKNOWN;
		}
	}

	private @NonNull Set<Perk> parseMayorPerks(@Nullable JsonObject mayorObj) {
		if (mayorObj == null) return Set.of();
		try {
			Set<Perk> perks = new HashSet<>();
			if (mayorObj.has("perks") && !mayorObj.get("perks").isJsonNull()) {
				JsonArray mayorPerks = mayorObj.getAsJsonArray("perks");
				for (JsonElement element : mayorPerks) {
					if (element.isJsonObject()) {
						JsonObject json = element.getAsJsonObject();
						String name = json.has("name") ? json.get("name").getAsString() : "";
						perks.add(Perk.fromDisplayName(name));
					}
				}
			}
			return perks;
		} catch (Exception ignored) {
			return Set.of();
		}
	}

	private @NonNull Optional<Perk> parseMinisterPerk(@Nullable JsonObject ministerObj) {
		if (ministerObj == null) return Optional.empty();
		try {
			if (ministerObj.has("perk") && !ministerObj.get("perk").isJsonNull()) {
				JsonElement element = ministerObj.get("perk");
				if (element.isJsonObject()) {
					JsonObject json = element.getAsJsonObject();
					String name = json.has("name") ? json.get("name").getAsString() : "";
					return Optional.of(Perk.fromDisplayName(name));
				}
			}
		} catch (Exception ignored) {
		}
		return Optional.empty();
	}

	/**
	 * Call-back runnable executed after a fetch attempt completes
	 */
	private @NonNull Runnable afterFetch() {
		return () -> {
			if (cachedElection == null) {
				CaribouStonks.LOGGER.warn("[ElectionFetcher] No election result yet");
				return;
			}

			ElectionResult result = cachedElection;
			if (result != null) {
				CaribouStonks.LOGGER.info("[ElectionFetcher] Current Election: Mayor: {}, Minister: {}",
						result.mayor().name(), result.minister().name());
			}
		};
	}
}
