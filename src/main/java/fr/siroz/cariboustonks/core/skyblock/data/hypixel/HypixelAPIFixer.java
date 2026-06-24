package fr.siroz.cariboustonks.core.skyblock.data.hypixel;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.Rarity;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockAttribute;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class HypixelAPIFixer {
	private static final Pattern MINION_PATTERN = Pattern.compile("^[A-Z_]+_GENERATOR_\\d+$");
	private static final Pattern ENCHANTMENT_PATTERN = Pattern.compile("^ENCHANTMENT_[A-Z_]+_\\d+$");
	private static final Pattern ESSENCE_PATTERN = Pattern.compile("^ESSENCE_[A-Z]+$");
	private static final Pattern SHARD_PATTERN = Pattern.compile("^SHARD_[A-Z]+(_[A-Z]+)*$");

	private int totalBlacklisted = 0;

	HypixelAPIFixer() {
	}

	public boolean isBlacklisted(@Nullable String inputId, @Nullable SkyBlockItemData skyBlockItemData) {
		boolean blacklisted = false;

		if (inputId != null) {
			Matcher minionMatcher = MINION_PATTERN.matcher(inputId);
			if (minionMatcher.matches()) {
				totalBlacklisted++;
				return true;
			}
		}

		if (skyBlockItemData != null) {
			boolean soulbound = skyBlockItemData.soulbound();
			if (soulbound) {
				totalBlacklisted++;
				return true;
			}
		}

		return blacklisted;
	}

	public boolean isEnchantment(@NonNull String inputId) {
		return ENCHANTMENT_PATTERN.matcher(inputId).matches();
	}

	public boolean isEssence(@NonNull String inputId) {
		return ESSENCE_PATTERN.matcher(inputId).matches();
	}

	public boolean isShard(@NonNull String inputId) {
		return SHARD_PATTERN.matcher(inputId).matches();
	}

	public @NonNull SkyBlockItemData createEnchant(@NonNull String skyBlockIdEnchantment) {
		String material = "ENCHANTED_BOOK";
		String name = getEnchantName(skyBlockIdEnchantment);
		Rarity tier = skyBlockIdEnchantment.contains("ULTIMATE") ? Rarity.MYTHIC : Rarity.UNCOMMON;
		return new SkyBlockItemData(
				skyBlockIdEnchantment,
				false,
				Optional.of(material),
				Optional.empty(),
				name,
				tier,
				Optional.empty(),
				Optional.empty(),
				OptionalDouble.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty()
		);
	}

	public @NonNull SkyBlockItemData createEssence(@NonNull String skyBlockIdEssence) {
		String material = "SKULL_ITEM";
		String name = getEssenceName(skyBlockIdEssence);
		return new SkyBlockItemData(
				skyBlockIdEssence,
				false,
				Optional.of(material),
				Optional.empty(),
				name,
				Rarity.MYTHIC,
				Optional.empty(),
				Optional.empty(),
				OptionalDouble.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty()
		);
	}

	public @Nullable SkyBlockItemData createShard(@NonNull String skyBlockIdShard) {
		SkyBlockAttribute attribute = CaribouStonks.mod().getModDataSource().getAttributeBySkyBlockId(skyBlockIdShard);
		if (attribute != null) {
			String material = "PRISMARINE_SHARD";
			String name = attribute.name() + " (" + attribute.id() + ")";
			Rarity tier = attribute.getRarityFromId();
			return new SkyBlockItemData(
					skyBlockIdShard,
					false,
					Optional.of(material),
					Optional.empty(),
					name,
					tier,
					Optional.empty(),
					Optional.empty(),
					OptionalDouble.empty(),
					Optional.empty(),
					Optional.empty(),
					Optional.empty()
			);
		}

		return null;
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
	public @NonNull String getEnchantName(@NonNull String inputId) {
		if (inputId.isEmpty()) return "";

		String withoutPrefix = inputId.replaceFirst("^ENCHANTMENT_", "");
		String[] words = withoutPrefix.split("_");

		StringBuilder prettyName = new StringBuilder();
		for (int i = 0; i < words.length; i++) {
			String word = words[i].toLowerCase(Locale.ENGLISH);
			word = word.substring(0, 1).toUpperCase(Locale.ENGLISH) + word.substring(1);

			if (i > 0) {
				prettyName.append(" ");
			}

			prettyName.append(word);
		}

		return prettyName.toString();
	}

	public @NonNull String getEssenceName(@NonNull String input) {
		if (!input.contains("_")) return input;

		String essenceName = input.split("_")[1];
		String prettyName = essenceName.substring(0, 1).toUpperCase(Locale.ENGLISH) + essenceName.substring(1).toLowerCase(Locale.ENGLISH);
		return prettyName + " Essence";
	}

	public int getTotalBlacklisted() {
		return totalBlacklisted;
	}

	public void resetTotalBlacklisted() {
		totalBlacklisted = 0;
	}
}
