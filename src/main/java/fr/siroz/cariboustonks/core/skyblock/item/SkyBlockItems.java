package fr.siroz.cariboustonks.core.skyblock.item;

import fr.siroz.cariboustonks.core.skyblock.Rarity;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockConstants;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.PetInfo;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.ItemMetadata;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.util.ItemUtils;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class SkyBlockItems {
	private static final String ITEM_ID = "id";
	private static final String ITEM_UUID = "uuid";
	private static final Pattern SHARD_WITH_QUANTITY_PATTERN = Pattern.compile("[A-Za-z ]+ Shard(?: x(?<amount>\\d+))?");
	public static final Pattern SHARD_SOURCE_PATTERN = Pattern.compile("Source: (?<shardName>[A-Za-z ]+?) Shard \\((?<id>[CUREL]\\d+)\\)");
	public static final Pattern SHARD_RARITY_AND_ID_PATTERN = Pattern.compile("(COMMON|UNCOMMON|RARE|EPIC|LEGENDARY).*?SHARD \\(ID ([CUREL]\\d+)\\)");

	private SkyBlockItems() {
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
	 * Create a {@link SkyblockItemStack} from the given {@link ItemStack}.
	 *
	 * @param itemStack the {@code ItemStack} to parse
	 * @return the {@code SkyblockItemStack} parsed from the given {@code ItemStack}
	 */
	public static @NonNull SkyblockItemStack createSkyBlockItemStack(@NonNull ItemStack itemStack) {
		CompoundTag customData = ItemUtils.getCustomData(itemStack);
		String skyBlockId = customData.getStringOr("id", "");
		return new SkyblockItemStack(skyBlockId, itemStack.getCount(), ItemMetadata.ofNbt(customData));
	}

	/**
	 * Determines if the currently held item has the specified SkyBlock item ID.
	 *
	 * @param skyBlockItemId the SkyBlock item ID to compare against the held item's ID
	 * @return {@code true} if the currently held is not null, and the skyBlockItemId matches
	 */
	public static boolean isHolding(@NonNull String skyBlockItemId) {
		ItemStack held = PlayerContext.getHeldItem();
		if (held == null || held.isEmpty()) return false;

		return getSkyBlockItemId(held).equals(skyBlockItemId);
	}

	/**
	 * Determines if the currently held item has a specified SkyBlock item ID from a Collection.
	 *
	 * @param skyBlockItemIds the collection of skyBlockItemId
	 * @return {@code true} if the currently held is not null, and one of a skyBlockItemIds matche
	 */
	public static boolean isHolding(@NonNull Collection<String> skyBlockItemIds) {
		ItemStack held = PlayerContext.getHeldItem();
		if (held == null || held.isEmpty()) return false;

		String heldItemId = getSkyBlockItemId(held);
		if (heldItemId.isEmpty()) return false;

		return skyBlockItemIds.contains(heldItemId);
	}

	/**
	 * Gets the {@link Rarity} of the given ItemStack.
	 *
	 * @param stack the ItemStack
	 * @return the Rarity of the ItemStack or {@link Rarity#UNKNOWN} if the item does not have a rarity
	 */
	public static @NonNull Rarity getRarity(@Nullable ItemStack stack) {
		if (stack == null || stack.isEmpty()) return Rarity.UNKNOWN;

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
		if (stack == null || stack.isEmpty()) return PetInfo.EMPTY;
		return PetInfo.parse(ItemUtils.getCustomData(stack));
	}

	public static OptionalInt getAttributeShardsUntilMax(Rarity rarity, int level) {
		if (level == SkyBlockConstants.ATTRIBUTE_SHARD_MAX_LEVEL) return OptionalInt.of(0);

		Int2IntMap counts = SkyBlockConstants.ATTRIBUTE_LEVELS.get(rarity);
		if (counts == null || !counts.containsKey(level) || !counts.containsKey(SkyBlockConstants.ATTRIBUTE_SHARD_MAX_LEVEL)) {
			return OptionalInt.empty();
		}

		return OptionalInt.of(counts.get(SkyBlockConstants.ATTRIBUTE_SHARD_MAX_LEVEL) - counts.get(level));
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
				String name = ItemUtils.getItemName(itemStack);
				SkyBlockAttribute attribute = SkyBlockItemRegistry.getAttributeByShardName(name);
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
			case "PARTY_HAT_CRAB", "PARTY_HAT_CRAB_ANIMATED", "BALLOON_HAT_2024", "BALLOON_HAT_2025", "CAKE_HAT_2026" -> {
				return id + "_" + customData.getStringOr("party_hat_color", "").toUpperCase(Locale.ENGLISH);
			}
			case "PARTY_HAT_SLOTH" -> {
				return id + "_" + customData.getStringOr("party_hat_emoji", "").toUpperCase(Locale.ENGLISH);
			}
			case "MIDAS_SWORD" -> {
				if (customData.getIntOr("winning_bid", 0) >= 50_000_000) {
					return id + "_50M";
				}
			}
			case "MIDAS_STAFF" -> {
				if (customData.getIntOr("winning_bid", 0) >= 100_000_000) {
					return id + "_100M";
				}
			}
			case "FACTION_RABBIT" -> {
				return id + "_" + customData.getStringOr("faction_rabbit_id", "").toUpperCase(Locale.ENGLISH);
			}
			default -> {
			}
		}

		return id;
	}

	public static @NonNull String getNeuId(@Nullable ItemStack stack) {
		if (stack == null) return "";

		String id = getSkyBlockItemId(stack);
		CompoundTag customData = ItemUtils.getCustomData(stack);

		return switch (id) {
			case "ENCHANTED_BOOK" -> {
				CompoundTag enchantments = customData.getCompoundOrEmpty("enchantments");
				String enchant = enchantments.keySet().stream().findFirst().orElse("");
				yield enchant.toUpperCase(Locale.ENGLISH) + ";" + enchantments.getIntOr(enchant, 0);
			}

			case "RUNE" -> {
				CompoundTag runes = customData.getCompoundOrEmpty("runes");
				String rune = runes.keySet().stream().findFirst().orElse("");
				yield rune.toUpperCase(Locale.ENGLISH) + "_RUNE;" + runes.getIntOr(rune, 0);
			}

			case "PET" -> {
				if (!customData.contains("petInfo")) yield id;
				PetInfo petInfo = PetInfo.parse(customData);
				yield petInfo.type() + ";" + petInfo.rarity().getIndex();
			}

			case "POTION" -> "POTION_" + customData.getStringOr("potion", "").toUpperCase(Locale.ENGLISH)
					+ ";"
					+ customData.getIntOr("potion_level", 0);

			case "ATTRIBUTE_SHARD" -> ""; // The Foraging Update 0.23 - "New Shard API"

			case "PARTY_HAT_CRAB", "BALLOON_HAT_2024", "BALLOON_HAT_2025", "CAKE_HAT_2026" -> id
					+ "_"
					+ customData.getStringOr("party_hat_color", "").toUpperCase(Locale.ENGLISH);

			case "PARTY_HAT_CRAB_ANIMATED" -> "PARTY_HAT_CRAB_"
					+ customData.getStringOr("party_hat_color", "").toUpperCase(Locale.ENGLISH)
					+ "_ANIMATED";

			case "PARTY_HAT_SLOTH" -> id
					+ "_"
					+ customData.getStringOr("party_hat_emoji", "").toUpperCase(Locale.ENGLISH);

			case "FACTION_RABBIT" -> id + "_" + customData.getStringOr("faction_rabbit_id", "").toUpperCase(Locale.ENGLISH);

			default -> id.replace(":", "-");
		};
	}

	public static @NonNull String getNeuIdFromSkyBlockId(@NonNull String skyBlockItemId) {
		if (skyBlockItemId.isEmpty()) return "";

		// Cas des enchantements : ENCHANTMENT_<NOM>_<NIVEAU>
		if (skyBlockItemId.startsWith("ENCHANTMENT_")) {
			String enchantment = skyBlockItemId.substring("ENCHANTMENT_".length());
			int lastUnderscore = enchantment.lastIndexOf('_');
			if (lastUnderscore != -1 && lastUnderscore < enchantment.length() - 1) {
				String name = enchantment.substring(0, lastUnderscore);
				String level = enchantment.substring(lastUnderscore + 1);
				return name + ";" + level;
			}
		}
		return skyBlockItemId.replace(":", "-");
	}

	/**
	 * Returns the {@code SkyBlock API ID} of the ItemStack in Hunting Box, Attribute Menu, Fusion Machine GUIs.
	 *
	 * @param fallback the skyBlockApiId fallback
	 * @param item     the ItemStack
	 * @param lines    the lore of the ItemStack
	 * @return the skyBlockApiId or the fallback
	 */
	public static String resolveAttributeApiId(@NonNull String fallback, ItemStack item, List<Component> lines) {
		Screen currentScreen = ClientContext.getScreen();
		if (!fallback.isEmpty() || currentScreen == null) return fallback;

		String title = currentScreen.getTitle().getString();

		if (title.contains("Hunting Box")) {
			String name = ItemUtils.getItemName(item);
			SkyBlockAttribute attribute = SkyBlockItemRegistry.getAttributeByShardName(name);
			return attribute != null ? attribute.skyBlockApiId() : fallback;
		}

		if (title.contains("Attribute Menu")) {
			String id = null;
			for (Component line : lines) {
				if (line.getString().isEmpty()) continue;

				Matcher matcher = SHARD_SOURCE_PATTERN.matcher(line.getString());
				if (matcher.matches()) {
					id = matcher.group("id");
					break;
				}
			}
			return attributeIdOr(id, fallback);
		}

		if (title.contains("Fusion Box") || title.contains("Shard Fusion")) {
			String id = null;
			for (Component line : lines) {
				if (line.getString().isEmpty()) continue;

				Matcher matcher = SHARD_RARITY_AND_ID_PATTERN.matcher(line.getString());
				if (matcher.matches()) {
					id = matcher.group(2);
					break;
				}
			}

			return attributeIdOr(id, fallback);
		}

		if (title.equals("Confirm Fusion")) {
			String id = null;
			for (Component line : lines) {
				if (line.getString().isEmpty()) continue;

				Matcher matcher = SHARD_RARITY_AND_ID_PATTERN.matcher(line.getString());
				if (matcher.matches()) {
					id = matcher.group(2);
					break;
				}
			}
			return attributeIdOr(id, fallback);
		}

		if (SkyBlockConstants.DUNGEON_CHESTS.contains(title) || SkyBlockConstants.KUUDRA_CHESTS.contains(title)) {
			String name = ItemUtils.getItemName(item);
			Matcher matcher = SHARD_WITH_QUANTITY_PATTERN.matcher(name);
			if (name.contains("Shard") && matcher.matches()) {
				SkyBlockAttribute attribute = SkyBlockItemRegistry.getAttributeByShardName(name);
				return attribute != null ? attribute.skyBlockApiId() : fallback;
			}
		}

		return fallback;
	}

	private static String attributeIdOr(@Nullable String id, @NonNull String fallback) {
		SkyBlockAttribute attribute = SkyBlockItemRegistry.getAttributeById(id);
		return attribute != null ? attribute.skyBlockApiId() : fallback;
	}
}
