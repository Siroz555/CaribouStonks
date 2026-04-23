package fr.siroz.cariboustonks.core.skyblock.data.hypixel.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.siroz.cariboustonks.util.JsonUtils;
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
 * <h1>------------------------------------</h1>
 * <h1>Important: Hypixel SKyBlock Latest Version</h1>
 * {@code itemModel} is the new Minecraft Material Mapper provided by Hypixel.
 * The material is a PAPER (always), with the {@code itemModel} field ("minecraft:bamboo").
 * {@code material} is deprecated but used for "legacy" items.
 * <h1>------------------------------------</h1>
 * Note: the {@code material} field is the <b>Hypixel</b> material and not the Minecraft material.
 * See {@code ModDataSource#getMinecraftId(String)} for the mapping.
 *
 * @param skyBlockId    the skyBlockId (e.g. "BOOSTER_COOKIE")
 * @param material      the material (e.g. "COOKIE")
 * @param itemModel     the Minecraft item_model (e.g. "minecraft:bamboo")
 * @param name          the name (e.g. "Booster Cookie")
 * @param tier          the tier (e.g. "COMMON")
 * @param category      the category if the item has a category field (e.g. "ACCESSORY")
 * @param skullTexture  the skullTexture if the item is a skull and contains a skin value (Base64 encoded)
 * @param gemstoneSlots the {@link GemstoneSlot} list if the item has a gemstone_slots field
 * @param prestige      the {@link PrestigeItem} if the item has a prestige field
 */
public record SkyBlockItemData(
		@NonNull String skyBlockId,
		Optional<String> material,
		Optional<String> itemModel,
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
			Optional.empty(),
			Optional.empty(),
			"",
			Rarity.UNKNOWN,
			Optional.empty(),
			Optional.empty(),
			Optional.empty(),
			Optional.empty()
	);

	private static final String SKULL_ITEM_MATERIAL = "SKULL_ITEM";

	/**
	 * Creates a {@code SkyBlockItem} from a {@link JsonObject} as returned by the SkyBlock API.
	 *
	 * @param jsonItem the JsonObject describing the item
	 */
	public static @NonNull SkyBlockItemData parse(@NonNull JsonObject jsonItem) throws SkyBlockItemParseException {
		try {
			String id = jsonItem.get("id").getAsString();
			String material = JsonUtils.getString(jsonItem, "material");
			Optional<String> itemModel = Optional.ofNullable(JsonUtils.getString(jsonItem, "item_model"));
			String name = jsonItem.get("name").getAsString();
			Rarity rarity = computeTier(jsonItem);
			Optional<String> category = Optional.ofNullable(JsonUtils.getString(jsonItem, "category"));
			Optional<String> skullTexture = Optional.ofNullable(computeSkullTexture(jsonItem, material));
			Optional<List<GemstoneSlot>> gemstoneSlots = Optional.ofNullable(computeGemstoneSlots(jsonItem));
			Optional<PrestigeItem> prestige = Optional.ofNullable(computePrestige(jsonItem));
			return new SkyBlockItemData(id, Optional.ofNullable(material), itemModel, name, rarity, category, skullTexture, gemstoneSlots, prestige);
		} catch (Exception ex) {
			String id = Optional.ofNullable(JsonUtils.getString(jsonItem, "id")).orElse("UNKNOWN");
			throw new SkyBlockItemParseException(id, ex);
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

		static @NonNull GearUpgrade parse(@NonNull JsonObject json) {
			return new GearUpgrade(
					Optional.ofNullable(JsonUtils.getString(json, "item_id")),
					Optional.ofNullable(JsonUtils.getString(json, "essence_type")),
					JsonUtils.getOptionalInt(json, "amount")
			);
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
			String slotType = JsonUtils.getStringOrDefault(json, "slot_type", "");
			JsonArray costsArray = JsonUtils.getArray(json, "costs");
			if (costsArray == null) return EMPTY;

			List<GemstoneSlotCost> costs = new ArrayList<>();
			for (JsonElement element : costsArray) {
				if (element.isJsonObject()) {
					GemstoneSlotCost parsedCost = GemstoneSlotCost.parse(element.getAsJsonObject());
					if (!parsedCost.equals(GemstoneSlotCost.EMPTY)) {
						costs.add(parsedCost);
					}
				}
			}
			return new GemstoneSlot(slotType, costs.isEmpty() ? Optional.empty() : Optional.of(costs));
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

			static @NonNull GemstoneSlotCost parse(@NonNull JsonObject json) {
				return new GemstoneSlotCost(
						JsonUtils.getStringOrDefault(json, "type", ""),
						Optional.ofNullable(JsonUtils.getString(json, "item_id")),
						JsonUtils.getOptionalInt(json, "amount"),
						JsonUtils.getOptionalInt(json, "coins")
				);
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
			String itemId = JsonUtils.getStringOrDefault(json, "item_id", "");
			JsonArray costsArray = JsonUtils.getArray(json, "costs");
			if (costsArray == null) return EMPTY;

			List<GearUpgrade> upgrades = new ArrayList<>();
			for (JsonElement element : costsArray) {
				if (element.isJsonObject()) {
					GearUpgrade parsedUpgrade = GearUpgrade.parse(element.getAsJsonObject());
					if (!parsedUpgrade.equals(GearUpgrade.EMPTY)) {
						upgrades.add(parsedUpgrade);
					}
				}
			}
			return new PrestigeItem(itemId, upgrades);
		}
	}

	private static @NonNull Rarity computeTier(@NonNull JsonObject jsonItem) {
		String tier = JsonUtils.getString(jsonItem, "tier");
		return tier != null ? Rarity.fromName(tier) : Rarity.UNKNOWN;
	}

	private static @Nullable String computeSkullTexture(@NonNull JsonObject jsonItem, @Nullable String material) {
		if (!SKULL_ITEM_MATERIAL.equals(material)) return null;
		JsonObject skin = JsonUtils.getObject(jsonItem, "skin");
		return skin != null ? JsonUtils.getString(skin, "value") : null;
	}

	private static @Nullable List<GemstoneSlot> computeGemstoneSlots(@NonNull JsonObject jsonItem) {
		JsonArray array = JsonUtils.getArray(jsonItem, "gemstone_slots");
		if (array == null) return null;

		List<GemstoneSlot> slots = new ArrayList<>();
		for (JsonElement element : array) {
			if (element.isJsonObject()) {
				// if (!parsedX.equals(X.EMPTY))
				slots.add(GemstoneSlot.parse(element.getAsJsonObject()));
			}
		}
		return slots.isEmpty() ? null : slots;
	}

	private static @Nullable PrestigeItem computePrestige(@NonNull JsonObject jsonItem) {
		JsonObject prestige = JsonUtils.getObject(jsonItem, "prestige");
		// if (!parsedX.equals(X.EMPTY))
		return prestige != null ? PrestigeItem.parse(prestige) : null;
	}
}
