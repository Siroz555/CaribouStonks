package fr.siroz.cariboustonks.core.skyblock.item.metadata;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the {@code modifiers} applied to an item.
 *
 * @param rarityUpgrades     the number of rarity upgrades (see {@link #isRecombobulated})
 * @param upgradeLevel       the upgrade level (Stars and Master Stars)
 * @param enrichment         the enrichment applied (Talisman/Accessories)
 * @param woodSingularity    the Wood Singularity (Axes)
 * @param manaDisintegrators the Mana Disintegrator (Deployables / Wands)
 * @param transmissionTuners Transmission Tuner (Boost Transmission ability range)
 * @param ethermerge         the ethermerge (Ether Transmission Ability)
 * @param pocketSackInASack  the pocket Sack-in-a-Sack (Sack Capacity Upgrade)
 * @param divanPowderCoating the divan powder coating (Divan Upgrade, Drill + Armure? J'ai que la Drill)
 * @param powerScroll        the power scroll (Power Ability)
 * @param abilityScrolls     the ability scrolls (Wither Scrolls)
 * @param dungeonItemTier    the dungeon tier (Mobs Drops)
 * @param dungeonItemLevel   the dungeon level (Essence Upgrades)
 * @param boosters           the boosters (Boosters)
 */
public record Modifiers(
		int rarityUpgrades,
		int upgradeLevel,
		Optional<String> enrichment,
		OptionalInt woodSingularity,
		OptionalInt manaDisintegrators,
		OptionalInt transmissionTuners,
		OptionalInt ethermerge,
		OptionalInt pocketSackInASack,
		OptionalInt divanPowderCoating,
		Optional<String> powerScroll,
		Optional<List<String>> abilityScrolls,
		OptionalInt dungeonItemTier,
		OptionalInt dungeonItemLevel,
		Optional<List<String>> boosters,
		OptionalInt overclockers
) {

	public static final Modifiers EMPTY = new Modifiers(
			0,
			0,
			Optional.empty(),
			OptionalInt.empty(),
			OptionalInt.empty(),
			OptionalInt.empty(),
			OptionalInt.empty(),
			OptionalInt.empty(),
			OptionalInt.empty(),
			Optional.empty(),
			Optional.empty(),
			OptionalInt.empty(),
			OptionalInt.empty(),
			Optional.empty(),
			OptionalInt.empty()
	);

	public boolean isRecombobulated() {
		return rarityUpgrades == 1;
	}

	public static Modifiers ofNbt(@NotNull CompoundTag customData) {
		try {
			int rarityUpgradeData = customData.getIntOr("rarity_upgrades", 0);
			int upgradeLevelData = customData.getIntOr("upgrade_level", 0);
			Optional<String> enrichmentData = customData.getString("talisman_enrichment");
			OptionalInt woodSingularityData = customData.getInt("wood_singularity_count")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt manaDisintegratorData = customData.getInt("mana_disintegrator_count")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt transmissionTunerData = customData.getInt("tuned_transmission")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt ethermergeData = customData.getInt("ethermerge")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt pocketSackInASackData = customData.getInt("sack_pss")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt divanPowerCoatingData = customData.getInt("divan_powder_coating")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			Optional<String> powerScrollData = customData.getString("power_ability_scroll");
			Optional<List<String>> abilityScrollsData = customData.getList("ability_scroll")
					.map(list -> list.stream()
							.map(Tag::asString)
							.flatMap(Optional::stream)
							.toList())
					.filter(list -> !list.isEmpty());
			OptionalInt dungeonItemTierData = customData.getInt("item_tier")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			OptionalInt dungeonItemLevelData = customData.getInt("dungeon_item_level")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());
			Optional<List<String>> boostersData = customData.getList("boosters")
					.map(list -> list.stream()
							.map(Tag::asString)
							.flatMap(Optional::stream)
							.toList())
					.filter(list -> !list.isEmpty());
			OptionalInt overclockData = customData.getInt("levelable_overclocks")
					.map(OptionalInt::of)
					.orElse(OptionalInt.empty());

			return new Modifiers(
					rarityUpgradeData,
					upgradeLevelData,
					enrichmentData,
					woodSingularityData,
					manaDisintegratorData,
					transmissionTunerData,
					ethermergeData,
					pocketSackInASackData,
					divanPowerCoatingData,
					powerScrollData,
					abilityScrollsData,
					dungeonItemTierData,
					dungeonItemLevelData,
					boostersData,
					overclockData
			);
		} catch (Exception ignored) {
			return EMPTY;
		}
	}
}
