package fr.siroz.cariboustonks.features.events.hoppity;

import net.minecraft.ChatFormatting;

/**
 * Represents the current claim status of a Hoppity's Hunt egg
 */
public enum EggStatus {
	WAITING(ChatFormatting.DARK_GRAY, "○"),
	AVAILABLE(ChatFormatting.LIGHT_PURPLE, "◎"),
	CLAIMED(ChatFormatting.GREEN, "✔");

	private final ChatFormatting color;
	private final String symbol;

	EggStatus(ChatFormatting color, String symbol) {
		this.color = color;
		this.symbol = symbol;
	}

	public ChatFormatting getColor() {
		return color;
	}

	public String getSymbol() {
		return symbol;
	}
}
