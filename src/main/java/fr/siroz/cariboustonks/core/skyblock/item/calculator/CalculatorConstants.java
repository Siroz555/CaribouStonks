package fr.siroz.cariboustonks.core.skyblock.item.calculator;

import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Constants for the {@link ItemValueCalculator}.
 *
 * <h2>Worth Mapping</h2>
 * See <a href="https://github.com/Altpapier/SkyHelper-Networth/blob/master/constants/applicationWorth.js">SkyHelper Networth Calculator GitHub</a>
 *
 * <h2> Enchantment Upgrades</h2>
 * <a href="https://wiki.hypixel.net/Enchantments">Hypixel Wiki Enchantments</a>
 *
 * <h2>Reforges Mapping</h2>
 * <a href="https://wiki.hypixel.net/Reforging">Hypixel Wiki Reforging</a>
 */
public interface CalculatorConstants {

	/**
	 * Represents the worth of an item type.
	 * <p>
	 * SIROZ-NOTE : J'ai volontairement modifié certaine valeur de SkyHelper, pour avoir de meilleurs résultats.
	 * Avec les mises à jour du SkyBlock, certain item type ont de meilleurs prix (moins volatil) (selon moi).
	 */
	Object2DoubleMap<String> WORTH = Object2DoubleMaps.unmodifiable(StonksUtils.make(new Object2DoubleOpenHashMap<>(), map -> {
		// Cosmetics
		map.put("skins", 0.67); // Community (-33% when applied)
		map.put("dye", 0.9);
		map.put("runes", 0.6);
		// Pets
		map.put("petItem", 1);
		// Special Auction
		map.put("specialAuctionPrice", 1);
		map.put("winningBid", 1);
		// Base
		map.put("reforge", 1);
		// Enchantements
		map.put("enchantments", 0.95); // 0.85
		map.put("enchantmentUpgrades", 0.95); // 0.8
		// Books
		map.put("fumingPotatoBook", 0.75); // 0.6
		map.put("hotPotatoBook", 1);
		map.put("artOfWar", 0.8); // 0.6
		map.put("artOfPeace", 0.8);
		map.put("farmingForDummies", 0.6); // 0.5
		map.put("polarvoidBook", 0.8); // 1
		map.put("jalapenoBook", 0.8);
		// Modifiers
		map.put("recombobulator", 0.95); // 0.8
		map.put("enrichment", 0.75); // 0.5
		map.put("woodSingularity", 0.7); // 0.5
		map.put("manaDisintegrator", 0.8);
		map.put("transmissionTuner", 0.7);
		map.put("etherwarp", 1);
		map.put("pocketSackInASack", 0.85); // 0.7
		map.put("divanPowderCoating", 0.8);
		map.put("powerScroll", 0.6); // 0.5
		map.put("witherScroll", 1);
		map.put("overclocker", 0.8); // -
		// Gemstones
		map.put("gemstones", 1);
		map.put("gemstoneSlots", 0.75); // 0.6
		// Parts
		map.put("drillPart", 1);
		map.put("rodPart", 1);
		// Boosters
		map.put("boosters", 0.8);
		// Others
		map.put("essence", 0.9); // 0.9
		map.put("masterStars", 1);
		// Default
		map.defaultReturnValue(1d);
	}));

	Object2ObjectMap<String, Map<Integer, String>> ENCHANTMENT_UPGRADES = Object2ObjectMaps.unmodifiable(StonksUtils.make(new Object2ObjectOpenHashMap<>(), map -> {
		map.put("SCAVENGER", Map.of(6, "GOLDEN_BOUNTY"));
		map.put("PESTERMINATOR", Map.of(6, "PESTHUNTING_GUIDE"));
		map.put("LUCK_OF_THE_SEA", Map.of(7, "GOLD_BOTTLE_CAP"));
		map.put("PISCARY", Map.of(7, "TROUBLED_BUBBLE"));
		map.put("FRAIL", Map.of(7, "SEVERED_PINCER"));
		map.put("SPIKED_HOOK", Map.of(7, "OCTOPUS_TENDRIL"));
		map.put("CHARM", Map.of(6, "CHAIN_END_TIMES"));
		map.put("SMITE", Map.of(7, "SEVERED_HAND"));
		map.put("ENDER_SLAYER", Map.of(7, "ENDSTONE_IDOL"));
		map.put("BANE_OF_ARTHROPODS", Map.of(7, "ENSNARED_SNAIL"));
		map.put("VENOMOUS", Map.of(7, "FATEFUL_STINGER"));
	}));

	List<String> MASTER_STARS = List.of(
			"FIRST_MASTER_STAR",
			"SECOND_MASTER_STAR",
			"THIRD_MASTER_STAR",
			"FOURTH_MASTER_STAR",
			"FIFTH_MASTER_STAR"
	);

	Map<String, Integer> ULTIMATE_BASE_LEVELS = Map.of(
			"ULTIMATE_THE_ONE", 4,
			"ULTIMATE_BOBBIN_TIME", 3
	);

	Set<String> STACKING_ENCHANTMENTS = Set.of(
			"EXPERTISE",
			"COMPACT",
			"CULTIVATING",
			"CHAMPION",
			"HECATOMB",
			"TOXOPHILITE"
	);

	Object2ObjectMap<String, Pair<Long, String>> MIDAS_WEAPONS = Object2ObjectMaps.unmodifiable(StonksUtils.make(new Object2ObjectOpenHashMap<>(), map -> {
		map.put("MIDAS_SWORD", Pair.of(50_000_000L, "MIDAS_SWORD_50M"));
		map.put("STARRED_MIDAS_SWORD", Pair.of(250_000_000L, "STARRED_MIDAS_SWORD_250M"));
		map.put("MIDAS_STAFF", Pair.of(100_000_000L, "MIDAS_STAFF_100M"));
		map.put("STARRED_MIDAS_STAFF", Pair.of(500_000_000L, "STARRED_MIDAS_STAFF_500M"));
	}));

	Set<String> ENRICHMENTS = Set.of(
			"TALISMAN_ENRICHMENT_ATTACK_SPEED",
			"TALISMAN_ENRICHMENT_CRITICAL_CHANCE",
			"TALISMAN_ENRICHMENT_CRITICAL_DAMAGE",
			"TALISMAN_ENRICHMENT_DEFENSE",
			"TALISMAN_ENRICHMENT_FEROCITY",
			"TALISMAN_ENRICHMENT_HEALTH",
			"TALISMAN_ENRICHMENT_INTELLIGENCE",
			"TALISMAN_ENRICHMENT_MAGIC_FIND",
			"TALISMAN_ENRICHMENT_SEA_CREATURE_CHANCE",
			"TALISMAN_ENRICHMENT_STRENGTH",
			"TALISMAN_ENRICHMENT_WALK_SPEED"
	);

	Object2ObjectMap<String, String> REFORGES = Object2ObjectMaps.unmodifiable(StonksUtils.make(new Object2ObjectOpenHashMap<>(), map -> {
		// Unavailable from Bazaar
		map.put("dirty", "DIRT_BOTTLE");
		map.put("moil", "MOIL_LOG");
		map.put("toil", "TOIL_LOG");
		map.put("greater_spook", "BOO_STONE");
		// Swords https://wiki.hypixel.net/Reforging#Swords_
		map.put("aote_stone", "AOTE_STONE");
		map.put("bulky", "BULKY_STONE");
		map.put("coldfusion", "ENTROPY_SUPPRESSOR");
		map.put("fabled", "DRAGON_CLAW");
		map.put("fanged", "FULL_JAW_FANGED_KIT");
		map.put("gilded", "MIDAS_JEWEL");
		map.put("jerry", "JERRY_STONE");
		map.put("suspicious", "SUSPICIOUS_VIAL");
		map.put("withered", "WITHER_BLOOD");
		// Bows (https://wiki.hypixel.net/Reforging#Bows_)
		map.put("headstrong", "SALMON_OPAL");
		map.put("precise", "OPTICAL_LENS");
		map.put("spiritual", "SPIRIT_DECOY");
		// Armor (https://wiki.hypixel.net/Reforging#Armor_)
		map.put("ancient", "PRECURSOR_GEAR");
		map.put("bustling", "SKYMART_BROCHURE");
		map.put("calcified", "CALCIFIED_HEART");
		map.put("candied", "CANDY_CORN");
		map.put("loving", "RED_SCARF");
		map.put("cubic", "MOLTEN_CUBE");
		map.put("dimensional", "TITANIUM_TESSERACT");
		map.put("empowered", "SADAN_BROOCH");
		map.put("festive", "FROZEN_BAUBLE");
		map.put("giant", "GIANT_TOOTH");
		map.put("groovy", "MANGROVE_GEM");
		map.put("ridiculous", "RED_NOSE");
		map.put("jaded", "JADERALD");
		map.put("mossy", "OVERGROWN_GRASS");
		map.put("necrotic", "NECROMANCER_BROOCH");
		map.put("perfect", "DIAMOND_ATOM");
		map.put("reinforced", "RARE_DIAMOND");
		map.put("renowned", "DRAGON_HORN");
		map.put("spiked", "DRAGON_SCALE");
		map.put("submerged", "DEEP_SEA_ORB");
		map.put("undead", "PREMIUM_FLESH");
		map.put("hyper", "ENDSTONE_GEODE");
		map.put("sunny", "SUNSTONE");
		// Equipment (https://wiki.hypixel.net/Reforging#Equipment_)
		map.put("blazing", "BLAZEN_SPHERE");
		map.put("blood_soaked", "PRESUMED_GALLON_OF_RED_PAINT");
		map.put("blood_shot", "SHRIVELED_CORNEA");
		map.put("blooming", "FLOWERING_BOUQUET");
		map.put("fortified", "METEOR_SHARD");
		map.put("glistening", "SHINY_PRISM");
		map.put("rooted", "BURROWING_SPORES");
		map.put("royal", "DWARVEN_TREASURE");
		map.put("snowy", "TERRY_SNOWGLOBE");
		map.put("squeaky", "SQUEAKY_TOY");
		map.put("strengthened", "SEARING_STONE");
		map.put("waxed", "BLAZE_WAX");
		map.put("lunar", "MOONSTONE");
		// Fishing Rods (https://wiki.hypixel.net/Reforging#Fishing_Rods_)
		map.put("chomp", "KUUDRA_MANDIBLE");
		map.put("lucky", "LUCKY_DICE");
		map.put("pitchin", "PITCHIN_KOI");
		map.put("salty", "SALT_CUBE");
		map.put("stiff", "HARDENED_WOOD");
		map.put("trashy", "OVERFLOWING_TRASH_CAN");
		map.put("treacherous", "RUSTY_ANCHOR");
		// Pickaxes (https://wiki.hypixel.net/Reforging#Pickaxes_)
		map.put("ambered", "AMBER_MATERIAL");
		map.put("auspicious", "ROCK_GEMSTONE");
		map.put("fleet", "DIAMONITE");
		map.put("fruitful", "ONYX");
		map.put("glacial", "FRIGID_HUSK");
		map.put("heated", "HOT_STUFF");
		map.put("magnetic", "LAPIS_CRYSTAL");
		map.put("mithraic", "PURE_MITHRIL");
		map.put("refined", "REFINED_AMBER");
		map.put("scraped", "POCKET_ICEBERG");
		map.put("stellar", "PETRIFIED_STARFALL");
		map.put("lustrous", "GLEAMING_CRYSTAL");
		// Axes (https://wiki.hypixel.net/Reforging#Axes_)
		map.put("earthy", "LARGE_WALNUT");
		map.put("moonglade", "MOONGLADE_JEWEL");
		// Hoes (https://wiki.hypixel.net/Reforging#Hoes_)
		map.put("bountiful", "GOLDEN_BALL"); // (Hoes / Axes)
		map.put("blessed", "BLESSED_FRUIT"); // (Hoes / Axe)
		// Vacuums (https://wiki.hypixel.net/Reforging#Vacuums_)
		map.put("beady", "BEADY_EYES");
		map.put("buzzing", "CLIPPED_WINGS");
		// Spades
		map.put("erudite", "DAEDALUS_NOTES");
	}));

	/**
	 * Represents all prestiges on Kuudra Armors.
	 * <p>
	 * SIROZ-NOTE : J'ai volontairement retiré l'armure Hollow et Fervor
	 */
	Object2ObjectMap<String, Set<String>> PRESTIGES = Object2ObjectMaps.unmodifiable(StonksUtils.make(new Object2ObjectOpenHashMap<>(), map -> {
		// Crimson
		map.put("HOT_CRIMSON_HELMET", Set.of("CRIMSON_HELMET"));
		map.put("HOT_CRIMSON_CHESTPLATE", Set.of("CRIMSON_CHESTPLATE"));
		map.put("HOT_CRIMSON_LEGGINGS", Set.of("CRIMSON_LEGGINGS"));
		map.put("HOT_CRIMSON_BOOTS", Set.of("CRIMSON_BOOTS"));

		map.put("BURNING_CRIMSON_HELMET", Set.of("HOT_CRIMSON_HELMET", "CRIMSON_HELMET"));
		map.put("BURNING_CRIMSON_CHESTPLATE", Set.of("HOT_CRIMSON_CHESTPLATE", "CRIMSON_CHESTPLATE"));
		map.put("BURNING_CRIMSON_LEGGINGS", Set.of("HOT_CRIMSON_LEGGINGS", "CRIMSON_LEGGINGS"));
		map.put("BURNING_CRIMSON_BOOTS", Set.of("HOT_CRIMSON_BOOTS", "CRIMSON_BOOTS"));

		map.put("FIERY_CRIMSON_HELMET", Set.of("BURNING_CRIMSON_HELMET", "HOT_CRIMSON_HELMET", "CRIMSON_HELMET"));
		map.put("FIERY_CRIMSON_CHESTPLATE", Set.of("BURNING_CRIMSON_CHESTPLATE", "HOT_CRIMSON_CHESTPLATE", "CRIMSON_CHESTPLATE"));
		map.put("FIERY_CRIMSON_LEGGINGS", Set.of("BURNING_CRIMSON_LEGGINGS", "HOT_CRIMSON_LEGGINGS", "CRIMSON_LEGGINGS"));
		map.put("FIERY_CRIMSON_BOOTS", Set.of("BURNING_CRIMSON_BOOTS", "HOT_CRIMSON_BOOTS", "CRIMSON_BOOTS"));

		map.put("INFERNAL_CRIMSON_HELMET", Set.of("FIERY_CRIMSON_HELMET", "BURNING_CRIMSON_HELMET", "HOT_CRIMSON_HELMET", "CRIMSON_HELMET"));
		map.put("INFERNAL_CRIMSON_CHESTPLATE", Set.of("FIERY_CRIMSON_CHESTPLATE", "BURNING_CRIMSON_CHESTPLATE", "HOT_CRIMSON_CHESTPLATE", "CRIMSON_CHESTPLATE"));
		map.put("INFERNAL_CRIMSON_LEGGINGS", Set.of("FIERY_CRIMSON_LEGGINGS", "BURNING_CRIMSON_LEGGINGS", "HOT_CRIMSON_LEGGINGS", "CRIMSON_LEGGINGS"));
		map.put("INFERNAL_CRIMSON_BOOTS", Set.of("FIERY_CRIMSON_BOOTS", "BURNING_CRIMSON_BOOTS", "HOT_CRIMSON_BOOTS", "CRIMSON_BOOTS"));

		// Aurora
		map.put("HOT_AURORA_HELMET", Set.of("AURORA_HELMET"));
		map.put("HOT_AURORA_CHESTPLATE", Set.of("AURORA_CHESTPLATE"));
		map.put("HOT_AURORA_LEGGINGS", Set.of("AURORA_LEGGINGS"));
		map.put("HOT_AURORA_BOOTS", Set.of("AURORA_BOOTS"));

		map.put("BURNING_AURORA_HELMET", Set.of("HOT_AURORA_HELMET", "AURORA_HELMET"));
		map.put("BURNING_AURORA_CHESTPLATE", Set.of("HOT_AURORA_CHESTPLATE", "AURORA_CHESTPLATE"));
		map.put("BURNING_AURORA_LEGGINGS", Set.of("HOT_AURORA_LEGGINGS", "AURORA_LEGGINGS"));
		map.put("BURNING_AURORA_BOOTS", Set.of("HOT_AURORA_BOOTS", "AURORA_BOOTS"));

		map.put("FIERY_AURORA_HELMET", Set.of("BURNING_AURORA_HELMET", "HOT_AURORA_HELMET", "AURORA_HELMET"));
		map.put("FIERY_AURORA_CHESTPLATE", Set.of("BURNING_AURORA_CHESTPLATE", "HOT_AURORA_CHESTPLATE", "AURORA_CHESTPLATE"));
		map.put("FIERY_AURORA_LEGGINGS", Set.of("BURNING_AURORA_LEGGINGS", "HOT_AURORA_LEGGINGS", "AURORA_LEGGINGS"));
		map.put("FIERY_AURORA_BOOTS", Set.of("BURNING_AURORA_BOOTS", "HOT_AURORA_BOOTS", "AURORA_BOOTS"));

		map.put("INFERNAL_AURORA_HELMET", Set.of("FIERY_AURORA_HELMET", "BURNING_AURORA_HELMET", "HOT_AURORA_HELMET", "AURORA_HELMET"));
		map.put("INFERNAL_AURORA_CHESTPLATE", Set.of("FIERY_AURORA_CHESTPLATE", "BURNING_AURORA_CHESTPLATE", "HOT_AURORA_CHESTPLATE", "AURORA_CHESTPLATE"));
		map.put("INFERNAL_AURORA_LEGGINGS", Set.of("FIERY_AURORA_LEGGINGS", "BURNING_AURORA_LEGGINGS", "HOT_AURORA_LEGGINGS", "AURORA_LEGGINGS"));
		map.put("INFERNAL_AURORA_BOOTS", Set.of("FIERY_AURORA_BOOTS", "BURNING_AURORA_BOOTS", "HOT_AURORA_BOOTS", "AURORA_BOOTS"));

		// Terror
		map.put("HOT_TERROR_HELMET", Set.of("TERROR_HELMET"));
		map.put("HOT_TERROR_CHESTPLATE", Set.of("TERROR_CHESTPLATE"));
		map.put("HOT_TERROR_LEGGINGS", Set.of("TERROR_LEGGINGS"));
		map.put("HOT_TERROR_BOOTS", Set.of("TERROR_BOOTS"));

		map.put("BURNING_TERROR_HELMET", Set.of("HOT_TERROR_HELMET", "TERROR_HELMET"));
		map.put("BURNING_TERROR_CHESTPLATE", Set.of("HOT_TERROR_CHESTPLATE", "TERROR_CHESTPLATE"));
		map.put("BURNING_TERROR_LEGGINGS", Set.of("HOT_TERROR_LEGGINGS", "TERROR_LEGGINGS"));
		map.put("BURNING_TERROR_BOOTS", Set.of("HOT_TERROR_BOOTS", "TERROR_BOOTS"));

		map.put("FIERY_TERROR_HELMET", Set.of("BURNING_TERROR_HELMET", "HOT_TERROR_HELMET", "TERROR_HELMET"));
		map.put("FIERY_TERROR_CHESTPLATE", Set.of("BURNING_TERROR_CHESTPLATE", "HOT_TERROR_CHESTPLATE", "TERROR_CHESTPLATE"));
		map.put("FIERY_TERROR_LEGGINGS", Set.of("BURNING_TERROR_LEGGINGS", "HOT_TERROR_LEGGINGS", "TERROR_LEGGINGS"));
		map.put("FIERY_TERROR_BOOTS", Set.of("BURNING_TERROR_BOOTS", "HOT_TERROR_BOOTS", "TERROR_BOOTS"));

		map.put("INFERNAL_TERROR_HELMET", Set.of("FIERY_TERROR_HELMET", "BURNING_TERROR_HELMET", "HOT_TERROR_HELMET", "TERROR_HELMET"));
		map.put("INFERNAL_TERROR_CHESTPLATE", Set.of("FIERY_TERROR_CHESTPLATE", "BURNING_TERROR_CHESTPLATE", "HOT_TERROR_CHESTPLATE", "TERROR_CHESTPLATE"));
		map.put("INFERNAL_TERROR_LEGGINGS", Set.of("FIERY_TERROR_LEGGINGS", "BURNING_TERROR_LEGGINGS", "HOT_TERROR_LEGGINGS", "TERROR_LEGGINGS"));
		map.put("INFERNAL_TERROR_BOOTS", Set.of("FIERY_TERROR_BOOTS", "BURNING_TERROR_BOOTS", "HOT_TERROR_BOOTS", "TERROR_BOOTS"));
	}));
}
