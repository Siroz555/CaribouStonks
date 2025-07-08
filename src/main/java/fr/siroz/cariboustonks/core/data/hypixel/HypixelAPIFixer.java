package fr.siroz.cariboustonks.core.data.hypixel;

import fr.siroz.cariboustonks.util.Rarity;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class HypixelAPIFixer {

	public static final Pattern MINION_PATTERN = Pattern.compile("^[A-Z_]+_GENERATOR_\\d+$");
	public static final Pattern ENCHANTMENT_PATTERN = Pattern.compile("^ENCHANTMENT_[A-Z_]+_\\d+$");
	public static final Pattern ESSENCE_PATTERN = Pattern.compile("^ESSENCE_[A-Z]+$");

	HypixelAPIFixer() {
	}

	public boolean isBlacklisted(@NotNull String inputId) {
		boolean blacklisted = false;

		Matcher minionMatcher = MINION_PATTERN.matcher(inputId);
		if (minionMatcher.matches()) {
			return true;
		}

		// Autres

		return blacklisted;
	}

	public boolean isEnchantment(@NotNull String inputId) {
		Matcher matcher = ENCHANTMENT_PATTERN.matcher(inputId);
		return matcher.matches();
	}

	public boolean isEssence(@NotNull String inputId) {
		Matcher matcher = ESSENCE_PATTERN.matcher(inputId);
		return matcher.matches();
	}

	public @NotNull SkyBlockItem createEnchant(@NotNull String skyBlockIdEnchantment) {
		String material = "ENCHANTED_BOOK";
		String name = getEnchantName(skyBlockIdEnchantment);
		Rarity tier = skyBlockIdEnchantment.contains("ULTIMATE") ? Rarity.MYTHIC : Rarity.UNCOMMON;
		return new SkyBlockItem(skyBlockIdEnchantment, material, name, tier);
	}

	public @NotNull SkyBlockItem createEssence(@NotNull String skyBlockIdEssence) {
		String material = "SKULL_ITEM";
		String name = getEssenceName(skyBlockIdEssence);
		return new SkyBlockItem(skyBlockIdEssence, material, name, Rarity.MYTHIC);
	}

	/**
	 * Transforme un enchantment ID en un vrai nom.
	 * <table>
	 *     <tr>
	 *         <th>Entrée</th>
	 *         <th>Sortie</th>
	 *     </tr>
	 *     <tr>
	 *         <td>{@code ENCHANTMENT_TABASCO_1}</td>
	 *         <td>{@code Tabasco 1}</td>
	 *     </tr>
	 *     <tr>
	 *         <td>{@code ENCHANTMENT_TURBO_CARROT_13}</td>
	 *         <td>{@code Turbo Carrot 13}</td>
	 *     </tr>
	 *     <tr>
	 *         <td>{@code ENCHANTMENT_ULTIMATE_LEGION_1}</td>
	 *         <td>{@code Ultimate Legion 1}</td>
	 *     </tr>
	 * </table>
	 *
	 * @param inputId enchantment ID
	 * @return le nom issu de l'ID
	 */
	public @NotNull String getEnchantName(@NotNull String inputId) {
		if (inputId.isEmpty()) {
			return "";
		}

		String withoutPrefix = inputId.replaceFirst("^ENCHANTMENT_", "");
		String[] words = withoutPrefix.split("_");

		StringBuilder prettyName = new StringBuilder();
		for (int i = 0; i < words.length; i++) {
			String word = words[i].toLowerCase();
			word = word.substring(0, 1).toUpperCase() + word.substring(1);

			if (i > 0) {
				prettyName.append(" ");
			}

			prettyName.append(word);
		}

		return prettyName.toString();
	}

	public @NotNull String getEssenceName(@NotNull String input) {
		if (!input.contains("_")) {
			return input;
		}

		String essenceName = input.split("_")[1];
		String prettyName = essenceName.substring(0, 1).toUpperCase() + essenceName.substring(1).toLowerCase();
		return prettyName + " Essence";
	}
}
