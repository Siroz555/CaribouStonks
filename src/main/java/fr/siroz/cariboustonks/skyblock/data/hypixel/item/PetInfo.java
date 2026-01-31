package fr.siroz.cariboustonks.skyblock.data.hypixel.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a SkyBlock Pet's info.
 *
 * @param name   the name of the pet, if any or null
 * @param type   the type of the pet or empty an empty string
 * @param rarity the rarity of the pet
 */
public record PetInfo(
		@Nullable String name,
		@NotNull String type,
		@NotNull Rarity rarity
) {

	public static final PetInfo EMPTY = new PetInfo(null, "", Rarity.UNKNOWN);

	@ApiStatus.Internal
	public static @NotNull PetInfo parse(@NotNull CompoundTag data) {
		String petInfo = data.getStringOr("petInfo", "");
		if (petInfo.isEmpty()) {
			return EMPTY;
		}

		try {
			JsonElement element = JsonParser.parseString(petInfo);
			JsonObject json = element.getAsJsonObject();

			String name = json.has("name") ? json.get("name").getAsString() : null;
			String type = json.has("type") ? json.get("type").getAsString() : "";
			Rarity rarity = Rarity.fromName(json.has("tier") ? json.get("tier").getAsString() : null);

			return new PetInfo(name, type, rarity);
		} catch (Exception ignored) {
		}

		return EMPTY;
	}
}
