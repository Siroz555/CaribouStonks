package fr.siroz.cariboustonks.core.skyblock.data.hypixel.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.siroz.cariboustonks.core.mod.ModDataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents a SkyBlock item as returned by the SkyBlock API.
 * <p>
 * <a href="https://api.hypixel.net/v2/resources/skyblock/items">Hypixel API - Resources - Items</a>
 * <p>
 * Note: the {@code material} field is the <b>Hypixel</b> material and not the Minecraft material.
 * See {@link ModDataSource#getMinecraftId(String)} for the mapping.
 *
 * @param skyBlockId    the skyBlockId (e.g. "BOOSTER_COOKIE")
 * @param material      the material (e.g. "COOKIE")
 * @param name          the name (e.g. "Booster Cookie")
 * @param tier          the tier (e.g. "COMMON")
 * @param category      the category if the item has a category field (e.g. "ACCESSORY")
 * @param skullTexture  the skullTexture if the item is a skull and contains a skin value (Base64 encoded)
 * @param gemstoneSlots the {@link GemstoneSlot} list if the item has a gemstone_slots field
 * @param prestige      the {@link PrestigeItem} if the item has a prestige field
 */
public record SkyBlockItemData(
		@NonNull String skyBlockId,
		@NonNull String material,
		@NonNull String name,
		@NonNull Rarity tier,
		Optional<String> category,
		Optional<String> skullTexture,
		Optional<List<GemstoneSlot>> gemstoneSlots,
		Optional<PrestigeItem> prestige
) {

	/**
	 * An empty {@link SkyBlockItemData} instance.
	 */
	public static final SkyBlockItemData EMPTY = new SkyBlockItemData(
			"",
			"",
			"", Rarity.UNKNOWN,
			Optional.empty(),
			Optional.empty(),
			Optional.empty(),
			Optional.empty()
	);

	/**
	 * Creates a {@code SkyBlockItem} from a {@link JsonObject} as returned by the SkyBlock API.
	 *
	 * @param jsonItem the JsonObject describing the item
	 */
	public static @NonNull SkyBlockItemData parse(@NonNull JsonObject jsonItem) throws RuntimeException {
		try {
			String id = jsonItem.get("id").getAsString();
			String material = jsonItem.get("material").getAsString();
			String name = jsonItem.get("name").getAsString();
			Rarity rarity = computeTier(jsonItem);
			Optional<String> category = Optional.ofNullable(computeCategory(jsonItem));
			Optional<String> skullTexture = Optional.ofNullable(computeSkullTexture(jsonItem, material));
			Optional<List<GemstoneSlot>> gemstoneSlots = Optional.ofNullable(computeGemstoneSlots(jsonItem));
			Optional<PrestigeItem> prestige = Optional.ofNullable(computePrestige(jsonItem));
			return new SkyBlockItemData(id, material, name, rarity, category, skullTexture, gemstoneSlots, prestige);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Represents an upgrade cost.
	 * <p>
	 * <h3>Example</h3>
	 * Diamond Hunter Helmet:
	 * <li>1- itemId=MAGMA_FISH amount=200</li>
	 * <li>2- itemId=MAGMA_FISH_SILVER amount=10</li>
	 * <li>3- itemId=MAGMA_FISH_SILVER amount=20</li>
	 * <li>4- itemId=MAGMA_FISH_SILVER amount=40</li>
	 * <li>5- itemId=MAGMA_FISH_GOLD amount=1</li>
	 * <li>...</li>
	 * <p>
	 * Infernal Crimson Chestplate:
	 * <li>1- essenceType=CRIMSON amount=30000</li>
	 * <li>2- essenceType=CRIMSON amount=35000</li>
	 * <li>3- essenceType=CRIMSON amount=41000</li>
	 * <li>4- essenceType=CRIMSON amount=56000</li>
	 * <li>...</li>
	 * <li>10- itemId=HEAVY_PEARL amount=3</li>
	 * <li>...</li>
	 *
	 * @param itemId      the item ID (e.g. "MAGMA_FISH", "MAGMA_FISH_SILVER")
	 * @param essenceType the essence type (e.g. "CRIMSON", "WITHER")
	 * @param amount      the amount
	 */
	public record GearUpgrade(
			Optional<String> itemId,
			Optional<String> essenceType,
			OptionalInt amount
	) {

		public static final GearUpgrade EMPTY = new GearUpgrade(Optional.empty(), Optional.empty(), OptionalInt.empty());

		static GearUpgrade parse(@NonNull JsonObject json) {
			try {
				Optional<String> itemId = Optional.ofNullable(json.has("item_id") ? json.get("item_id").getAsString() : null);
				Optional<String> essenceType = Optional.ofNullable(json.has("essence_type") ? json.get("essence_type").getAsString() : null);
				OptionalInt amount = json.has("amount") ? OptionalInt.of(json.get("amount").getAsInt()) : OptionalInt.empty();
				return new GearUpgrade(itemId, essenceType, amount);
			} catch (Exception ignored) {
				return EMPTY;
			}
		}
	}

	/**
	 * Represents a gemstone slot.
	 *
	 * @param slotType the slot type (e.g. "COMBAT")
	 * @param costs    an optional list of {@link GemstoneSlotCost}
	 */
	public record GemstoneSlot(
			String slotType,
			Optional<List<GemstoneSlotCost>> costs
	) {

		public static final GemstoneSlot EMPTY = new GemstoneSlot("", Optional.empty());

		static GemstoneSlot parse(@NonNull JsonObject json) {
			try {
				String slotType = json.has("slot_type") ? json.get("slot_type").getAsString() : "";
				JsonArray costs = json.has("costs") ? json.get("costs").getAsJsonArray() : null;
				List<GemstoneSlotCost> parsedCosts = new ArrayList<>();
				if (costs != null) {
					for (JsonElement cost : costs) {
						if (cost.isJsonObject()) {
							JsonObject costJson = cost.getAsJsonObject();
							GemstoneSlotCost parsedCost = GemstoneSlotCost.parse(costJson);
							if (!parsedCost.equals(GemstoneSlotCost.EMPTY)) {
								parsedCosts.add(parsedCost);
							}
						}
					}
				}
				return new GemstoneSlot(slotType, parsedCosts.isEmpty() ? Optional.empty() : Optional.of(parsedCosts));
			} catch (Exception ignored) {
				return EMPTY;
			}
		}

		/**
		 * Represents a gemstone slot cost.
		 * <p>
		 * <h3>Example</h3>
		 * Infernal Crimson Chestplate (First Slot):
		 * <li>type=COINS, coins=250000</li>
		 * <li>type=ITEM, itemId=FLAWLESS_JASPER_GEM, amount=1</li>
		 * <li>type=ITEM, itemId=FLAWLESS_SAPPHIRE_GEM, amount=1</li>
		 * <li>type=ITEM, itemId=FLAWLESS_RUBY_GEM, amount=1</li>
		 * <li>type=ITEM, itemId=FLAWLESS_AMETHYST_GEM, amount=1</li>
		 *
		 * @param type   the type (e.g. "ITEM" or "COINS")
		 * @param itemId the item ID (e.g. "FLAWLESS_JASPER_GEM")
		 * @param amount the amount
		 * @param coins  the number of coins
		 */
		public record GemstoneSlotCost(
				String type,
				Optional<String> itemId,
				OptionalInt amount,
				OptionalInt coins
		) {

			public static final GemstoneSlotCost EMPTY = new GemstoneSlotCost("", Optional.empty(), OptionalInt.empty(), OptionalInt.empty());

			static GemstoneSlotCost parse(@NonNull JsonObject json) {
				try {
					String type = json.has("type") ? json.get("type").getAsString() : "";
					Optional<String> itemId = Optional.ofNullable(json.has("item_id") ? json.get("item_id").getAsString() : null);
					OptionalInt amount = json.has("amount") ? OptionalInt.of(json.get("amount").getAsInt()) : OptionalInt.empty();
					OptionalInt coins = json.has("coins") ? OptionalInt.of(json.get("coins").getAsInt()) : OptionalInt.empty();
					return new GemstoneSlotCost(type, itemId, amount, coins);
				} catch (Exception ignored) {
					return EMPTY;
				}
			}
		}
	}

	/**
	 * Represents a prestige.
	 *
	 * @param itemId the item ID (e.g. "INFERNAL_CRIMSON_HELMET")
	 * @param costs  the list of {@link GearUpgrade}
	 */
	public record PrestigeItem(
			String itemId,
			List<GearUpgrade> costs
	) {

		public static final PrestigeItem EMPTY = new PrestigeItem("", List.of());

		static PrestigeItem parse(@NonNull JsonObject json) {
			try {
				String itemId = json.has("item_id") ? json.get("item_id").getAsString() : "";
				JsonArray upgradesCost = json.has("costs") ? json.get("costs").getAsJsonArray() : null;
				List<GearUpgrade> upgrades = new ArrayList<>();
				if (upgradesCost != null) {
					for (JsonElement upgradeCost : upgradesCost) {
						if (upgradeCost.isJsonObject()) {
							JsonObject upgrade = upgradeCost.getAsJsonObject();
							GearUpgrade parsedUpgrade = GearUpgrade.parse(upgrade);
							if (!parsedUpgrade.equals(GearUpgrade.EMPTY)) {
								upgrades.add(parsedUpgrade);
							}
						}
					}
				}
				return new PrestigeItem(itemId, upgrades);
			} catch (Exception ignored) {
				return EMPTY;
			}
		}
	}

	private static Rarity computeTier(@NonNull JsonObject jsonItem) {
		if (jsonItem.has("tier")) {
			return Rarity.fromName(jsonItem.get("tier").getAsString());
		}
		return Rarity.UNKNOWN;
	}

	private static @Nullable String computeCategory(@NonNull JsonObject jsonItem) {
		if (jsonItem.has("category")) {
			return jsonItem.get("category").getAsString();
		}
		return null;
	}

	private static @Nullable String computeSkullTexture(@NonNull JsonObject jsonItem, String material) {
		if (jsonItem.has("skin") && "SKULL_ITEM".equals(material)) {
			JsonObject skin = jsonItem.get("skin").getAsJsonObject();
			return skin.has("value") ? skin.get("value").getAsString() : null;
		}
		return null;
	}

	private static @Nullable List<GemstoneSlot> computeGemstoneSlots(@NonNull JsonObject jsonItem) {
		if (jsonItem.has("gemstone_slots") && jsonItem.get("gemstone_slots").isJsonArray()) {
			List<GemstoneSlot> gemstoneSlots = new ArrayList<>();
			JsonArray gemstoneSlotsJson = jsonItem.get("gemstone_slots").getAsJsonArray();
			for (JsonElement gemstoneSlotJson : gemstoneSlotsJson) {
				if (gemstoneSlotJson.isJsonObject()) {
					JsonObject gemstoneSlot = gemstoneSlotJson.getAsJsonObject();
					GemstoneSlot parsedGemstoneSlot = GemstoneSlot.parse(gemstoneSlot);
					// if (!parsedX.equals(X.EMPTY))
					gemstoneSlots.add(parsedGemstoneSlot);
				}
			}
			return gemstoneSlots.isEmpty() ? null : gemstoneSlots;
		}
		return null;
	}

	private static @Nullable PrestigeItem computePrestige(@NonNull JsonObject jsonItem) {
		if (jsonItem.has("prestige") && jsonItem.get("prestige").isJsonObject()) {
			JsonObject prestige = jsonItem.get("prestige").getAsJsonObject();
			// if (!parsedX.equals(X.EMPTY))
			return PrestigeItem.parse(prestige);
		}
		return null;
	}
}
