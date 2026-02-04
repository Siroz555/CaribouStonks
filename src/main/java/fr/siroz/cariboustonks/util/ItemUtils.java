package fr.siroz.cariboustonks.util;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for handling operations related to {@link ItemStack}.
 */
public final class ItemUtils {

	private ItemUtils() {
	}

	/**
	 * Gets the {@link CompoundTag} in the custom data component of the ItemStack.
	 *
	 * @return The {@link DataComponents#CUSTOM_DATA custom data} of the ItemStack,
	 * or an empty {@link CompoundTag} if the ItemStack is missing a custom data component
	 */
	public static @NotNull CompoundTag getCustomData(@NotNull DataComponentHolder stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	/**
	 * Gets an item by its id from the Minecraft {@link BuiltInRegistries#ITEM}.
	 *
	 * @param id the item id
	 * @return the item or an empty {@link Optional}
	 */
	public static Optional<Item> getItemById(@NotNull String id) {
		try {
			Identifier search = Identifier.parse(id);
			//itemRegistry.getOptional(RegistryKey.of(RegistryKeys.ITEM, identifier))
			return BuiltInRegistries.ITEM.getOptional(search);
		} catch (Exception ex) {
			fr.siroz.cariboustonks.CaribouStonks.LOGGER.warn("[ItemUtils] Unable to find {} in Item Registry", id, ex);
			return Optional.empty();
		}
	}

	/**
	 * Returns the lore as a {@link List} of {@link Component} of an item.
	 *
	 * @param stack the ItemStack to get the lore from
	 * @return the list of lore
	 */
	public static @NotNull List<Component> getLore(@NotNull ItemStack stack) {
		return stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).styledLines();
	}

	/**
	 * Returns the first lore line that matches the {@link Pattern}.
	 *
	 * @param stack   the ItemStack to get the lore from
	 * @param pattern the pattern to match against
	 * @return the first matching lore line or {@code null}
	 */
	@Nullable
	public static Matcher getLoreLineIfMatch(@NotNull ItemStack stack, @NotNull Pattern pattern) {
		Matcher matcher = pattern.matcher("");
		for (Component line : getLore(stack)) {
			if (matcher.reset(line.getString()).matches()) {
				return matcher;
			}
		}

		return null;
	}

	/**
	 * Returns the first lore line that matches the {@link Predicate}.
	 *
	 * @param stack     the ItemStack to get the lore from
	 * @param predicate the predicate to match against
	 * @return the first matching lore line or {@code null}
	 */
	@Nullable
	public static String getLoreLineIf(@NotNull ItemStack stack, @NotNull Predicate<String> predicate) {
		for (Component line : getLore(stack)) {
			String string = line.getString();
			if (predicate.test(string)) {
				return string;
			}
		}

		return null;
	}

	/**
	 * Concatenates the lore of an item into one string.
	 *
	 * @param item the item to get the lore from
	 * @return the concatenated lore
	 */
	public static @NotNull String getConcatenatedLore(@NotNull ItemStack item) {
		return concatenateLore(getLore(item));
	}

	/**
	 * Concatenates the lore of an item into one string.
	 *
	 * @param lore the lore to concatenate
	 * @return the concatenated lore
	 */
	public static @NotNull String concatenateLore(@NotNull List<Component> lore) {
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
	 * Creates a {@link Items#PLAYER_HEAD} item with the given texture.
	 *
	 * @param textureB64 the texture in Base64 format
	 * @return the created ItemStack
	 */
	public static @NotNull ItemStack createSkull(@NotNull String textureB64) {
		ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
		try {
			PropertyMap map = new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", textureB64)));
			GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "skull", map);
			ResolvableProfile profile = ResolvableProfile.createResolved(gameProfile);
			skull.set(DataComponents.PROFILE, profile);
		} catch (Exception ex) {
			fr.siroz.cariboustonks.CaribouStonks.LOGGER.error("[ItemUtils] Failed to create skull", ex);
		}
		return skull;
	}

	/**
	 * Returns the {@code value} of the textures from
	 * the {@link DataComponents#PROFILE profile} component or an empty string.
	 *
	 * @param stack the ItemStack to get the texture from
	 * @return the texture or an empty string
	 * @see #getHeadTextureOptional(ItemStack)
	 */
	public static @NotNull String getHeadTexture(@NotNull ItemStack stack) {
		if (!stack.is(Items.PLAYER_HEAD) || !stack.has(DataComponents.PROFILE)) {
			return "";
		}

		ResolvableProfile profile = stack.get(DataComponents.PROFILE);
		if (profile == null) {
			return "";
		}

		return profile.partialProfile().properties().get("textures").stream()
				.filter(Objects::nonNull)
				.map(Property::value)
				.findFirst()
				.orElse("");
	}

	/**
	 * Returns the {@code value} of the textures from the
	 * {@link DataComponents#PROFILE profile} component or an empty {@link Optional}.
	 *
	 * @param stack the ItemStack to get the texture from
	 * @return the texture or an empty {@link Optional}
	 * @see #getHeadTexture(ItemStack)
	 */
	public static @NotNull Optional<String> getHeadTextureOptional(@NotNull ItemStack stack) {
		String texture = getHeadTexture(stack);
		return texture.isBlank() ? Optional.empty() : Optional.of(texture);
	}
}
