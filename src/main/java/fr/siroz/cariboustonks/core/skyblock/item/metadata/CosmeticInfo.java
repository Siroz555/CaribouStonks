package fr.siroz.cariboustonks.core.skyblock.item.metadata;

import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Represents "all" (No runes) {@code Cosmetic} applied to an item.
 *
 * @param skin    the skin
 * @param dye     the dye
 * @param isShiny whether the item is shiny
 */
public record CosmeticInfo(
		Optional<String> skin,
		Optional<String> dye,
		boolean isShiny
) {

	public static final CosmeticInfo EMPTY = new CosmeticInfo(
			Optional.empty(),
			Optional.empty(),
			false
	);

	public static CosmeticInfo ofNbt(@NotNull NbtCompound customData) {
		try {
			Optional<String> skinData = customData.getString("skin");
			Optional<String> dyeData = customData.getString("dye_item");
			boolean isShinyData = customData.getBoolean("is_shiny", false);

			return new CosmeticInfo(skinData, dyeData, isShinyData);
		} catch (Exception ignored) {
			return EMPTY;
		}
	}
}
