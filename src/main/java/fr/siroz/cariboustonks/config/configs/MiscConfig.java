package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

import fr.siroz.cariboustonks.util.colors.ColorUtils;
import java.awt.Color;
import net.minecraft.util.Formatting;

public class MiscConfig {

	@SerialEntry
	public boolean highlightPartyMembers = false;

	@SerialEntry
	public Color highlightPartyMembersColor = Color.GREEN;

	@SerialEntry
	public Color highlighterColor = ColorUtils.getAwtColor(Formatting.AQUA);

	@SerialEntry
	public boolean stopPickobulusAbilityOnDynamic = false;

	@SerialEntry
	public PartyCommands partyCommands = new PartyCommands();

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
