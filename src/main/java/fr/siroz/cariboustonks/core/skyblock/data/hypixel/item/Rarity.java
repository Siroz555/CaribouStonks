package fr.siroz.cariboustonks.core.skyblock.data.hypixel.item;

import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents SkyBlock's Rarity
 */
public enum Rarity {
	COMMON('C', 1, ChatFormatting.WHITE),
	UNCOMMON('U', 2, ChatFormatting.GREEN),
	RARE('R', 3, ChatFormatting.BLUE),
	EPIC('E', 4, ChatFormatting.DARK_PURPLE),
	LEGENDARY('L', 5, ChatFormatting.GOLD),
	MYTHIC('M', 6, ChatFormatting.LIGHT_PURPLE),
	DIVINE('D', 7, ChatFormatting.AQUA),
	SPECIAL(' ', 8, ChatFormatting.RED),
	VERY_SPECIAL(' ', 9, ChatFormatting.RED),
	ULTIMATE(' ', 10, ChatFormatting.DARK_RED),
	ADMIN(' ', 11, ChatFormatting.DARK_RED),
	UNKNOWN(' ', 555, ChatFormatting.BLACK),
	;

	private static final Rarity[] VALUES = values();

	private final char code;
	private final int power;
	private final int color;
	private final ChatFormatting formatting;

	Rarity(char code, int power, ChatFormatting formatting) {
		this.code = code;
		this.power = power;
		//noinspection DataFlowIssue
		this.color = formatting.getColor();
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

	public ChatFormatting getFormatting() {
		return formatting;
	}
}
