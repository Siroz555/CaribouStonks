package fr.siroz.cariboustonks.core.data.hypixel.item;

import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents SkyBlock's Rarity
 */
public enum Rarity {
	COMMON('C', 1, Formatting.WHITE),
	UNCOMMON('U', 2, Formatting.GREEN),
	RARE('R', 3, Formatting.BLUE),
	EPIC('E', 4, Formatting.DARK_PURPLE),
	LEGENDARY('L', 5, Formatting.GOLD),
	MYTHIC('M', 6, Formatting.LIGHT_PURPLE),
	DIVINE('D', 7, Formatting.AQUA),
	SPECIAL(' ', 8, Formatting.RED),
	VERY_SPECIAL(' ', 9, Formatting.RED),
	ULTIMATE(' ', 10, Formatting.DARK_RED),
	ADMIN(' ', 11, Formatting.DARK_RED),
	UNKNOWN(' ', 555, Formatting.BLACK),
	;

	private static final Rarity[] VALUES = values();

	private final char code;
	private final int power;
	private final int color;
	private final Formatting formatting;

	Rarity(char code, int power, Formatting formatting) {
		this.code = code;
		this.power = power;
		//noinspection DataFlowIssue
		this.color = formatting.getColorValue();
		this.formatting = formatting;
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

	public static @NotNull Optional<Rarity> containsName(@NotNull String name) {
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

	public int getColor() {
		return color;
	}

	public Formatting getFormatting() {
		return formatting;
	}
}
