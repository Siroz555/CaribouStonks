package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.util.ColorUtils;
import java.awt.Color;
import net.minecraft.ChatFormatting;

public class MiscConfig {

	@Deprecated
	@SerialEntry
	public boolean hoppityEggFinderGuess = false; // Event Config -> hoppityHunt -> eggFinderGuess

	@SerialEntry
	public Color highlighterColor = ColorUtils.getAwtColor(ChatFormatting.AQUA);

	@SerialEntry
	public boolean bestiaryHighlight = true;

	@SerialEntry
	public boolean showHexOnDyedItemEverywhere = false;

	@SerialEntry
	public boolean serverTracker = false;

	@SerialEntry
	public boolean disableAbiphonePlacement = false;

	@SerialEntry
	public Compatibility compatibility = new Compatibility();

	@SerialEntry
	public PartyCommands partyCommands = new PartyCommands();

	public static class Compatibility {

		@SerialEntry
		public boolean reiSearchBarCalculator = false; // REI / JEI
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
