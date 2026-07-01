package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.core.module.hud.HudAnchor;
import fr.siroz.cariboustonks.core.module.hud.HudConfig;
import fr.siroz.cariboustonks.util.ColorUtils;
import java.awt.Color;
import net.minecraft.network.chat.TextColor;

public class MiscConfig {

	@Deprecated
	@SerialEntry
	public boolean hoppityEggFinderGuess = false; // Event Config -> hoppityHunt -> eggFinderGuess

	@SerialEntry
	public Color highlighterColor = ColorUtils.getAwtColor(TextColor.AQUA);

	@SerialEntry
	public boolean bestiaryHighlight = true;

	@SerialEntry
	public BestiaryTracker bestiaryTracker = new BestiaryTracker();

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

	public static class BestiaryTracker {

		@SerialEntry
		public TrackerHud trackerHud = new TrackerHud();

		@SerialEntry
		public int maxDisplayedEntries = 5;

		public static class TrackerHud implements HudConfig {

			@SerialEntry
			public boolean enabled = false;

			@SerialEntry
			public int x = 20;

			@SerialEntry
			public int y = 100;

			@SerialEntry
			public float scale = 1f;

			@SerialEntry
			public HudAnchor anchor = HudAnchor.TOP_LEFT;

			@Override
			public int x() {
				return this.x;
			}

			@Override
			public void setX(int x) {
				this.x = x;
			}

			@Override
			public int y() {
				return this.y;
			}

			@Override
			public void setY(int y) {
				this.y = y;
			}

			@Override
			public float scale() {
				return this.scale;
			}

			@Override
			public void setScale(float scale) {
				this.scale = scale;
			}

			@Override
			public HudAnchor anchor() {
				return anchor;
			}

			@Override
			public void setAnchor(HudAnchor anchor) {
				this.anchor = anchor;
			}

			@Override
			public boolean shouldRender() {
				return this.enabled;
			}
		}
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
