package fr.siroz.cariboustonks.core.skyblock.item.metadata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

// SIROZ-NOTE : Remplacer l'autre PetInfo par celui ci et le cleanup pour le SkyBlockAPI#getPetInfo
// + faire le calculator pour les Pets, mais j'ai pas tout compris ce que fait Altpapier SkyHelper-Networth.

public record PetInfo(
		String type,
		double exp,
		String tier,
		int candies,
		Optional<String> heldItem,
		Optional<String> skin
) {

	public static final PetInfo EMPTY = new PetInfo(
			"",
			0,
			"",
			0,
			Optional.empty(),
			Optional.empty()
	);

	public static PetInfo ofNbt(@NotNull CompoundTag customData) {
		try {
			String petInfoJson = customData.getStringOr("petInfo", "");
			if (petInfoJson.isEmpty()) {
				return EMPTY;
			}

			JsonObject json = JsonParser.parseString(petInfoJson).getAsJsonObject();

			String type = json.has("type") ? json.get("type").getAsString() : "";
			double exp = json.has("exp") ? json.get("exp").getAsDouble() : 0;
			String tier = json.has("tier") ? json.get("tier").getAsString() : "";
			int candies = json.has("candyUsed") ? json.get("candyUsed").getAsInt() : 0;
			Optional<String> heldItem = json.has("heldItem")
					? Optional.of(json.get("heldItem").getAsString())
					: Optional.empty();
			Optional<String> skin = json.has("skin")
					? Optional.of(json.get("skin").getAsString())
					: Optional.empty();

			return new PetInfo(type, exp, tier, candies, heldItem, skin);
		} catch (Exception ignored) {
			return EMPTY;
		}
	}
}
