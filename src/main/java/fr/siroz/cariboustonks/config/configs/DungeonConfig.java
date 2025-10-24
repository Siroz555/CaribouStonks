package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.awt.Color;

public class DungeonConfig {

	@SerialEntry
	public Croesus croesus = new Croesus();

	public static class Croesus {

		@SerialEntry
		public boolean mainMenuOpenedChest = false;

		@SerialEntry
		public Color mainMenuOpenedChestColor = Colors.GRAY.withAlpha(0.25f).toAwtColor();

		@SerialEntry
		public boolean mainMenuKismetAvailable = false;

		@SerialEntry
		public Color mainMenuKismetAvailableColor = Colors.BLUE.withAlpha(0.25f).toAwtColor();

		@SerialEntry
		public boolean mainMenuNoMoreChest = false;

		@SerialEntry
		public Color mainMenuNoMoreChestColor = Colors.RED.withAlpha(0.25f).toAwtColor();
	}
}
