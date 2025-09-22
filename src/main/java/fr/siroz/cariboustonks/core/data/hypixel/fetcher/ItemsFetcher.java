package fr.siroz.cariboustonks.core.data.hypixel.fetcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelAPIFixer;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.data.hypixel.item.SkyBlockItem;
import fr.siroz.cariboustonks.core.data.mod.ModDataSource;
import fr.siroz.cariboustonks.core.json.GsonProvider;
import fr.siroz.cariboustonks.core.scheduler.AsyncScheduler;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.http.Http;
import fr.siroz.cariboustonks.util.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.http.client.HttpResponseException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fetches and caches {@code Hypixel SkyBlock Items resource}.
 * <p>
 * <a href="https://api.hypixel.net/v2/resources/skyblock/items">Hypixel API - Resources - Items</a>
 * <p>
 * Performs an asynchronous fetch of the SkyBlock items resource and stores an immutable snapshot
 * of parsed {@link SkyBlockItem} objects.
 */
@ApiStatus.Internal
public final class ItemsFetcher {

	private static final String ITEMS_URL = "https://api.hypixel.net/v2/resources/skyblock/items";

	private static final Duration FIRST_RETRY_DELAY = Duration.ofMinutes(1);
	private static final int MAX_RETRIES = 5;

	private final HypixelDataSource hypixelDataSource;
	private final ModDataSource modDataSource;
	private final HypixelAPIFixer apiFixer;

	private final AtomicBoolean fetchInProgress;
	private final AtomicInteger retryAttempts;
	private final AtomicBoolean lastFetchSuccessful;

	private final AtomicReference<Map<String, SkyBlockItem>> skyBlockItems;

	public ItemsFetcher(HypixelDataSource hypixelDataSource, ModDataSource modDataSource, HypixelAPIFixer apiFixer) {
		this.hypixelDataSource = hypixelDataSource;
		this.modDataSource = modDataSource;
		this.apiFixer = apiFixer;
		this.fetchInProgress = new AtomicBoolean(false);
		this.retryAttempts = new AtomicInteger(0);
		this.lastFetchSuccessful = new AtomicBoolean(false);
		this.skyBlockItems = new AtomicReference<>(Map.of());
	}

	/**
	 * Trigger an initial fetch and attach a post-fetch logging callback.
	 */
	public void start() {
		triggerFetch(false).thenRun(afterFetch());
	}

	/**
	 * Return an immutable snapshot of the latest parsed SkyBlock items.
	 *
	 * @return immutable snapshot Map of item id -> SkyBlockItem
	 */
	public Map<String, SkyBlockItem> getSkyBlockItemsSnapshot() {
		return skyBlockItems.get();
	}

	/**
	 * Returns true if the last fetch attempt completed successfully.
	 *
	 * @return {@code true} if the last fetch was successful
	 */
	public boolean isLastFetchSuccessful() {
		return lastFetchSuccessful.get();
	}

	/**
	 * Atomically put or replace a single SkyBlock item in the cached snapshot.
	 * <p>
	 * Performs a copy-on-write update, it copies the current snapshot into a new mutable {@link HashMap},
	 * applies the change, and stores an immutable snapshot (via {@link Map#copyOf}).
	 *
	 * @param key  the item id
	 * @param item the SkyBlockItem instance
	 */
	public void putItem(@NotNull String key, @NotNull SkyBlockItem item) {
		skyBlockItems.updateAndGet(prev -> {
			Map<String, SkyBlockItem> mutable = new HashMap<>(prev);
			mutable.put(key, item);
			return Map.copyOf(mutable);
		});
	}

	/**
	 * Trigger an asynchronous fetch of the SkyBlock items resource.
	 * <p>
	 * On failure a retry is scheduled with an exponential-ish backoff until {@code MAX_RETRIES} is reached.
	 *
	 * @param force if true, force a fetch even if one is already in progress
	 * @return a CompletableFuture that completes when the attempt finishes; the future completes normally
	 * whether the attempt succeeded or failed (failure triggers retry scheduling).
	 */
	private CompletableFuture<Void> triggerFetch(boolean force) {
		if (!force && !fetchInProgress.compareAndSet(false, true)) {
			CaribouStonks.LOGGER.warn("[ItemsFetcher] Skipping fetch, already in progress");
			return CompletableFuture.completedFuture(null);
		}

		CompletableFuture<Void> promise = CompletableFuture.runAsync(
				this::executeFetch,
				AsyncScheduler.getInstance().blockingExecutor()
		);

		promise = promise.exceptionallyCompose(throwable -> {
			Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
			CaribouStonks.LOGGER.error("[ItemsFetcher] Fetch items failed (attempt {}). Cause: {}", retryAttempts.get(), cause);

			int attemptsSoFar = retryAttempts.getAndIncrement();
			if (attemptsSoFar >= MAX_RETRIES) {
				CaribouStonks.LOGGER.error("[ItemsFetcher] Max retries reached, aborting fetch");
				fetchInProgress.set(false);
				lastFetchSuccessful.set(false);
			} else {
				long minutes = FIRST_RETRY_DELAY.toMinutes() << attemptsSoFar;
				CaribouStonks.LOGGER.warn("[ItemsFetcher] Retrying items fetch in {} minutes (attempt {}/{})", minutes, attemptsSoFar + 1, MAX_RETRIES);
				TickScheduler.getInstance().runLater(() -> triggerFetch(true).thenRun(afterFetch()), (int) minutes, TimeUnit.MINUTES);
			}
			return CompletableFuture.completedFuture(null);
		});

		promise = promise.whenComplete((v, t) -> fetchInProgress.set(false));
		return promise;
	}

	/**
	 * Perform the blocking HTTP request and parse the item resource reply.
	 *
	 * @throws RuntimeException if the request fails or the reply is invalid
	 */
	private void executeFetch() {
		try (HttpResponse response = Http.request(ITEMS_URL)) {
			if (!response.success()) {
				throw new HttpResponseException(response.statusCode(), response.content());
			}

			String body = response.content();
			if (body == null || body.isBlank()) {
				throw new RuntimeException("SkyBlock API Items Resource returned null or blank reply");
			}

			JsonObject root = JsonParser.parseString(body).getAsJsonObject();
			if (!root.has("success") || !root.get("success").getAsBoolean()) {
				String cause = root.has("cause") ? root.get("cause").getAsString() : "?";
				throw new RuntimeException("SkyBlock API Items Resource failed. Cause: " + cause);
			}

			JsonArray itemsArray = GsonProvider.safeGetAsArray(root, "items");

			Map<String, SkyBlockItem> items = parseItems(itemsArray);
			if (items != null && !items.isEmpty()) {
				skyBlockItems.set(Map.copyOf(items));
				retryAttempts.set(0);
				lastFetchSuccessful.set(true);
			} else {
				throw new RuntimeException("Unable to parse SkyBlock Items");
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private Map<String, SkyBlockItem> parseItems(@Nullable JsonArray itemsArray) {
		if (itemsArray == null) return null;

		Map<String, SkyBlockItem> items = new HashMap<>();
		for (int i = 0; i < itemsArray.size(); i++) {

			JsonObject item = itemsArray.get(i).getAsJsonObject();
			if (item.has("id")) {
				String id = item.get("id").getAsString();
				if (apiFixer.isBlacklisted(id)) continue;

				try {
					SkyBlockItem skyBlockItem = SkyBlockItem.parse(item);
					items.put(id, skyBlockItem);
				} catch (Exception ex) {
					CaribouStonks.LOGGER.error("[ItemFetcher] Unable to parse SkyBlock Item: {}", id, ex);
				}
			}
		}

		return items;
	}

	/**
	 * Call-back runnable executed after a fetch attempt completes
	 */
	@Contract(pure = true)
	private @NotNull Runnable afterFetch() {
		return () -> {
			if (lastFetchSuccessful.get()) {
				CaribouStonks.LOGGER.info("[ItemsFetcher] Loaded {} SkyBlock Items", skyBlockItems.get().size());
				hypixelDataSource.fixSkyBlockItems();
			}

			if (lastFetchSuccessful.get() && !modDataSource.isItemsMappingError()) {
				List<String> hypixelMaterials = skyBlockItems.get().values().stream()
						.map(SkyBlockItem::material)
						.collect(Collectors.toSet())
						.stream()
						.toList();

				for (String material : hypixelMaterials) {
					if (!modDataSource.containsItem(material)) {
						CaribouStonks.LOGGER.warn("[ItemsFetcher] (Minecraft Ids Mapping) -> {} is not registered!", material);
					}
				}
			} else {
				CaribouStonks.LOGGER.error("[ItemsFetcher] (Minecraft Ids Mapping) SkyBlock Items error or mapping error");
			}
		};
	}
}
