package fr.siroz.cariboustonks.core.skyblock.data.hypixel.election;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public enum Perk {
	// Aatrox
	SLAYER_XP_BUFF("Slayer XP Buff"),
	PATHFINDER("Pathfinder"),
	SLASHED_PRICING("SLASHED Pricing"),
	// Cole
	MINING_FIESTA("Mining Festa"),
	MINING_XP_BUFF("Mining XP Buff"),
	MOLTEN_FORGE("Molten Forge"),
	PROSPECTION("Prospection"),
	// Diana
	PET_XP_BUFF("Pet XP Buff"),
	LUCKY("Lucky!"),
	MYTHOLOGICAL_RITUAL("Mythological Ritual"),
	SHARING_IS_CARING("Sharing is Caring"),
	// Diaz
	LONG_TERM_INVESTMENT("Long Term Investment"),
	SHOPPING_SPREE("Shopping Spree"),
	STOCK_EXCHANGE("Stock Exchange"),
	VOLUME_TRADING("Volume Trading"),
	// Finnegan
	BLOOMING_BUSINESS("Blooming Business"),
	GOATED("GOATed"),
	PELT_POCALYPSE("Pelt Pocalypse"),
	PEST_ERADICATOR("Pest Eradiator"),
	// Foxy
	A_TIME_FOR_GIVING("A Time for Giving"),
	CHIVALROUS_CARNIVAL("Chivalrous Carnival"),
	EXTRA_EVENT("Extra Event"),
	SWEET_BENEVOLENCE("Sweet Benevolence"),
	// Marina
	DOUBLE_TROUBLE("Double Trouble"),
	FISHING_XP_BUFF("Fishing XP Buff"),
	FISHING_FESTIVAL("Fishing Festival"),
	LUCK_OF_THE_SEA_2_0("Luck of the Sea 2.0"),
	// Paul
	BENEDICTION("Benediction"),
	MARAUDER("Marauder"),
	EZPZ("EZPZ"),
    UNKNOWN("");

    private static final Map<String, Perk> BY_DISPLAY_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Perk::getDisplayName, Function.identity()));

	private final String displayName;

	Perk(String displayName) {
		this.displayName = displayName;
	}

    public static @NonNull Perk fromDisplayName(@Nullable String displayName) {
		if (displayName == null) return UNKNOWN;
        return BY_DISPLAY_NAME.getOrDefault(displayName, UNKNOWN);
    }

	public String getDisplayName() {
		return displayName;
	}
}
