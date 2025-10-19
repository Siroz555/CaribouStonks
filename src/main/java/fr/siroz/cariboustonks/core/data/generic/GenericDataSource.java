package fr.siroz.cariboustonks.core.data.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.core.json.GsonProvider;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.http.Http;
import fr.siroz.cariboustonks.util.http.HttpResponse;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.apache.http.client.HttpResponseException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class GenericDataSource {

	// Price History Mapping (NEU API)
	private static final String NEU_PRICE_HISTORY_URL = "http://pricehistory.notenoughupdates.org/";
	private final Map<String, GraphCacheEntry> graphCache = new HashMap<>();
	public static final Duration CACHE_EXPIRATION_PRICE_HISTORY = Duration.ofMinutes(30);

	// Liste des items à l'Auction (NEU API)
	private static final String NEU_LOWEST_BIN_AUCTION_URL = "https://moulberry.codes/lowestbin.json";
	private final Object2DoubleMap<String> lowestBinsNEU = new Object2DoubleOpenHashMap<>();

	private boolean lowestBinsInUpdate = false;
	private boolean lowestBinsError = false;

	@ApiStatus.Internal
	public GenericDataSource() {
		ClientLifecycleEvents.CLIENT_STARTED.register(_mc -> TickScheduler.getInstance().runRepeating(() -> {
			if (ConfigManager.getConfig().general.internal.fetchAuctionData) {
				updateLowestBins().thenRun(() -> {
					lowestBinsInUpdate = false;
					lowestBinsError = false;
					checkLowestBinsResult();
				});
			}
		}, 5, TimeUnit.MINUTES));
	}

	public boolean isLowestBinsInUpdate() {
		return lowestBinsInUpdate;
	}

	public boolean hasLowestBin(@NotNull ItemLookupKey key) {
		if (key.isNull() || key.neuId() == null || lowestBinsNEU.isEmpty()) return false;
		return lowestBinsNEU.containsKey(key.neuId());
	}

	public Optional<Double> getLowestBin(@NotNull ItemLookupKey key) {
		if (key.isNull() || key.neuId() == null || lowestBinsNEU.isEmpty()) return Optional.empty();
		return Optional.of(lowestBinsNEU.getDouble(key.neuId()));
	}

	public CompletableFuture<List<ItemPrice>> loadGraphData(@NotNull ItemLookupKey key) {
		if (key.isNull()) {
			return CompletableFuture.completedFuture(null);
		}

		GraphCacheEntry cacheEntry = graphCache.get(key.neuId());
		if (cacheEntry != null && cacheEntry.isValid()) {
			return CompletableFuture.completedFuture(cacheEntry.data());
		} else if (cacheEntry != null && !cacheEntry.isValid()) {
			graphCache.remove(key.neuId());
		}

		return fetchGraphData(key);
	}

	@Contract("_ -> new")
	private @NotNull CompletableFuture<List<ItemPrice>> fetchGraphData(@NotNull ItemLookupKey key) {
		return CompletableFuture.supplyAsync(() -> {
			try (HttpResponse response = Http.request(NEU_PRICE_HISTORY_URL + "?item=" + key.neuId())) {
				if (!response.success()) {
					throw new HttpResponseException(response.statusCode(), "HTTP error " + response.statusCode());
				}

				JsonObject json = GsonProvider.prettyPrinting().fromJson(response.content(), JsonObject.class);
				if (json == null) {
					throw new IllegalStateException("Json is null or empty");
				}

				List<ItemPrice> result = new ArrayList<>();
				for (Map.Entry<String, JsonElement> element : json.entrySet()) {
					try {
						Instant instant = Instant.parse(element.getKey());
						JsonObject jsonPrice = element.getValue().getAsJsonObject();
						double buyPrice = jsonPrice.get("b").getAsDouble();
						Double sellPrice = jsonPrice.has("s") ? jsonPrice.get("s").getAsDouble() : null;
						result.add(new ItemPrice(instant, buyPrice, sellPrice));
					} catch (Exception ex) {
						CaribouStonks.LOGGER.error(
								"[GenericDataSource] Failed to parse price history for {}", key.neuId(), ex);
					}
				}

				if (result.size() > 2) {
					GraphCacheEntry entry = new GraphCacheEntry(result, Instant.now());
					graphCache.put(key.neuId(), entry);
				}

				return result;
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[GenericDataSource] Failed to fetch price history for {}", key.neuId(), ex);
				return null;
			}
		});
	}

	private @NotNull CompletableFuture<Void> updateLowestBins() {
		lowestBinsInUpdate = true;

		return fetchLowestBins().thenAccept(result -> {
			if (result == null || result.isEmpty() || result.size() < 2) {
				lowestBinsError = true;
				return;
			}

			lowestBinsNEU.clear();
			lowestBinsNEU.putAll(result);
		});
	}

	@Contract(" -> new")
	private @NotNull CompletableFuture<Map<String, Double>> fetchLowestBins() {
		return CompletableFuture.supplyAsync(() -> {
			try (HttpResponse response = Http.request(NEU_LOWEST_BIN_AUCTION_URL)) {
				if (!response.success()) {
					throw new HttpResponseException(response.statusCode(), response.content());
				}

				JsonObject json = GsonProvider.prettyPrinting().fromJson(response.content(), JsonObject.class);
				if (json == null) {
					throw new IllegalStateException("Json is null or empty");
				}

				Map<String, Double> result = new HashMap<>();
				for (Map.Entry<String, JsonElement> element : json.entrySet()) {
					try {
						result.put(element.getKey(), element.getValue().getAsDouble());
					} catch (Exception ex) {
						CaribouStonks.LOGGER.error(
								"[GenericDataSource] Failed to parse lowest bin {}", element.getKey(), ex);
					}
				}

				return result;
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[GenericDataSource] Failed to fetch Auction lowest bins", ex);
				return null;
			}
		});
	}

	private void checkLowestBinsResult() {
		if (!lowestBinsError) {
			CaribouStonks.LOGGER.info("[GenericDataSource] Updated {} lowest bins", lowestBinsNEU.size());
		} else {
			CaribouStonks.LOGGER.warn("[GenericDataSource] Unable to update lowest bins");
		}
	}
}
