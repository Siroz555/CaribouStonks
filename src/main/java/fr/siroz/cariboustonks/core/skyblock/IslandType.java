package fr.siroz.cariboustonks.core.skyblock;

import java.util.Arrays;
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
	ANY("#any#", "Any", false, false),

	/**
	 * Private Island - Personal Island & Guest
	 */
	PRIVATE_ISLAND("dynamic", "Private Island", false, false),

	/**
	 * Hub
	 */
	HUB("hub", "Hub", true, false),

	/**
	 * The Farming Islands
	 */
	THE_FARMING_ISLAND("farming_1", "The Farming Islands", false, false),

	/**
	 * Garden
	 */
	GARDEN("garden", "Garden", false, false),

	/**
	 * Gold Mine
	 */
	GOLD_MINE("mining_1", "Gold Mine", false, false),

	/**
	 * Deep Caverns
	 */
	DEEP_CAVERNS("mining_2", "Deep Caverns", false, false),

	/**
	 * Dwarven Mines
	 */
	DWARVEN_MINES("mining_3", "Dwarven Mines", false, false),

	/**
	 * Crystal Hollows
	 */
	CRYSTAL_HOLLOWS("crystal_hollows", "Crystal Hollows", false, false),

	/**
	 * Mineshaft
	 */
	GLACITE_MINESHAFT("mineshaft", "Mineshaft", false, false), // need to check

	/**
	 * Spider's Den
	 */
	SPIDER_DEN("combat_1", "Spider's Den", true, false),

	/**
	 * Nether
	 */
	@Deprecated
	BLAZING_FORTRESS("combat_2", "Nether", false, false),

	/**
	 * The End
	 */
	THE_END("combat_3", "The End", false, false),

	/**
	 * Crimson Isle
	 */
	CRIMSON_ISLE("crimson_isle", "Crimson Isle", true, false),

	/**
	 * Kuudra's Hollow
	 */
	KUUDRA_HOLLOW("kuudra", "Kuudra's Hollow", false, false),

	/**
	 * Dungeon Hub
	 */
	DUNGEON_HUB("dungeon_hub", "Dungeon Hub", false, false),

	/**
	 * Dungeon
	 */
	DUNGEON("dungeon", "Dungeons", false, false), // -_-

	/**
	 * The Park
	 */
	THE_PARK("foraging_1", "The Park", true, true),

	/**
	 * Galatea
	 */
	GALATEA("foraging_2", "Galatea", false, true),

	/**
	 * Torrhus Canyon
	 */
	TORRHUS_CANYON("foraging_3", "Torrhus Canyon", true, true),

	/**
	 * Torrhus Canyon - Safari instance
	 */
	SAFARI("safari", "Safari", false, false), // Hunting, pas foraging

	/**
	 * Backwater Bayou
	 */
	BACKWATER_BAYOU("fishing_1", "Backwater Bayou", true, false),

	/**
	 * Lotus Atoll
	 */
	LOTUS_ATOLL("lotus_atoll", "Lotus Atoll", true, false),

	/**
	 * Jerry's Workshop
	 */
	JERRY_WORKSHOP("winter", "Jerry's Workshop", true, false),

	/**
	 * Dark Auction
	 */
	DARK_AUCTION("dark_auction", "Dark Auction", false, false),

	/**
	 * The Rift
	 */
	THE_RIFT("rift", "The Rift", false, false),

	/**
	 * Unknown
	 */
	UNKNOWN("unknown", "Unknown", false, false),
	;

	/**
	 * Represents an array of all possible {@link IslandType} constants.
	 * This array contains all predefined constants of the {@code Island} class in the order they are declared.
	 * It allows iterating over all available Islands or accessing specific ones by their index.
	 * <p>
	 * <b>Important:</b> {@code @Deprecated} constants are ignored.
	 */
	public static final IslandType[] VALUES;

	private final String id;
	private final String name;
	private final boolean hasHotspotFishing;
	private final boolean isForagingIsland;

	IslandType(String id, String name, boolean hasHotspotFishing, boolean isForagingIsland) {
		this.id = id;
		this.name = name;
		this.hasHotspotFishing = hasHotspotFishing;
		this.isForagingIsland = isForagingIsland;
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
	 * Returns the "display name"
	 *
	 * @return the display name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Checks if the Island has Hotspot for Fishing
	 *
	 * @return {@code true if the Island is an Fishing Island or with Hotspot}
	 */
	public boolean hasHotspotFishing() {
		return hasHotspotFishing;
	}

	/**
	 * Checks if the Island is a Foraging type Island
	 *
	 * @return {@code true} if the Island is a Foraging Island
	 */
	public boolean isForagingIsland() {
		return isForagingIsland;
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
		return name;
	}

	/**
	 * Returns the {@link IslandType} from the given id.
	 *
	 * @param id the Island id
	 * @return the Island or {@link IslandType#UNKNOWN} if not found
	 */
	public static @NonNull IslandType getById(@Nullable String id) {
		if (id == null || id.isEmpty()) return UNKNOWN;

		return Arrays.stream(IslandType.values())
				.filter(is -> id.equals(is.getId()))
				.findFirst()
				.orElse(UNKNOWN);
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
