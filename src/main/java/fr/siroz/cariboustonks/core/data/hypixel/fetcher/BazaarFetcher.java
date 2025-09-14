package fr.siroz.cariboustonks.core.data.hypixel.fetcher;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.Product;
import fr.siroz.cariboustonks.core.json.GsonProvider;
import fr.siroz.cariboustonks.core.scheduler.AsyncScheduler;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.http.Http;
import fr.siroz.cariboustonks.util.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.apache.http.client.HttpResponseException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Fetches and caches {@code Hypixel SkyBlock Bazaar products}.
 * <p>
 * <a href="https://api.hypixel.net/v2/skyblock/bazaar">Hypixel API - Bazaar</a>
 * <p>
 * Periodically triggers an asynchronous fetch of the Bazaar endpoint and stores an immutable snapshot
 * of the {@link Product} objects.
 */
@ApiStatus.Internal
public final class BazaarFetcher {

	private static final String BAZAAR_URL = "https://api.hypixel.net/v2/skyblock/bazaar";

	private final HypixelDataSource hypixelDataSource;
	private final int intervalInMinutes;
	private final BooleanSupplier shouldFetch;

	private final AtomicBoolean fetchInProgress;
	private final AtomicBoolean lastFetchSuccessful;

	private final AtomicReference<Map<String, Product>> bazaarCache;
	private volatile Instant lastBazaarUpdate;

	private volatile boolean firstBazaarUpdated;

	public BazaarFetcher(HypixelDataSource hypixelDataSource, int intervalInMinutes, @NotNull BooleanSupplier shouldFetch) {
		this.hypixelDataSource = hypixelDataSource;
		this.intervalInMinutes = intervalInMinutes;
		this.shouldFetch = shouldFetch;
		this.fetchInProgress = new AtomicBoolean(false);
		this.lastFetchSuccessful = new AtomicBoolean(false);
		this.bazaarCache = new AtomicReference<>(Map.of());
		this.lastBazaarUpdate = null;
		this.firstBazaarUpdated = false;
		CaribouStonks.LOGGER.info("[BazaarFetcher] Bazaar products will be updated every {} minute", intervalInMinutes);
	}

	/**
	 * Start periodic fetching of Bazaar products
	 */
	public void start() {
		TickScheduler.getInstance().runRepeating(() -> {
			if (shouldFetch.getAsBoolean()) {
				triggerFetch().thenRun(() -> {
					if (lastFetchSuccessful.get()) {
						CaribouStonks.LOGGER.info("[BazaarFetcher] {} products updated. (last update: {})",
								bazaarCache.get().size(), lastBazaarUpdate == null ? "n/a" : lastBazaarUpdate.toString());
					}

					if (!firstBazaarUpdated) {
						firstBazaarUpdated = true;
						hypixelDataSource.fixSkyBlockItems();
					}
				});
			}
		}, intervalInMinutes, TimeUnit.MINUTES);
	}

	/**
	 * Return an immutable snapshot of the latest bazaar products.
	 *
	 * @return an immutable Map snapshot of product id -> Product
	 */
	public Map<String, Product> getBazaarSnapshot() {
		return bazaarCache.get();
	}

	/**
	 * Returns whether the initial bazaar update callback has been executed.
	 *
	 * @return {@code true} if the firstBazaarUpdated action has been executed
	 */
	public boolean isFirstBazaarUpdated() {
		return firstBazaarUpdated;
	}

	/**
	 * Returns whether a fetch is currently in progress.
	 *
	 * @return {@code true} if a background fetch is running
	 */
	public boolean isFetching() {
		return fetchInProgress.get();
	}

	/**
	 * Trigger an asynchronous fetch of the Bazaar endpoint and return a CompletableFuture that
	 * completes when the attempt finishes (successfully or not).
	 *
	 * @return a CompletableFuture that completes when the fetch attempt finishes.
	 */
	private @NotNull CompletableFuture<Void> triggerFetch() {
		fetchInProgress.set(true);
		lastFetchSuccessful.set(false);

		CompletableFuture<Void> promise = CompletableFuture.runAsync(
				this::executeFetch,
				AsyncScheduler.getInstance().blockingExecutor()
		);

		promise = promise.exceptionallyCompose(throwable -> {
			Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
			CaribouStonks.LOGGER.error("[BazaarFetcher] Fetch Bazaar failed.", cause);

			return CompletableFuture.completedFuture(null);
		});

		promise = promise.whenComplete((v, t) -> fetchInProgress.set(false));
		return promise;
	}

	/**
	 * Perform the blocking HTTP request and parse the Bazaar reply.
	 *
	 * @throws RuntimeException if the request fails or the reply is invalid
	 */
	private void executeFetch() {
		try (HttpResponse response = Http.request(BAZAAR_URL)) {
			if (!response.success()) {
				throw new HttpResponseException(response.statusCode(), response.content());
			}

			SkyBlockBazaarReply reply = GsonProvider.prettyPrinting().fromJson(response.content(), SkyBlockBazaarReply.class);
			if (reply == null) {
				throw new IllegalStateException("SkyBlock API Bazaar returned null reply");
			}

			if (!reply.success) {
				String cause = reply.cause != null ? reply.cause : "?";
				throw new RuntimeException("SkyBlock API Bazaar failed. Cause: " + cause);
			}

			if (reply.products == null || reply.products.isEmpty()) {
				throw new RuntimeException("SkyBlock API Bazaar returned empty products");
			}

			// TODO - remove complètement les Summary et Mappé directement les valeurs dans un nouveau Product Object
			//  pour la version 0.7.0 - 1.7 Mo en heap est utilisé juste pour ça

			bazaarCache.set(Map.copyOf(reply.products));
			lastBazaarUpdate = reply.lastUpdated > 0 ? Instant.ofEpochSecond(reply.lastUpdated) : Instant.MIN;
			lastFetchSuccessful.set(true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * SkyBlockBazaarReply from Hypixel API - DTO for Gson parsing
	 */
	private static class SkyBlockBazaarReply {
		public boolean success;
		public String cause;
		public long lastUpdated;
		public Map<String, Product> products;
	}
}
