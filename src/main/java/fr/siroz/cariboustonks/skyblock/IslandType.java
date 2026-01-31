package fr.siroz.cariboustonks.skyblock;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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
	@ApiStatus.Internal
	ANY("#any#", "Any"),

	/**
	 * Private Island - Personal Island & Guest
	 */
	PRIVATE_ISLAND("dynamic", "Private Island"),

	/**
	 * Hub
	 */
	HUB("hub", "Hub"),

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
	SPIDER_DEN("combat_1", "Spider's Den"),

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
	CRIMSON_ISLE("crimson_isle", "Crimson Isle"),

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
	THE_PARK("foraging_1", "The Park"),

	/**
	 * The Foraging Update - Galatea
	 */
	GALATEA("foraging_2", "Galatea"),

	/**
	 * Backwater Bayou
	 */
	BACKWATER_BAYOU("fishing_1", "Backwater Bayou"),

	/**
	 * Jerry's Workshop
	 */
	JERRY_WORKSHOP("winter", "Jerry's Workshop"),

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
	 * Represents an array of all possible {@link IslandType} constants.
	 * This array contains all predefined constants of the {@code Island} class in the order they are declared.
	 * It allows iterating over all available Islands or accessing specific ones by their index.
	 * <p>
	 * <b>Important:</b> {@code @Deprecated} constants are ignored.
	 */
	public static final IslandType[] VALUES;

	private final String id;
	private final String name;

	IslandType(String id, String name) {
		this.id = id;
		this.name = name;
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
	public static @NotNull IslandType getById(@Nullable String id) {
		if (id == null || id.isEmpty()) {
			return UNKNOWN;
		}

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
					} catch (NoSuchFieldException ignored) {
						return true;
					}
				})
				.toArray(IslandType[]::new);
	}
}
