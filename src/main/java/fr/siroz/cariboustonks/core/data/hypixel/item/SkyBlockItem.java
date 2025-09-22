package fr.siroz.cariboustonks.core.data.hypixel.item;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a SkyBlock item as returned by the SkyBlock API.
 * <p>
 * Note: the {@code material} field is the <b>Hypixel</b> material and not the Minecraft material.
 * See {@link fr.siroz.cariboustonks.core.data.mod.ModDataSource#getMinecraftId(String)} for the mapping.
 *
 * @param skyBlockId   the skyBlockId (e.g. "BOOSTER_COOKIE")
 * @param material     the material (e.g. "COOKIE")
 * @param name         the name (e.g. "Booster Cookie")
 * @param tier         the tier (e.g. "COMMON")
 * @param skullTexture the skullTexture if the item is a skull and contains a skin value, otherwise null
 */
public record SkyBlockItem(
		@NotNull String skyBlockId,
		@NotNull String material,
		@NotNull String name,
		@NotNull Rarity tier,
		@Nullable String skullTexture
) {

	/**
	 * Creates a {@code SkyBlockItem} from a {@link JsonObject} as returned by the SkyBlock API.
	 *
	 * @param jsonItem the JsonObject describing the item
	 */
	@ApiStatus.Internal
	@Contract("_ -> new")
	public static @NotNull SkyBlockItem parse(@NotNull JsonObject jsonItem) throws RuntimeException {
		try {
			String id = jsonItem.get("id").getAsString();
			String material = jsonItem.get("material").getAsString();
			String name = jsonItem.get("name").getAsString();
			Rarity rarity = computeTier(jsonItem);
			String skullTexture = computeSkullTexture(jsonItem, material);
			return new SkyBlockItem(id, material, name, rarity, skullTexture);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static Rarity computeTier(@NotNull JsonObject jsonItem) {
		if (jsonItem.has("tier")) {
			return Rarity.fromName(jsonItem.get("tier").getAsString());
		}
		return Rarity.UNKNOWN;
	}

	private static @Nullable String computeSkullTexture(@NotNull JsonObject jsonItem, String material) {
		if (jsonItem.has("skin") && "SKULL_ITEM".equals(material)) {
			JsonObject skin = jsonItem.get("skin").getAsJsonObject();
			return skin.has("value") ? skin.get("value").getAsString() : null;
		}
		return null;
	}
}
