package fr.siroz.cariboustonks.core.mod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.cooldown.Cooldown;
import fr.siroz.cariboustonks.core.module.http.Http;
import fr.siroz.cariboustonks.core.module.http.HttpResponse;
import fr.siroz.cariboustonks.core.service.scheduler.AsyncScheduler;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockAttribute;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockEnchantment;
import fr.siroz.cariboustonks.util.Client;
import java.io.BufferedReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ModDataSource {

	// Hypixel SkyBlock API - Item "material" > Minecraft Item material ID
	private static final Identifier ITEMS_MAPPING_JSON = CaribouStonks.identifier("repo/items_mapping.json");
	private final Map<String, String> minecraftIdsMapping = new HashMap<>();

	// Hypixel SkyBlock Wiki - Enchantments
	private static final Identifier ENCHANTMENTS_JSON = CaribouStonks.identifier("repo/enchantments.json");
	private final Map<String, SkyBlockEnchantment> skyBlockEnchants = new HashMap<>();

	// Hypixel SkyBlock Attributes
	private static final String ATTRIBUTES_JSON_URL = "https://raw.githubusercontent.com/Siroz555/Caribou-REPO/refs/heads/main/data/attributes.json";
	private static final Duration FIRST_RETRY_DELAY = Duration.ofMinutes(1);
	private static final Cooldown RELOAD_COOLDOWN = Cooldown.of(10, TimeUnit.SECONDS);
	private static final int MAX_RETRIES = 10;
	private volatile List<SkyBlockAttribute> skyBlockAttributes = Collections.emptyList();
	private final AtomicBoolean attributesFetchInProgress = new AtomicBoolean(false);
	private final AtomicInteger attributesRetryAttempts = new AtomicInteger(0);
	private volatile boolean attributesFetchError = false;

	private boolean itemsMappingError = false;
	private boolean enchantmentsError = false;

	public ModDataSource() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> loadModData(client).thenRun(this::checkInternalDataResults));
		this.triggerSkyBlockAttributesFetch(false).thenRun(checkExternalDataResults());
	}

	public void reload() {
		if (RELOAD_COOLDOWN.test()) {
			Client.sendMessageWithPrefix(Component.literal("Reloading attributes..").withColor(Colors.YELLOW.asInt()));
			triggerSkyBlockAttributesFetch(false).thenRun(() -> {
				if (attributesFetchError) {
					Client.sendMessageWithPrefix(Component.literal("Reloading attributes failed! Try again later.").withColor(Colors.RED.asInt()));
				} else {
					Client.sendMessageWithPrefix(Component.literal("Attributes reloaded! (" + skyBlockAttributes.size() + " loaded)").withColor(Colors.GREEN.asInt()));
				}
			});
		} else {
			Client.sendMessageWithPrefix(Component.literal("Reloading attributes on cooldown! Try again in few seconds.").withColor(Colors.RED.asInt()));
		}
	}

	public @Nullable String getMinecraftId(@NonNull String hypixelMaterial) {
		if (minecraftIdsMapping.isEmpty()) return null;
		return minecraftIdsMapping.get(hypixelMaterial);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean containsItem(@NonNull String hypixelMaterial) {
		if (minecraftIdsMapping.isEmpty()) return false;
		return minecraftIdsMapping.containsKey(hypixelMaterial);
	}

	public boolean isItemsMappingError() {
		return itemsMappingError;
	}

	public @Nullable SkyBlockEnchantment getSkyBlockEnchantment(@NonNull String id) {
		if (skyBlockEnchants.isEmpty()) return null;
		return skyBlockEnchants.get(id);
	}

	public @Nullable SkyBlockAttribute getAttributeBySkyBlockId(@Nullable String skyBlockId) {
		if (skyBlockId == null || skyBlockId.isEmpty()) return null;

		List<SkyBlockAttribute> attributes = skyBlockAttributes;
		for (SkyBlockAttribute attribute : attributes) {
			if (attribute.skyBlockApiId().equals(skyBlockId)) {
				return attribute;
			}
		}

		return null;
	}

	public @Nullable SkyBlockAttribute getAttributeById(@Nullable String id) {
		if (id == null || id.isEmpty()) return null;

		List<SkyBlockAttribute> attributes = skyBlockAttributes;
		for (SkyBlockAttribute attribute : attributes) {
			if (attribute.id().equals(id)) {
				return attribute;
			}
		}

		return null;
	}

	public @Nullable SkyBlockAttribute getAttributeByShardName(@Nullable String name) {
		if (name == null || name.isEmpty()) return null;

		// Support SkyBlock 0.23.3 | "Shard" a été rajouté après le nom de la shard
		// Si je rajoute "Shard" dans chaque nom dans le fichier attributes.json, l'Hunting Box bug
		// et il faut re-check le container pour double check bref...
		int index = name.indexOf("Shard");
		if (index > -1) name = name.substring(0, index - 1);
		name = name.replace("BUY ", "").replace("SELL ", "");

		List<SkyBlockAttribute> attributes = skyBlockAttributes;
		for (SkyBlockAttribute attribute : attributes) {
			if (attribute.shardName().equals(name)) {
				return attribute;
			}
		}

		return null;
	}

	private @NonNull CompletableFuture<Void> loadModData(Minecraft client) {
		CompletableFuture<Void> itemsMappingFuture = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = client.getResourceManager().openAsReader(ITEMS_MAPPING_JSON)) {

				JsonObject jsonMapping = JsonParser.parseReader(reader).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : jsonMapping.entrySet()) {
					minecraftIdsMapping.put(entry.getKey(), entry.getValue().getAsString());
				}
			} catch (Exception ex) {
				itemsMappingError = true;
				CaribouStonks.LOGGER.error("[ModDataSource] There was an error while loading items mapping", ex);
			}
		});

		CompletableFuture<Void> enchantmentsFuture = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = client.getResourceManager().openAsReader(ENCHANTMENTS_JSON)) {

				JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
				for (JsonElement element : jsonArray) {
					JsonObject jsonEnchantment = element.getAsJsonObject();
					SkyBlockEnchantment enchantment = getSkyBlockEnchantment(jsonEnchantment);
					skyBlockEnchants.put(enchantment.id(), enchantment);
				}
			} catch (Throwable throwable) {
				enchantmentsError = true;
				CaribouStonks.LOGGER.error("[ModDataSource] There was an error while loading enchantments", throwable);
			}
		});

		return CompletableFuture.allOf(itemsMappingFuture, enchantmentsFuture);
	}

	private CompletableFuture<Void> triggerSkyBlockAttributesFetch(boolean force) {
		if (!force && !attributesFetchInProgress.compareAndSet(false, true)) {
			CaribouStonks.LOGGER.warn("[ModDataSource] Skipping attributes fetch, already in progress");
			return CompletableFuture.completedFuture(null);
		}

		CompletableFuture<Void> promise = CompletableFuture.runAsync(
				this::executeFetch,
				AsyncScheduler.getInstance().blockingExecutor()
		);

		promise = promise.exceptionallyCompose(throwable -> {
			Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
			CaribouStonks.LOGGER.error("[ModDataSource] Fetch attributes failed (attempt {}). Cause: {}", attributesRetryAttempts.get(), cause);
			attributesFetchError = true;

			int attemptsSoFar = attributesRetryAttempts.getAndIncrement();
			if (attemptsSoFar >= MAX_RETRIES) {
				CaribouStonks.LOGGER.error("[ModDataSource] Max retries reached, aborting fetch");
				attributesFetchInProgress.set(false);
			} else {
				long minutes = FIRST_RETRY_DELAY.toMinutes() << attemptsSoFar;
				CaribouStonks.LOGGER.warn("[ModDataSource] Retrying attributes fetch in {} minutes (attempt {}/{})", minutes, attemptsSoFar + 1, MAX_RETRIES);
				TickScheduler.getInstance().runLater(() -> triggerSkyBlockAttributesFetch(true).thenRun(checkExternalDataResults()), (int) minutes, TimeUnit.MINUTES);
			}
			return CompletableFuture.completedFuture(null);
		});

		promise = promise.whenComplete((_v, _t) -> attributesFetchInProgress.set(false));
		return promise;
	}

	private void executeFetch() {
		try (HttpResponse response = Http.request(ATTRIBUTES_JSON_URL)) {
			if (!response.success()) {
				throw new RuntimeException("GitHub returned an error code: " + response.statusCode() + " cause: " + response.content());
			}

			String body = response.content();
			if (body == null || body.isBlank()) {
				throw new RuntimeException("GitHub returned null or blank reply");
			}

			JsonArray jsonArray = JsonParser.parseString(body).getAsJsonArray();

			List<SkyBlockAttribute> attributes = new ArrayList<>(jsonArray.size());
			for (JsonElement element : jsonArray) {
				JsonObject jsonAttribute = element.getAsJsonObject();
				SkyBlockAttribute attribute = getSkyBlockAttribute(jsonAttribute);
				attributes.add(attribute);
			}

			skyBlockAttributes = List.copyOf(attributes);
			attributesRetryAttempts.set(0);
			attributesFetchError = false;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void checkInternalDataResults() {
		if (!itemsMappingError && !minecraftIdsMapping.isEmpty()) {
			CaribouStonks.LOGGER.info("[ModDataSource] Loaded {} items in the items mapping", minecraftIdsMapping.size());
		}

		if (!enchantmentsError && !skyBlockEnchants.isEmpty()) {
			CaribouStonks.LOGGER.info("[ModDataSource] Loaded {} enchantments", skyBlockEnchants.size());
		}
	}

	private @NonNull Runnable checkExternalDataResults() {
		return () -> {
			if (skyBlockAttributes.isEmpty()) {
				CaribouStonks.LOGGER.warn("[ModDataSource] No attributes loaded yet");
			} else {
				CaribouStonks.LOGGER.info("[ModDataSource] Loaded {} attributes (external source)", skyBlockAttributes.size());
			}
		};
	}

	private @NonNull SkyBlockEnchantment getSkyBlockEnchantment(@NonNull JsonObject jsonEnchantment) {
		return new SkyBlockEnchantment(
				jsonEnchantment.get("id").getAsString(),
				jsonEnchantment.get("name").getAsString(),
				jsonEnchantment.get("maxLevel").getAsInt(),
				jsonEnchantment.has("goodLevel")
						? OptionalInt.of(jsonEnchantment.get("goodLevel").getAsInt()) : OptionalInt.empty()
		);
	}

	private @NonNull SkyBlockAttribute getSkyBlockAttribute(@NonNull JsonObject jsonAttribute) {
		return new SkyBlockAttribute(
				jsonAttribute.get("name").getAsString(),
				jsonAttribute.get("shardName").getAsString(),
				jsonAttribute.get("id").getAsString(),
				jsonAttribute.get("skyBlockApiId").getAsString()
		);
	}
}
