package fr.siroz.cariboustonks.manager.slayer;

import net.minecraft.ChatFormatting;

public enum SlayerTier {

	UNKNOWN("?", ChatFormatting.GRAY),
	I("I", ChatFormatting.GREEN),
	II("II", ChatFormatting.YELLOW),
	III("III", ChatFormatting.RED),
	IV("IV", ChatFormatting.DARK_RED),
	V("V", ChatFormatting.DARK_PURPLE);

	private final String name;
	private final ChatFormatting color;

	SlayerTier(String name, ChatFormatting color) {
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public ChatFormatting getColor() {
		return color;
	}
}
