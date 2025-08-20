package fr.siroz.cariboustonks.util;

import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.serialization.JsonOps;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.data.mod.SkyBlockAttribute;
import fr.siroz.cariboustonks.core.skyblock.Rarity;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectReferencePair;
import java.util.Objects;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;
import java.util.function.Predicate;

public final class ItemUtils {

	private static final String ITEM_ID = "id";
	private static final String ITEM_UUID = "uuid";
	private static final Pattern ABILITY = Pattern.compile("Ability: (?<name>.*?) *");

	private static final List<ObjectReferencePair<String, Rarity>> LORE_RARITIES = List.of(
			ObjectReferencePair.of("ULTIMATE", Rarity.ULTIMATE),
			ObjectReferencePair.of("DIVINE", Rarity.DIVINE),
			ObjectReferencePair.of("MYTHIC", Rarity.MYTHIC),
			ObjectReferencePair.of("SPECIAL", Rarity.SPECIAL),
			ObjectReferencePair.of("LEGENDARY", Rarity.LEGENDARY),
			ObjectReferencePair.of("EPIC", Rarity.EPIC),
			ObjectReferencePair.of("RARE", Rarity.RARE),
			ObjectReferencePair.of("UNCOMMON", Rarity.UNCOMMON),
			ObjectReferencePair.of("COMMON", Rarity.COMMON)
	);
	private static final Int2ReferenceOpenHashMap<Rarity> CACHE_RARITIES = new Int2ReferenceOpenHashMap<>();

	private ItemUtils() {
	}

	/**
	 * Gets the nbt in the custom data component of the item stack.
	 *
	 * @return The {@link DataComponentTypes#CUSTOM_DATA custom data} of the itemstack,
	 * or an empty {@link NbtCompound} if the itemstack is missing a custom data component
	 */
	@SuppressWarnings("deprecation")
	public static @NotNull NbtCompound getCustomData(@NotNull ComponentHolder stack) {
		return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt();
	}

	/**
	 * Gets the {@code SkyBlock Item ID} of the ItemStack.
	 *
	 * @param stack the ItemStack
	 * @return the SkyBlock Item ID or an empty string
	 */
	public static @NotNull String getSkyBlockItemId(@NotNull ComponentHolder stack) {
		return getCustomData(stack).getString(ITEM_ID, "");
	}

	/**
	 * Gets the {@code SkyBlock Item UUID} of the ItemStack.
	 *
	 * @param stack the ItemStack
	 * @return the UUID or an empty string
	 */
	public static @NotNull String getSkyBlockItemUuid(@NotNull ComponentHolder stack) {
		return getCustomData(stack).getString(ITEM_UUID, "");
	}

	@Deprecated
	public static @NotNull Optional<Item> getItemByNumericId(int id) {
		return Registries.ITEM.stream().filter(item -> Registries.ITEM.getRawId(item) == id).findFirst();
	}

	public static Optional<Item> getItemById(@NotNull String id) {
		try {
			Identifier search = Identifier.of(id);
			//itemRegistry.getOptional(RegistryKey.of(RegistryKeys.ITEM, identifier))
			return Registries.ITEM.getOptionalValue(search);
		} catch (Exception ex) {
			CaribouStonks.LOGGER.warn("[ItemUtils] Unable to find {} in Item Registry", id, ex);
			return Optional.empty();
		}
	}

	public static @NotNull List<Text> getLore(@NotNull ItemStack stack) {
		return stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines();
	}

	@Nullable
	public static Matcher getLoreLineIfMatch(@NotNull ItemStack stack, @NotNull Pattern pattern) {
		Matcher matcher = pattern.matcher("");
		for (Text line : getLore(stack)) {
			if (matcher.reset(line.getString()).matches()) {
				return matcher;
			}
		}

		return null;
	}

	@Nullable
	public static String getLoreLineIf(ItemStack stack, Predicate<String> predicate) {
		for (Text line : getLore(stack)) {
			String string = line.getString();
			if (predicate.test(string)) {
				return string;
			}
		}

		return null;
	}

	public static @NotNull String getConcatenatedLore(@NotNull ItemStack item) {
		return concatenateLore(getLore(item));
	}

	/**
	 * Concatenates the lore of an item into one string
	 */
	public static @NotNull String concatenateLore(@NotNull List<Text> lore) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < lore.size(); i++) {
			stringBuilder.append(lore.get(i).getString());
			if (i != lore.size() - 1) {
				stringBuilder.append(" ");
			}
		}

		return stringBuilder.toString();
	}

	/**
	 * Gets the {@code SkyBlock API ID} of the ItemStack.
	 *
	 * @return the SkyBlock API ID or an empty String
	 */
	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	public static @NotNull String getSkyBlockApiId(@NotNull ComponentHolder itemStack) {
		NbtCompound customData = getCustomData(itemStack);
		String id = customData.getString(ITEM_ID, "");

		if (customData.contains("is_shiny")) {
			return "SHINY_" + id;
		}

		switch (id) {
			case "ENCHANTED_BOOK" -> {
				if (customData.contains("enchantments")) {
					NbtCompound enchants = customData.getCompoundOrEmpty("enchantments");
					Optional<String> firstEnchant = enchants.getKeys().stream().findFirst();
					String enchant = firstEnchant.orElse("");
					return "ENCHANTMENT_" + enchant.toUpperCase(Locale.ENGLISH) + "_" + enchants.getInt(enchant, 0);
				}
			}

			case "POTION" -> {
				String enhanced = customData.contains("enhanced") ? "_ENHANCED" : "";
				String extended = customData.contains("extended") ? "_EXTENDED" : "";
				String splash = customData.contains("splash") ? "_SPLASH" : "";
				if (customData.contains("potion") && customData.contains("potion_level")) {
					return (customData.getString("potion", "")
							+ "_" + id + "_" + customData.getInt("potion_level", 0)
							+ enhanced + extended + splash).toUpperCase(Locale.ENGLISH);
				}
			}

			case "RUNE" -> {
				if (customData.contains("runes")) {
					NbtCompound runes = customData.getCompoundOrEmpty("runes");
					String rune = runes.getKeys().stream().findFirst().orElse("");
					return rune.toUpperCase(Locale.ENGLISH) + "_RUNE_" + runes.getInt(rune, 0);
				}
			}

			case "ATTRIBUTE_SHARD" -> {
				String name = itemStack.getOrDefault(DataComponentTypes.CUSTOM_NAME, Text.empty()).getString();
				SkyBlockAttribute attribute = CaribouStonks.core().getModDataSource().getAttributeByShardName(name);
				if (attribute != null) {
					return attribute.skyBlockApiId();
				}
			}
			default -> {
			}
		}

		return id;
	}

	public static @Nullable Rarity getItemRarity(@Nullable ItemStack stack) {
		if (stack == null || stack.isEmpty()) return null;

		String itemUuid = getSkyBlockItemUuid(stack);

		int hashCode = itemUuid.isEmpty() ? System.identityHashCode(stack) : itemUuid.hashCode();
		if (CACHE_RARITIES.containsKey(hashCode)) {
			return CACHE_RARITIES.get(hashCode);
		}

		List<Text> lore = getLore(stack);
		String[] tooltipStr = lore.stream().map(Text::getString).toArray(String[]::new);

		for (ObjectReferencePair<String, Rarity> loreToRarity : LORE_RARITIES) {
			if (Arrays.stream(tooltipStr).anyMatch(line -> line.contains(loreToRarity.left()))) {
				Rarity rarity = loreToRarity.right();
				if (rarity != null) {
					CACHE_RARITIES.put(hashCode, rarity);
					return rarity;
				}
			}
		}

		CACHE_RARITIES.put(hashCode, null);
		return null;
	}

	public static @Nullable String getAbility(@NotNull ItemStack stack) {
		Matcher abilityMatcher = getLoreLineIfMatch(stack, ABILITY);
		return abilityMatcher != null ? abilityMatcher.group("name") : null;
	}

	public static @NotNull ItemStack createSkull(@NotNull String textureB64) {
		ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
		try {
			PropertyMap map = new PropertyMap();
			map.put("textures", new Property("textures", textureB64));
			ProfileComponent profile = new ProfileComponent(Optional.of("skull"), Optional.of(UUID.randomUUID()), map);
			skull.set(DataComponentTypes.PROFILE, profile);
		} catch (Exception exception) {
			CaribouStonks.LOGGER.error("[ItemUtils] Failed to create skull", exception);
		}
		return skull;
	}

	public static @NotNull String getHeadTexture(@NotNull ItemStack stack) {
		if (!stack.isOf(Items.PLAYER_HEAD) || !stack.contains(DataComponentTypes.PROFILE)) return "";

		ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
		if (profile == null) return "";

		return profile.properties().get("textures").stream()
				.filter(Objects::nonNull)
				.map(Property::value)
				.findFirst()
				.orElse("");
	}

	public static @NotNull Optional<String> getHeadTextureOptional(@NotNull ItemStack stack) {
		String texture = getHeadTexture(stack);
		return texture.isBlank() ? Optional.empty() : Optional.of(texture);
	}

	public static Optional<PropertyMap> propertyMapWithTexture(@NotNull String textureValue) {
		try {
			String json = "[{\"name\":\"textures\",\"value\":\"" + textureValue + "\"}]";
			PropertyMap propertyMap = Codecs.GAME_PROFILE_PROPERTY_MAP.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();

			return Optional.ofNullable(propertyMap);
		} catch (Throwable ex) {
			CaribouStonks.LOGGER.error("[ItemUtils] Failed to get PropertyMap from the texture", ex);
			return Optional.empty();
		}
	}
}
