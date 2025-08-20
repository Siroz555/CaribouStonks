package fr.siroz.cariboustonks.core.skyblock;

import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

/**
 * Represents SkyBlock's Rarity
 */
public enum Rarity {
	UNKNOWN(' ', 0, Formatting.GRAY),
	COMMON('C', 1, Formatting.WHITE),
	UNCOMMON('U', 2, Formatting.GREEN),
	RARE('R', 3, Formatting.BLUE),
	EPIC('E', 4, Formatting.DARK_PURPLE),
	LEGENDARY('L', 5, Formatting.GOLD),
	SPECIAL(' ', 6, Formatting.RED),
	VERY_SPECIAL(' ', 7, Formatting.RED),
	MYTHIC('M', 8, Formatting.LIGHT_PURPLE),
	DIVINE('D', 9, Formatting.AQUA),
	ULTIMATE(' ', 10, Formatting.DARK_RED),
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
