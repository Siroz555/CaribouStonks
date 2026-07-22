package fr.siroz.cariboustonks.core.skyblock;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * All SkyBlock Islands
 * <p>
 * <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
 * <p>
 * {@link #values()} return <b>all</b> Island constants.
 * <p>
 * {@link #VALUES} return <b>non-deprecated</b> Island constants.
 */
public enum IslandType {
	/**
	 * Any
	 */
	ANY("#any#", "Any"),

	/**
	 * Private Island - Personal Island & Guest
	 */
	PRIVATE_ISLAND("dynamic", "Private Island"),

	/**
	 * Hub
	 */
	HUB("hub", "Hub", Trait.HOTSPOT_FISHING),

	/**
	 * The Farming Islands
	 */
	THE_FARMING_ISLAND("farming_1", "The Farming Islands"),

	/**
	 * Garden
	 */
	GARDEN("garden", "Garden"),

	/**
	 * Gold Mine
	 */
	GOLD_MINE("mining_1", "Gold Mine"),

	/**
	 * Deep Caverns
	 */
	DEEP_CAVERNS("mining_2", "Deep Caverns"),

	/**
	 * Dwarven Mines
	 */
	DWARVEN_MINES("mining_3", "Dwarven Mines"),

	/**
	 * Crystal Hollows
	 */
	CRYSTAL_HOLLOWS("crystal_hollows", "Crystal Hollows"),

	/**
	 * Mineshaft
	 */
	GLACITE_MINESHAFT("mineshaft", "Mineshaft"), // need to check

	/**
	 * Spider's Den
	 */
	SPIDER_DEN("combat_1", "Spider's Den", Trait.HOTSPOT_FISHING),

	/**
	 * Nether
	 */
	@Deprecated
	BLAZING_FORTRESS("combat_2", "Nether"),

	/**
	 * The End
	 */
	THE_END("combat_3", "The End"),

	/**
	 * Crimson Isle
	 */
	CRIMSON_ISLE("crimson_isle", "Crimson Isle", Trait.HOTSPOT_FISHING),

	/**
	 * Kuudra's Hollow
	 */
	KUUDRA_HOLLOW("kuudra", "Kuudra's Hollow"),

	/**
	 * Dungeon Hub
	 */
	DUNGEON_HUB("dungeon_hub", "Dungeon Hub"),

	/**
	 * Dungeon
	 */
	DUNGEON("dungeon", "Dungeons"), // -_-

	/**
	 * The Park
	 */
	THE_PARK("foraging_1", "The Park", Trait.HOTSPOT_FISHING, Trait.FORAGING),

	/**
	 * Galatea
	 */
	GALATEA("foraging_2", "Galatea", Trait.FORAGING),

	/**
	 * Torrhus Canyon
	 */
	TORRHUS_CANYON("foraging_3", "Torrhus Canyon", Trait.HOTSPOT_FISHING, Trait.FORAGING),

	/**
	 * Torrhus Canyon - Safari instance
	 */
	SAFARI("safari", "Safari"), // Hunting, pas foraging

	/**
	 * Backwater Bayou
	 */
	BACKWATER_BAYOU("fishing_1", "Backwater Bayou", Trait.HOTSPOT_FISHING),

	/**
	 * Lotus Atoll
	 */
	LOTUS_ATOLL("lotus_atoll", "Lotus Atoll", Trait.HOTSPOT_FISHING),

	/**
	 * Jerry's Workshop
	 */
	JERRY_WORKSHOP("winter", "Jerry's Workshop", Trait.HOTSPOT_FISHING),

	/**
	 * Dark Auction
	 */
	DARK_AUCTION("dark_auction", "Dark Auction"),

	/**
	 * The Rift
	 */
	THE_RIFT("rift", "The Rift"),

	/**
	 * Unknown
	 */
	UNKNOWN("unknown", "Unknown"),
	;

	/**
	 * Island's Trait
	 */
	public enum Trait {
		/**
		 * The Island has Hotspots Fishing
		 */
		HOTSPOT_FISHING,
		/**
		 * The Island is a Foraging Type Island
		 */
		FORAGING,
	}

	/**
	 * Represents an array of all possible {@link IslandType} constants.
	 * This array contains all predefined constants of the {@code Island} class in the order they are declared.
	 * It allows iterating over all available Islands or accessing specific ones by their index.
	 * <p>
	 * <b>Important:</b> {@code @Deprecated} constants are ignored.
	 */
	public static final IslandType[] VALUES;
	private static final Map<String, IslandType> BY_ID = Arrays.stream(values())
			.collect(Collectors.toMap(IslandType::getId, Function.identity()));

	private final String id;
	private final String displayName;
	private final Set<Trait> traits;

	IslandType(String id, String displayName, Trait... traits) {
		this.id = id;
		this.displayName = displayName;
		this.traits = traits.length == 0 ? EnumSet.noneOf(Trait.class) : EnumSet.copyOf(Arrays.asList(traits));
	}

	/**
	 * Returns the Island id
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the display name of the Island
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Checks if the Island has a specific {@link Trait}
	 *
	 * @param trait the trait
	 * @return {@code true} if the Island has the trait
	 */
	public boolean hasTrait(@NonNull Trait trait) {
		return traits.contains(trait);
	}

	/**
	 * Returns the string representation of this Island.
	 * <p>
	 * -> {@code "Private Island"}}
	 *
	 * @return the display name of the Island
	 */
	@Override
	public String toString() {
		return displayName;
	}

	/**
	 * Returns the {@link IslandType} from the given id.
	 *
	 * @param id the Island id
	 * @return the Island or {@link IslandType#UNKNOWN} if not found
	 */
	public static @NonNull IslandType getById(@Nullable String id) {
		if (id == null) return UNKNOWN;
		return BY_ID.getOrDefault(id, UNKNOWN);
	}

	static {
		VALUES = Arrays.stream(IslandType.values())
				.filter(is -> {
					try {
						return IslandType.class.getField(is.name()).getAnnotation(Deprecated.class) == null;
					} catch (NoSuchFieldException _) {
						return true;
					}
				})
				.toArray(IslandType[]::new);
	}
}
