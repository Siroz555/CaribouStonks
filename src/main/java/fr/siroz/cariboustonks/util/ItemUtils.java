package fr.siroz.cariboustonks.util;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import fr.siroz.cariboustonks.CaribouStonks;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;
import java.util.function.Predicate;

public final class ItemUtils {

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

	@Deprecated
	public static @NotNull Optional<Item> getItemByNumericId(int id) {
		return Registries.ITEM.stream().filter(item -> Registries.ITEM.getRawId(item) == id).findFirst();
	}

	public static Optional<Item> getItemById(@NotNull String id) {
		try {
			Identifier search = Identifier.of(id);
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
}
