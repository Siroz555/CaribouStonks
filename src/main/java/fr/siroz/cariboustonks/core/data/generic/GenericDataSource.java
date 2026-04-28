package fr.siroz.cariboustonks.core.data.generic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.AsyncScheduler;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.core.json.GsonProvider;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.JsonUtils;
import fr.siroz.cariboustonks.util.http.Http;
import fr.siroz.cariboustonks.util.http.HttpResponse;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;
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
import org.jetbrains.annotations.Nullable;

/**
 * GenericDataSource
 * <p>
 * <h3>Auction House Data</h3>
 * Moulberry (NEU) > Elite's API Endpoints.
 * <p>
 * Elite's API is used. <a href="https://eliteskyblock.com/"EliteSkyBlock</a> .
 * All requests to the Elite API are subject to its Privacy Policy.
 * <a href="https://api.eliteskyblock.com/">Elite API</a>
 * Credits to {@code ptlthg} for the API backend/access
 */
public final class GenericDataSource {

	// Price History Mapping (Elite's History API)
	private static final String PRICE_HISTORY_BASE_URL = "https://api.eliteskyblock.com/resources/";
	private final Map<String, GraphCacheEntry> graphCache = new HashMap<>();
	public static final Duration CACHE_EXPIRATION_PRICE_HISTORY = Duration.ofMinutes(15);

	// Liste des items à l'Auction (Elite's LBIN API)
	private static final String LOWEST_BIN_AUCTION_URL = "https://api.eliteskyblock.com/resources/auctions/neu";
	private final Object2DoubleMap<String> lowestBinsPrices = new Object2DoubleOpenHashMap<>();

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
		if (key.isNull() || key.neuId() == null || lowestBinsPrices.isEmpty()) return false;
		return lowestBinsPrices.containsKey(key.neuId());
	}

	public Optional<Double> getLowestBin(@NotNull ItemLookupKey key) {
		if (key.isNull() || key.neuId() == null || lowestBinsPrices.isEmpty()) return Optional.empty();
		return Optional.of(lowestBinsPrices.getDouble(key.neuId()));
	}

	public CompletableFuture<GraphParseResult> loadGraphData(ItemLookupKey key) {
		if (key.isNull()) {
			return CompletableFuture.completedFuture(null);
		}

		GraphCacheEntry cacheEntry = graphCache.get(key.hypixelSkyBlockId());
		if (cacheEntry != null && cacheEntry.isValid()) {
			return CompletableFuture.completedFuture(cacheEntry.data());
		} else if (cacheEntry != null && !cacheEntry.isValid()) {
			graphCache.remove(key.hypixelSkyBlockId());
		}

		return fetchGraphData(key);
	}

	private CompletableFuture<GraphParseResult> fetchGraphData(ItemLookupKey key) {
		final GraphFetchStrategy strategy = resolveStrategy(key);
		return CompletableFuture.supplyAsync(() -> {
			try (HttpResponse response = Http.request(strategy.url())) {
				if (!response.success()) {
					CaribouStonks.LOGGER.warn("[GenericDataSource] Price History API returned error {} for {}", response.statusCode(), key.hypixelSkyBlockId());
					return null;
				}

				JsonObject json = GsonProvider.prettyPrinting().fromJson(response.content(), JsonObject.class);
				if (json == null) {
					CaribouStonks.LOGGER.warn("[GenericDataSource] Json is null or empty for {}", key.hypixelSkyBlockId());
					return null;
				}

				GraphParseResult result = strategy.parser().apply(json);
				if (result != null && result.prices() != null && result.prices().size() > 2) {
					graphCache.put(key.hypixelSkyBlockId(), new GraphCacheEntry(result, Instant.now()));
				}

				return result;
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[GenericDataSource] Failed to fetch price history for {}", key.hypixelSkyBlockId(), ex);
				return null;
			}
		}, AsyncScheduler.getInstance().blockingExecutor());
	}

	private GraphFetchStrategy resolveStrategy(ItemLookupKey key) {
		if (CaribouStonks.core().getHypixelDataSource().hasBazaarItem(key.hypixelSkyBlockId())) {
			return new GraphFetchStrategy(
					PRICE_HISTORY_BASE_URL + "bazaar/" + key.hypixelSkyBlockId() + "/history?timespan=30d",
					json -> GraphParseResult.ofBazaar(parseBazaarGraph(json))
			);
		} else {
			return new GraphFetchStrategy(
					PRICE_HISTORY_BASE_URL + "auctions/" + key.hypixelSkyBlockId() + "/default?timespan=30d",
					this::parseAuctionHouseGraph
			);
		}
	}

	@Nullable
	private List<ItemPrice> parseBazaarGraph(JsonObject json) {
		JsonArray historyArray = JsonUtils.getArray(json, "history");
		if (historyArray == null) return null;

		List<ItemPrice> result = new ArrayList<>();
		for (JsonElement element : historyArray) {
			if (element.isJsonObject()) {
				JsonObject history = element.getAsJsonObject();
				Instant timestamp = JsonUtils.getInstant(history, "timestamp");
				OptionalDouble buyPrice = JsonUtils.getOptionalDouble(history, "instaBuyPrice");
				OptionalDouble sellPrice = JsonUtils.getOptionalDouble(history, "instaSellPrice");
				if (timestamp != null && buyPrice.isPresent() && sellPrice.isPresent()) {
					result.add(new ItemPrice(timestamp, buyPrice.getAsDouble(), sellPrice.getAsDouble()));
				}
			}
		}

		return result;
	}

	@Nullable
	private GraphParseResult parseAuctionHouseGraph(JsonObject json) {
		JsonArray historyArray = JsonUtils.getArray(json, "history");
		if (historyArray == null) return null;

		List<ItemPrice> itemPrices = new ArrayList<>();
		List<AuctionStatistics.AuctionDataPoint> dataPoints = new ArrayList<>();

		for (JsonElement element : historyArray) {
			if (element.isJsonObject()) {
				JsonObject history = element.getAsJsonObject();
				Instant timestamp = JsonUtils.getInstant(history, "timestamp");
				OptionalDouble price = JsonUtils.getOptionalDouble(history, "lowestBinPrice");
				OptionalInt itemsSold    = JsonUtils.getOptionalInt(history, "itemsSold");
				// Skip dans tout les cas si null
				if (timestamp == null) continue;
				// Prix -> Graphique
				if (price.isPresent()) {
					itemPrices.add(new ItemPrice(timestamp, price.getAsDouble(), null));
				}
				// Stats -> agrégation
				dataPoints.add(new AuctionStatistics.AuctionDataPoint(timestamp, itemsSold.orElse(0), price));
			}
		}

		return GraphParseResult.ofAuction(itemPrices, AuctionStatistics.compute(dataPoints));
	}

	private @NotNull CompletableFuture<Void> updateLowestBins() {
		lowestBinsInUpdate = true;

		return fetchLowestBins().thenAccept(result -> {
			if (result == null || result.isEmpty() || result.size() < 2) {
				lowestBinsError = true;
				return;
			}

			lowestBinsPrices.clear();
			lowestBinsPrices.putAll(result);
		});
	}

	@Contract(" -> new")
	private @NotNull CompletableFuture<Map<String, Double>> fetchLowestBins() {
		return CompletableFuture.supplyAsync(() -> {
			try (HttpResponse response = Http.request(LOWEST_BIN_AUCTION_URL)) {
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
		}, AsyncScheduler.getInstance().blockingExecutor());
	}

	private void checkLowestBinsResult() {
		if (!lowestBinsError) {
			CaribouStonks.LOGGER.info("[GenericDataSource] Updated {} lowest bins", lowestBinsPrices.size());
		} else {
			CaribouStonks.LOGGER.warn("[GenericDataSource] Unable to update lowest bins");
		}
	}

	private record GraphFetchStrategy(String url, Function<JsonObject, GraphParseResult> parser) {
	}
}
