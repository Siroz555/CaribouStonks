package fr.siroz.cariboustonks.util;

import fr.siroz.cariboustonks.core.data.hypixel.item.PetInfo;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Utility class for handling operations related to NotEnoughUpdates (NEU).
 * <p>
 * <b>This class is deprecated</b>
 */
@ApiStatus.Experimental
public final class NotEnoughUpdatesUtils {

	private NotEnoughUpdatesUtils() {
	}

	public static @NotNull String getNeuId(@Nullable ItemStack stack) {
		if (stack == null) {
			return "";
		}

		String id = SkyBlockAPI.getSkyBlockItemId(stack);
		CompoundTag customData = ItemUtils.getCustomData(stack);

		return switch (id) {
			case "ENCHANTED_BOOK" -> {
				CompoundTag enchantments = customData.getCompoundOrEmpty("enchantments");
				String enchant = enchantments.keySet().stream().findFirst().orElse("");
				yield enchant.toUpperCase(Locale.ENGLISH) + ";" + enchantments.getIntOr(enchant, 0);
			}

			case "RUNE" -> {
				CompoundTag runes = customData.getCompoundOrEmpty("runes");
				String rune = runes.keySet().stream().findFirst().orElse("");
				yield rune.toUpperCase(Locale.ENGLISH) + "_RUNE;" + runes.getIntOr(rune, 0);
			}

			case "PET" -> {
				if (!customData.contains("petInfo")) yield id;
				PetInfo petInfo = PetInfo.parse(customData);
				yield petInfo.type() + ";" + petInfo.rarity().getIndex();
			}

			case "POTION" -> "POTION_" + customData.getStringOr("potion", "").toUpperCase(Locale.ENGLISH)
					+ ";"
					+ customData.getIntOr("potion_level", 0);

			case "ATTRIBUTE_SHARD" -> ""; // The Foraging Update 0.23 - "New Shard API"

			case "PARTY_HAT_CRAB", "BALLOON_HAT_2024" -> id
					+ "_"
					+ customData.getStringOr("party_hat_color", "").toUpperCase(Locale.ENGLISH);

			case "PARTY_HAT_CRAB_ANIMATED" -> "PARTY_HAT_CRAB_"
					+ customData.getStringOr("party_hat_color", "").toUpperCase(Locale.ENGLISH)
					+ "_ANIMATED";

			case "PARTY_HAT_SLOTH" -> id
					+ "_"
					+ customData.getStringOr("party_hat_emoji", "").toUpperCase(Locale.ENGLISH);

			default -> id.replace(":", "-");
		};
	}

	public static @NotNull String getNeuIdFromSkyBlockId(@NotNull String skyBlockItemId) {
		if (skyBlockItemId.isEmpty()) {
			return "";
		}

		// Cas des enchantements : ENCHANTMENT_<NOM>_<NIVEAU>
		if (skyBlockItemId.startsWith("ENCHANTMENT_")) {
			String enchantment = skyBlockItemId.substring("ENCHANTMENT_".length());
			int lastUnderscore = enchantment.lastIndexOf('_');
			if (lastUnderscore != -1 && lastUnderscore < enchantment.length() - 1) {
				String name = enchantment.substring(0, lastUnderscore);
				String level = enchantment.substring(lastUnderscore + 1);
				return name + ";" + level;
			}
		}

		return skyBlockItemId.replace(":", "-");
	}
}
