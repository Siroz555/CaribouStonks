package fr.siroz.cariboustonks.core.skyblock.data.hypixel.election;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * All SkyBlock Mayors. <a href="https://wiki.hypixel.net/Mayors">Hypixel Wiki Mayors</a>
 */
public enum Mayor {
	AATROX("SLAYER", "Aatrox", EnumSet.of(Perk.SLAYER_XP_BUFF, Perk.PATHFINDER, Perk.SLASHED_PRICING)),
	COLE("MINING", "Cole", EnumSet.of(Perk.MINING_FIESTA, Perk.MINING_XP_BUFF, Perk.MOLTEN_FORGE, Perk.PROSPECTION)),
	DIANA("PETS", "Diana", EnumSet.of(Perk.PET_XP_BUFF, Perk.LUCKY, Perk.MYTHOLOGICAL_RITUAL, Perk.SHARING_IS_CARING)),
	DIAZ("ECONOMIST", "Diaz", EnumSet.of(Perk.LONG_TERM_INVESTMENT, Perk.SHOPPING_SPREE, Perk.STOCK_EXCHANGE, Perk.VOLUME_TRADING)),
	FINNEGAN("FARMING", "Finnegan", EnumSet.of(Perk.BLOOMING_BUSINESS, Perk.GOATED, Perk.PELT_POCALYPSE, Perk.PEST_ERADICATOR)),
	FOXY("EVENTS", "Foxy", EnumSet.of(Perk.A_TIME_FOR_GIVING, Perk.CHIVALROUS_CARNIVAL, Perk.EXTRA_EVENT, Perk.SWEET_BENEVOLENCE)),
	MARINA("FISHING", "Marina", EnumSet.of(Perk.DOUBLE_TROUBLE, Perk.FISHING_XP_BUFF, Perk.FISHING_FESTIVAL, Perk.LUCK_OF_THE_SEA_2_0)),
	PAUL("DUNGEONS", "Paul", EnumSet.of(Perk.BENEDICTION, Perk.MARAUDER, Perk.EZPZ)),
	// Special
	JERRY("JERRY", "Jerry", Collections.emptySet(), true),
	DERPY("DERP", "Derpy", Collections.emptySet(), true),
	SCORPIUS("SHADY", "Scorpius", Collections.emptySet(), true),
    UNKNOWN("", "", Collections.emptySet());

    private static final Map<String, Mayor> BY_ID = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Mayor::getId, Function.identity()));

	private final String id;
	private final String displayName;
	private final Set<Perk> allowedPerks;
	private final boolean special;

	Mayor(String id, String displayName, Set<Perk> allowedPerks) {
		this(id, displayName, allowedPerks, false);
	}

	Mayor(String id, String displayName, Set<Perk> allowedPerks, boolean special) {
		this.id = id;
		this.displayName = displayName;
		this.allowedPerks = allowedPerks;
		this.special = special;
	}

    public static @NotNull Mayor fromId(@Nullable String id) {
		if (id == null) return UNKNOWN;
        return BY_ID.getOrDefault(id.toUpperCase(Locale.ROOT), UNKNOWN);
    }

	public boolean allows(Perk perk) {
		return allowedPerks.contains(perk);
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Set<Perk> getAllowedPerks() {
		return allowedPerks;
	}

	public boolean isSpecial() {
		return special;
	}
}
