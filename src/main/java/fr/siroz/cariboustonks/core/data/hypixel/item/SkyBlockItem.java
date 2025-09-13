package fr.siroz.cariboustonks.core.data.hypixel.item;

import com.google.gson.JsonObject;
import fr.siroz.cariboustonks.core.data.mod.ModDataSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a SkyBlock item as returned by the SkyBlock API.
 * <p>
 * Note: the {@code material} field is the <b>Hypixel</b> material and not the Minecraft material.
 * See {@link ModDataSource#getMinecraftId(String)} for the mapping.
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
	public SkyBlockItem(@NotNull JsonObject jsonItem) throws NullPointerException, UnsupportedOperationException, IllegalStateException {
		this(
				jsonItem.get("id").getAsString(),
				jsonItem.get("material").getAsString(),
				jsonItem.get("name").getAsString(),
				computeTier(jsonItem),
				computeSkullTexture(jsonItem, jsonItem.get("material").getAsString())
		);
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
