package fr.siroz.cariboustonks.core.mod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockAttribute;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockEnchantment;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
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

	// Hypixel SkyBlock API & IG - Attributes 1.23 The Foraging Update
	private static final Identifier ATTRIBUTES_JSON = CaribouStonks.identifier("repo/attributes.json");
	private final List<SkyBlockAttribute> skyBlockAttributes = new ArrayList<>();

	private boolean itemsMappingError = false;
	private boolean enchantmentsError = false;
	private boolean attributesError = false;

	public ModDataSource() {
		ClientLifecycleEvents.CLIENT_STARTED.register(
				client -> loadModData(client).thenRun(this::checkResults));
	}

	public @Nullable String getMinecraftId(@NonNull String hypixelMaterial) {
		if (minecraftIdsMapping.isEmpty()) return null;
		return minecraftIdsMapping.get(hypixelMaterial);
	}

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
		if (skyBlockId == null || skyBlockId.isEmpty() || skyBlockAttributes.isEmpty()) return null;

		for (SkyBlockAttribute attribute : skyBlockAttributes) {
			if (attribute.skyBlockApiId().equals(skyBlockId)) {
				return attribute;
			}
		}

		return null;
	}

	public @Nullable SkyBlockAttribute getAttributeById(@Nullable String id) {
		if (id == null || id.isEmpty() || skyBlockAttributes.isEmpty()) return null;

		for (SkyBlockAttribute attribute : skyBlockAttributes) {
			if (attribute.id().equals(id)) {
				return attribute;
			}
		}

		return null;
	}

	public @Nullable SkyBlockAttribute getAttributeByShardName(@Nullable String name) {
		if (name == null || name.isEmpty() || skyBlockAttributes.isEmpty()) return null;

		// Support SkyBlock 0.23.3 | "Shard" a été rajouté après le nom de la shard
		// Si je rajoute "Shard" dans chaque nom dans le fichier attributes.json, l'Hunting Box bug
		// et il faut re-check le container pour double check bref...
		name = name.replace(" Shard", "");

		for (SkyBlockAttribute attribute : skyBlockAttributes) {
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

		CompletableFuture<Void> attributesFuture = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = client.getResourceManager().openAsReader(ATTRIBUTES_JSON)) {

				JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
				for (JsonElement element : jsonArray) {
					JsonObject jsonAttribute = element.getAsJsonObject();
					SkyBlockAttribute attribute = getSkyBlockAttribute(jsonAttribute);
					skyBlockAttributes.add(attribute);
				}
			} catch (Throwable throwable) {
				attributesError = true;
				CaribouStonks.LOGGER.error("[ModDataSource] There was an error while loading attributes", throwable);
			}
		});

		return CompletableFuture.allOf(itemsMappingFuture, enchantmentsFuture, attributesFuture);
	}

	private void checkResults() {
		if (!itemsMappingError && !minecraftIdsMapping.isEmpty()) {
			CaribouStonks.LOGGER.info("[ModDataSource] Loaded {} items in the items mapping", minecraftIdsMapping.size());
		}

		if (!enchantmentsError && !skyBlockEnchants.isEmpty()) {
			CaribouStonks.LOGGER.info("[ModDataSource] Loaded {} enchantments", skyBlockEnchants.size());
		}

		if (!attributesError && !skyBlockAttributes.isEmpty()) {
			CaribouStonks.LOGGER.info("[ModDataSource] Loaded {} attributes", skyBlockAttributes.size());
		}
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
