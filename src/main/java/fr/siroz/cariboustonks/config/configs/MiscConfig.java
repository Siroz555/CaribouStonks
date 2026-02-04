package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.util.colors.ColorUtils;
import java.awt.Color;
import net.minecraft.ChatFormatting;

public class MiscConfig {

	@SerialEntry
	public boolean hoppityEggFinderGuess = false;

	@SerialEntry
	public Color highlighterColor = ColorUtils.getAwtColor(ChatFormatting.AQUA);

	@SerialEntry
	public boolean showHexOnDyedItemEverywhere = false;

	@SerialEntry
	public Compatibility compatibility = new Compatibility();

	@SerialEntry
	public PartyCommands partyCommands = new PartyCommands();

	public static class Compatibility {

		@SerialEntry
		public boolean reiSearchBarCalculator = false;
	}

	public static class PartyCommands {

		@SerialEntry
		public boolean enabled = false;

		@SerialEntry
		public boolean coords = false;

		@SerialEntry
		public boolean warp = false;

		@SerialEntry
		public boolean magic8Ball = false;

		@SerialEntry
		public boolean diceGame = false;

		@SerialEntry
		public boolean coinFlip = false;

		@SerialEntry
		public boolean tps = false;
	}
}
