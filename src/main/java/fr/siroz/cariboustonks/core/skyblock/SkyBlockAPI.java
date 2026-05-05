package fr.siroz.cariboustonks.core.skyblock;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.ElectionResult;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.Mayor;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.Perk;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.PetInfo;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.Rarity;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockAttribute;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.ItemUtils;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The {@code SkyBlockAPI} class provides a utility layer to interact with SkyBlock-related states and contents.
 */
public final class SkyBlockAPI {
	/**
	 * Real-world Unix timestamp (ms) corresponding to the SkyBlock Day 1, Year 1.
	 */
	private static final long SKYBLOCK_EPOCH_START_MILLIS = 1_560_275_700_000L;
	private static final Minecraft CLIENT = Minecraft.getInstance();
	// Common constants
	private static final String ITEM_ID = "id";
	private static final String ITEM_UUID = "uuid";
	// Dep
	private static Supplier<ElectionResult> electionSource;
	private static Function<String, SkyBlockAttribute> attributeLookup;
	// General states
	private static boolean onSkyBlockState = false;
	private static IslandType islandType = IslandType.UNKNOWN;
	private static String gameType = "";
	private static SkyBlockTime time = SkyBlockTime.DEFAULT;
	private static SkyBlockSeason season = SkyBlockSeason.SPRING;
	private static SkyBlockSeason.Month month = SkyBlockSeason.Month.EARLY_SPRING;

	private SkyBlockAPI() {
		throw new UnsupportedOperationException();
	}

	static void bootstrap(
			@NonNull Supplier<ElectionResult> electionSourceFactory,
			@NonNull Function<String, SkyBlockAttribute> attributeLookupFactory
	) {
		electionSource = electionSourceFactory;
		attributeLookup = attributeLookupFactory;
	}

	/**
	 * Returns the current SkyBlock Time milliseconds
	 *
	 * @return the current SkyBlock Time in milliseconds
	 */
	public static long getSkyBlockMillis() {
		return System.currentTimeMillis() - SKYBLOCK_EPOCH_START_MILLIS;
	}

	/**
	 * Checks if the player is currently on the SkyBlock.
	 *
	 * @return {@code true} if the player is on SkyBlock
	 */
	public static boolean isOnSkyBlock() {
		return onSkyBlockState;
	}

	/**
	 * Retrieves the current {@link IslandType} the player is on in SkyBlock.
	 *
	 * @return the {@code IslandType} representing the player's current island.
	 */
	public static IslandType getIsland() {
		return islandType;
	}

	/**
	 * Checks if the current {@link IslandType} matches any of the specified island types.
	 *
	 * @param islandTypes an array of {@link IslandType} values to check against the current island type
	 * @return {@code true} if the current island type matches any of the specified types
	 */
	public static boolean isOnIslands(IslandType @NonNull ... islandTypes) {
		if (islandTypes.length == 0) {
			return false;
		}

		if (islandTypes.length == 1) {
			return islandTypes[0] == islandType;
		}

		for (IslandType type : islandTypes) {
			if (type == islandType) return true;
		}

		return false;
	}

	/**
	 * Returns the current {@link SkyBlockTime}
	 *
	 * @return the {@code SkyBlockTime}
	 */
	public static SkyBlockTime getTime() {
		return time;
	}

	/**
	 * Returns the current {@link SkyBlockSeason}
	 *
	 * @return the {@code SkyBlockSeason}
	 */
	public static SkyBlockSeason getSeason() {
		return season;
	}

	/**
	 * Returns the current {@link SkyBlockSeason.Month}
	 *
	 * @return the {@code Month}
	 */
	public static SkyBlockSeason.Month getMonth() {
		return month;
	}

	/**
	 * Returns whether the given {@link Mayor} is currently the mayor or minister.
	 *
	 * @param mayor the {@link Mayor} to check
	 * @return {@code true} if the given {@code mayor} matches the current mayor or minister
	 * @see #isMayorOrMinister(Mayor, Perk)
	 */
	public static boolean isMayorOrMinister(@NonNull Mayor mayor) {
		return isMayorOrMinister(mayor, null);
	}

	/**
	 * Returns whether the given {@link Mayor} currently holds the mayor or minister role,
	 * and (optionally) whether the specified {@link Perk} is present for that role.
	 *
	 * <p>Behavior:
	 * <ul>
	 *   <li>If the latest {@link ElectionResult} is not available, this method returns {@code false}.</li>
	 *   <li>If {@code perk} is {@code null}, the method returns {@code true} when {@code mayor}
	 *       equals either the current mayor or the current minister.</li>
	 *   <li>If {@code perk} is non-null:
	 *       <ul>
	 *         <li>When {@code mayor} equals the current mayor, the method returns {@code true}
	 *             only if the mayor's perk set contains {@code perk}.</li>
	 *         <li>When {@code mayor} equals the current minister, the method returns {@code true}
	 *             only if the minister has a perk and that perk equals {@code perk}.</li>
	 *       </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param mayor the {@link Mayor} to check
	 * @param perk  optional {@link Perk} to verify for the given role; if {@code null} only the role is checked
	 * @return {@code true} if the given {@code mayor} matches the current mayor or minister and,
	 * when {@code perk} is provided, the requested perk is present for that role
	 * @see #isMayorOrMinister(Mayor)
	 */
	public static boolean isMayorOrMinister(@NonNull Mayor mayor, @Nullable Perk perk) {
		ElectionResult result = electionSource != null ? electionSource.get() : null;
		if (result == null) return false;

		if (perk == null) {
			return mayor == result.mayor() || mayor == result.minister();
		}

		if (mayor == result.mayor()) {
			final Set<Perk> mayorPerks = result.mayorPerks();
			return !mayorPerks.isEmpty() && mayorPerks.contains(perk);
		}

		if (mayor == result.minister()) {
			final Optional<Perk> opt = result.ministerPerk();
			return opt.isPresent() && opt.get() == perk;
		}

		return false;
	}

	/**
	 * Retrieves the current SkyBlock Area where the player is from the Scoreboard.
	 *
	 * @return an {@link Optional} containing the area name
	 */
	public static @NonNull Optional<String> getArea() {
		for (String line : Client.getScoreboard()) {
			if (line.contains("⏣") || line.contains("ф")) {
				return Optional.of(line.strip());
			}
		}
		return Optional.empty();
	}

	/**
	 * Gets the {@code SkyBlock Item ID} of the ItemStack.
	 *
	 * @param stack the ItemStack
	 * @return the SkyBlock Item ID or an empty string
	 */
	public static @NonNull String getSkyBlockItemId(@NonNull DataComponentHolder stack) {
		return ItemUtils.getCustomData(stack).getStringOr(ITEM_ID, "");
	}

	/**
	 * Gets the {@code SkyBlock Item UUID} of the ItemStack.
	 *
	 * @param stack the ItemStack
	 * @return the UUID or an empty string
	 */
	public static @NonNull String getSkyBlockItemUuid(@NonNull DataComponentHolder stack) {
		return ItemUtils.getCustomData(stack).getStringOr(ITEM_UUID, "");
	}

	/**
	 * Determines if the currently held item has the specified SkyBlock item ID.
	 *
	 * @param skyBlockItemId the SkyBlock item ID to compare against the held item's ID
	 * @return {@code true} if the currently held is not null, and the skyBlockItemId matches
	 */
	public static boolean isHoldingItem(@NonNull String skyBlockItemId) {
		ItemStack held = Client.getHeldItem();
		return held != null && getSkyBlockItemId(held).equals(skyBlockItemId);
	}

	/**
	 * Gets the {@link Rarity} of the given ItemStack.
	 *
	 * @param stack the ItemStack
	 * @return the Rarity of the ItemStack or {@link Rarity#UNKNOWN} if the item does not have a rarity
	 */
	public static @NonNull Rarity getRarity(@Nullable ItemStack stack) {
		if (!onSkyBlockState || stack == null || stack.isEmpty()) return Rarity.UNKNOWN;

		if (getSkyBlockItemId(stack).equals("PET")) {
			return getPetInfo(stack).rarity();
		}

		return ItemUtils.getLore(stack).reversed().stream()
				.map(Component::getString)
				.map(Rarity::containsName)
				.flatMap(Optional::stream)
				.findFirst()
				.orElse(Rarity.UNKNOWN);
	}

	/**
	 * Gets the {@link PetInfo} of the given ItemStack.
	 *
	 * @param stack the ItemStack
	 * @return the PetInfo or {@link PetInfo#EMPTY} if the item is not a pet
	 */
	public static @NonNull PetInfo getPetInfo(@Nullable ItemStack stack) {
		if (!onSkyBlockState || stack == null || stack.isEmpty()) {
			return PetInfo.EMPTY;
		}

		return PetInfo.parse(ItemUtils.getCustomData(stack));
	}

	/**
	 * Gets the {@code SkyBlock API ID} of the ItemStack.
	 *
	 * @return the SkyBlock API ID or an empty String
	 */
	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	public static @NonNull String getSkyBlockApiId(@NonNull DataComponentHolder itemStack) {
		CompoundTag customData = ItemUtils.getCustomData(itemStack);
		String id = customData.getStringOr(ITEM_ID, "");

		if (customData.contains("is_shiny")) {
			return "SHINY_" + id;
		}

		switch (id) {
			case "ENCHANTED_BOOK" -> {
				if (customData.contains("enchantments")) {
					CompoundTag enchants = customData.getCompoundOrEmpty("enchantments");
					Optional<String> firstEnchant = enchants.keySet().stream().findFirst();
					String enchant = firstEnchant.orElse("");
					return "ENCHANTMENT_" + enchant.toUpperCase(Locale.ENGLISH) + "_" + enchants.getIntOr(enchant, 0);
				}
			}

			case "POTION" -> {
				String enhanced = customData.contains("enhanced") ? "_ENHANCED" : "";
				String extended = customData.contains("extended") ? "_EXTENDED" : "";
				String splash = customData.contains("splash") ? "_SPLASH" : "";
				if (customData.contains("potion") && customData.contains("potion_level")) {
					return (customData.getStringOr("potion", "")
							+ "_" + id + "_" + customData.getIntOr("potion_level", 0)
							+ enhanced + extended + splash).toUpperCase(Locale.ENGLISH);
				}
			}

			case "RUNE" -> {
				if (customData.contains("runes")) {
					CompoundTag runes = customData.getCompoundOrEmpty("runes");
					String rune = runes.keySet().stream().findFirst().orElse("");
					return rune.toUpperCase(Locale.ENGLISH) + "_RUNE_" + runes.getIntOr(rune, 0);
				}
			}

			case "ATTRIBUTE_SHARD" -> {
				String name = itemStack.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty()).getString();
				SkyBlockAttribute attribute = attributeLookup != null ? attributeLookup.apply(name) : null;
				if (attribute != null) {
					return attribute.skyBlockApiId();
				}
			}

			case "PET" -> {
				if (customData.contains("petInfo")) {
					PetInfo petInfo = PetInfo.parse(customData);
					return "LVL_1_" + petInfo.rarity() + "_" + petInfo.type();
				}
			}

			case "NEW_YEAR_CAKE" -> {
				return id + "_" + customData.getIntOr("new_years_cake", 0);
			}

			default -> {
			}
		}

		return id;
	}

	static String getGameType() {
		return gameType;
	}

	static void handleInternalUpdate() {
		if (CLIENT.level == null || CLIENT.isLocalServer()) {
			if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
				onSkyBlockState = true;
			}
		}
	}

	static void handleInternalLocationUpdate(
			@Nullable Boolean onSkyBlock,
			@Nullable String gameTypeFromServer,
			@Nullable IslandType islandTypeFromMode
	) {
		if (onSkyBlock != null) onSkyBlockState = onSkyBlock;
		if (gameTypeFromServer != null) gameType = gameTypeFromServer;
		if (islandTypeFromMode != null) islandType = islandTypeFromMode;

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[SkyBlockAPI] Updated: {}, {}, {}", onSkyBlockState, gameType, islandType.name());
		}
	}

	static void handleInternalTimeUpdate(
			@Nullable SkyBlockTime skyBlockTime,
			@Nullable SkyBlockSeason skyBlockSeason,
			SkyBlockSeason.@Nullable Month skyBlockMonth
	) {
		if (skyBlockTime != null) time = skyBlockTime;
		if (skyBlockSeason != null) season = skyBlockSeason;
		if (skyBlockMonth != null) month = skyBlockMonth;

		// Log dans une fenêtre de 1s autour de x multiple de 5s pour éviter le spam en dev
		if (DeveloperTools.isInDevelopment() && (System.currentTimeMillis() % 5000L < 1000L)) {
			CaribouStonks.LOGGER.info("[SkyBlockAPI] Time Updated: Year {}, Season {}, Month {}, Day {}, Hour {}",
					time.year(), season, month, time.day(), time.hour());
		}
	}
}
