package fr.siroz.cariboustonks.core.skyblock.data.hypixel.item;

import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents SkyBlock's Rarity
 */
public enum Rarity {
	COMMON('C', 1, TextColor.WHITE),
	UNCOMMON('U', 2, TextColor.GREEN),
	RARE('R', 3, TextColor.BLUE),
	EPIC('E', 4, TextColor.DARK_PURPLE),
	LEGENDARY('L', 5, TextColor.GOLD),
	MYTHIC('M', 6, TextColor.LIGHT_PURPLE),
	DIVINE('D', 7, TextColor.AQUA),
	SPECIAL(' ', 8, TextColor.RED),
	VERY_SPECIAL(' ', 9, TextColor.RED),
	ULTIMATE(' ', 10, TextColor.DARK_RED),
	ADMIN(' ', 11, TextColor.DARK_RED),
	UNKNOWN(' ', 555, TextColor.BLACK),
	;

	private static final Rarity[] VALUES = values();

	private final char code;
	private final int power;
	private final TextColor color;

	Rarity(char code, int power, TextColor color) {
		this.code = code;
		this.power = power;
		this.color = color;
	}

	public static Rarity fromName(@Nullable String name) {
		if (name == null) return UNKNOWN;

		for (Rarity rarity : VALUES) {
			if (rarity.name().equalsIgnoreCase(name)) {
				return rarity;
			}
		}

		return UNKNOWN;
	}

	public static Rarity fromCode(char code) {
		char upper = Character.toUpperCase(code);
		for (Rarity rarity : VALUES) {
			if (rarity.code == upper) {
				return rarity;
			}
		}

		return UNKNOWN;
	}

	public static @NonNull Optional<Rarity> containsName(@NonNull String name) {
		return Streams.findLast(Arrays.stream(VALUES).filter(rarity -> name.contains(rarity.name())));
	}

	public int getIndex() {
		return ordinal();
	}

	public char getCode() {
		return code;
	}

	public int getPower() {
		return power;
	}

	public TextColor getColor() {
		return color;
	}
}
