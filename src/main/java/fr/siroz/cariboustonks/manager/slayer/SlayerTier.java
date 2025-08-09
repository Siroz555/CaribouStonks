package fr.siroz.cariboustonks.manager.slayer;

import net.minecraft.util.Formatting;

public enum SlayerTier {

	UNKNOWN("unknown", Formatting.GRAY),
	I("I", Formatting.GREEN),
	II("II", Formatting.YELLOW),
	III("III", Formatting.RED),
	IV("IV", Formatting.DARK_RED),
	V("V", Formatting.DARK_PURPLE);

	private final String name;
	private final Formatting color;

	SlayerTier(String name, Formatting color) {
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public Formatting getColor() {
		return color;
	}
}
