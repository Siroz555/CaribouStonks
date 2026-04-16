package fr.siroz.cariboustonks.core.skyblock;

import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.Rarity;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SkyBlock Constants.
 */
public interface SkyBlockConstants {

	/**
	 * Enchantment Upgrades.
	 * <p>
	 * Key: {@code enchantId} value: {@code maxLevel:upgradeId}
	 * <p>
	 * Example: {@code SCAVENGER} -> {@code (Level 6) GOLDEN_BOUNTY}
	 * <p>
	 * <a href="https://wiki.hypixel.net/Enchantments">Hypixel Wiki Enchantments</a>
	 */
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

	Map<String, Integer> ULTIMATE_BASE_LEVELS = Map.of(
			"ULTIMATE_THE_ONE", 4,
			"ULTIMATE_BOBBIN_TIME", 3,
			"ULTIMATE_HABANERO_TACTICS", 4
	);

	/**
	 * Reforges Mapping.
	 * <p>
	 * Key: {@code reforgeId} value: {@code skyBlockId}
	 * <p>
	 * Example: {@code withered} -> {@code WITHER_BLOOD}
	 * <p>
	 * <a href="https://wiki.hypixel.net/Reforging">Hypixel Wiki Reforging</a>
	 */
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

	List<String> MASTER_STARS = List.of(
			"FIRST_MASTER_STAR",
			"SECOND_MASTER_STAR",
			"THIRD_MASTER_STAR",
			"FOURTH_MASTER_STAR",
			"FIFTH_MASTER_STAR"
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

	Set<String> DUNGEON_CHESTS = Set.of(
			"Wood Chest", "Wood",
			"Gold Chest", "Gold",
			"Diamond Chest", "Diamond",
			"Emerald Chest", "Emerald",
			"Obsidian Chest", "Obsidian",
			"Bedrock Chest", "Bedrock"
	);

	Set<String> KUUDRA_CHESTS = Set.of(
			"Free Chest", "Free Chest Chest",
			"Paid Chest", "Paid Chest Chest"
	);

	/**
	 * Rarity to Attribute levels Mapping.
	 * <p>
	 * Key: {@code Rarity} value: {@code level:maxShard}
	 * <p>
	 * Example: {@code LEGENDARY} -> {@code 10:24}
	 */
	Map<Rarity, Int2IntMap> ATTRIBUTE_LEVELS = Collections.unmodifiableMap(StonksUtils.make(new EnumMap<>(Rarity.class), map -> {
		map.put(Rarity.COMMON, StonksUtils.make(new Int2IntArrayMap(), common -> {
			common.put(1, 1);
			common.put(2, 4);
			common.put(3, 9);
			common.put(4, 15);
			common.put(5, 22);
			common.put(6, 30);
			common.put(7, 40);
			common.put(8, 54);
			common.put(9, 72);
			common.put(10, 96);
		}));
		map.put(Rarity.UNCOMMON, StonksUtils.make(new Int2IntArrayMap(), uncommon -> {
			uncommon.put(1, 1);
			uncommon.put(2, 3);
			uncommon.put(3, 6);
			uncommon.put(4, 10);
			uncommon.put(5, 15);
			uncommon.put(6, 21);
			uncommon.put(7, 28);
			uncommon.put(8, 36);
			uncommon.put(9, 48);
			uncommon.put(10, 64);
		}));
		map.put(Rarity.RARE, StonksUtils.make(new Int2IntArrayMap(), rare -> {
			rare.put(1, 1);
			rare.put(2, 3);
			rare.put(3, 6);
			rare.put(4, 9);
			rare.put(5, 13);
			rare.put(6, 17);
			rare.put(7, 22);
			rare.put(8, 28);
			rare.put(9, 39);
			rare.put(10, 48);
		}));
		map.put(Rarity.EPIC, StonksUtils.make(new Int2IntArrayMap(), epic -> {
			epic.put(1, 1);
			epic.put(2, 2);
			epic.put(3, 4);
			epic.put(4, 6);
			epic.put(5, 9);
			epic.put(6, 12);
			epic.put(7, 16);
			epic.put(8, 20);
			epic.put(9, 25);
			epic.put(10, 32);
		}));
		map.put(Rarity.LEGENDARY, StonksUtils.make(new Int2IntArrayMap(), legendary -> {
			legendary.put(1, 1);
			legendary.put(2, 2);
			legendary.put(3, 3);
			legendary.put(4, 5);
			legendary.put(5, 7);
			legendary.put(6, 9);
			legendary.put(7, 12);
			legendary.put(8, 15);
			legendary.put(9, 19);
			legendary.put(10, 24);
		}));
	}));

	/**
	 * Represents all prestiges on Kuudra Armors.
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

		// Hollow Armour
		map.put("HOT_HOLLOW_HELMET", Set.of("HOLLOW_HELMET"));
		map.put("HOT_HOLLOW_CHESTPLATE", Set.of("HOLLOW_CHESTPLATE"));
		map.put("HOT_HOLLOW_LEGGINGS", Set.of("HOLLOW_LEGGINGS"));
		map.put("HOT_HOLLOW_BOOTS", Set.of("HOLLOW_BOOTS"));

		map.put("BURNING_HOLLOW_HELMET", Set.of("HOT_HOLLOW_HELMET", "HOLLOW_HELMET"));
		map.put("BURNING_HOLLOW_CHESTPLATE", Set.of("HOT_HOLLOW_CHESTPLATE", "HOLLOW_CHESTPLATE"));
		map.put("BURNING_HOLLOW_LEGGINGS", Set.of("HOT_HOLLOW_LEGGINGS", "HOLLOW_LEGGINGS"));
		map.put("BURNING_HOLLOW_BOOTS", Set.of("HOT_HOLLOW_BOOTS", "HOLLOW_BOOTS"));

		map.put("FIERY_HOLLOW_HELMET", Set.of("BURNING_HOLLOW_HELMET", "HOT_HOLLOW_HELMET", "HOLLOW_HELMET"));
		map.put("FIERY_HOLLOW_CHESTPLATE", Set.of("BURNING_HOLLOW_CHESTPLATE", "HOT_HOLLOW_CHESTPLATE", "HOLLOW_CHESTPLATE"));
		map.put("FIERY_HOLLOW_LEGGINGS", Set.of("BURNING_HOLLOW_LEGGINGS", "HOT_HOLLOW_LEGGINGS", "HOLLOW_LEGGINGS"));
		map.put("FIERY_HOLLOW_BOOTS", Set.of("BURNING_HOLLOW_BOOTS", "HOT_HOLLOW_BOOTS", "HOLLOW_BOOTS"));

		map.put("INFERNAL_HOLLOW_HELMET", Set.of("FIERY_HOLLOW_HELMET", "BURNING_HOLLOW_HELMET", "HOT_HOLLOW_HELMET", "HOLLOW_HELMET"));
		map.put("INFERNAL_HOLLOW_CHESTPLATE", Set.of("FIERY_HOLLOW_CHESTPLATE", "BURNING_HOLLOW_CHESTPLATE", "HOT_HOLLOW_CHESTPLATE", "HOLLOW_CHESTPLATE"));
		map.put("INFERNAL_HOLLOW_LEGGINGS", Set.of("FIERY_HOLLOW_LEGGINGS", "BURNING_HOLLOW_LEGGINGS", "HOT_HOLLOW_LEGGINGS", "HOLLOW_LEGGINGS"));
		map.put("INFERNAL_HOLLOW_BOOTS", Set.of("FIERY_HOLLOW_BOOTS", "BURNING_HOLLOW_BOOTS", "HOT_HOLLOW_BOOTS", "HOLLOW_BOOTS"));

		// Fervor Armour
		map.put("HOT_FERVOR_HELMET", Set.of("FERVOR_HELMET"));
		map.put("HOT_FERVOR_CHESTPLATE", Set.of("FERVOR_CHESTPLATE"));
		map.put("HOT_FERVOR_LEGGINGS", Set.of("FERVOR_LEGGINGS"));
		map.put("HOT_FERVOR_BOOTS", Set.of("FERVOR_BOOTS"));

		map.put("BURNING_FERVOR_HELMET", Set.of("HOT_FERVOR_HELMET", "FERVOR_HELMET"));
		map.put("BURNING_FERVOR_CHESTPLATE", Set.of("HOT_FERVOR_CHESTPLATE", "FERVOR_CHESTPLATE"));
		map.put("BURNING_FERVOR_LEGGINGS", Set.of("HOT_FERVOR_LEGGINGS", "FERVOR_LEGGINGS"));
		map.put("BURNING_FERVOR_BOOTS", Set.of("HOT_FERVOR_BOOTS", "FERVOR_BOOTS"));

		map.put("FIERY_FERVOR_HELMET", Set.of("BURNING_FERVOR_HELMET", "HOT_FERVOR_HELMET", "FERVOR_HELMET"));
		map.put("FIERY_FERVOR_CHESTPLATE", Set.of("BURNING_FERVOR_CHESTPLATE", "HOT_FERVOR_CHESTPLATE", "FERVOR_CHESTPLATE"));
		map.put("FIERY_FERVOR_LEGGINGS", Set.of("BURNING_FERVOR_LEGGINGS", "HOT_FERVOR_LEGGINGS", "FERVOR_LEGGINGS"));
		map.put("FIERY_FERVOR_BOOTS", Set.of("BURNING_FERVOR_BOOTS", "HOT_FERVOR_BOOTS", "FERVOR_BOOTS"));

		map.put("INFERNAL_FERVOR_HELMET", Set.of("FIERY_FERVOR_HELMET", "BURNING_FERVOR_HELMET", "HOT_FERVOR_HELMET", "FERVOR_HELMET"));
		map.put("INFERNAL_FERVOR_CHESTPLATE", Set.of("FIERY_FERVOR_CHESTPLATE", "BURNING_FERVOR_CHESTPLATE", "HOT_FERVOR_CHESTPLATE", "FERVOR_CHESTPLATE"));
		map.put("INFERNAL_FERVOR_LEGGINGS", Set.of("FIERY_FERVOR_LEGGINGS", "BURNING_FERVOR_LEGGINGS", "HOT_FERVOR_LEGGINGS", "FERVOR_LEGGINGS"));
		map.put("INFERNAL_FERVOR_BOOTS", Set.of("FIERY_FERVOR_BOOTS", "BURNING_FERVOR_BOOTS", "HOT_FERVOR_BOOTS", "FERVOR_BOOTS"));
	}));
}
