package fr.siroz.cariboustonks.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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

		String id = ItemUtils.getSkyBlockItemId(stack);
		NbtCompound customData = ItemUtils.getCustomData(stack);

		return switch (id) {
			case "ENCHANTED_BOOK" -> {
				NbtCompound enchantments = customData.getCompoundOrEmpty("enchantments");
				String enchant = enchantments.getKeys().stream().findFirst().orElse("");
				yield enchant.toUpperCase(Locale.ENGLISH) + ";" + enchantments.getInt(enchant, 0);
			}

			case "RUNE" -> {
				NbtCompound runes = customData.getCompoundOrEmpty("runes");
				String rune = runes.getKeys().stream().findFirst().orElse("");
				yield rune.toUpperCase(Locale.ENGLISH) + "_RUNE;" + runes.getInt(rune, 0);
			}

			case "POTION" -> "POTION_" + customData.getString("potion", "").toUpperCase(Locale.ENGLISH)
					+ ";"
					+ customData.getInt("potion_level", 0);
			case "ATTRIBUTE_SHARD" -> ""; // The Foraging Update 0.23 - "New Shard API"
			case "PARTY_HAT_CRAB", "BALLOON_HAT_2024" -> id
					+ "_"
					+ customData.getString("party_hat_color", "").toUpperCase(Locale.ENGLISH);
			case "PARTY_HAT_CRAB_ANIMATED" -> "PARTY_HAT_CRAB_"
					+ customData.getString("party_hat_color", "").toUpperCase(Locale.ENGLISH)
					+ "_ANIMATED";
			case "PARTY_HAT_SLOTH" -> id
					+ "_"
					+ customData.getString("party_hat_emoji", "").toUpperCase(Locale.ENGLISH);
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
