package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.core.module.color.Colors;
import java.awt.Color;

public class InstanceConfig {

	@SerialEntry
	public Croesus croesus = new Croesus();

	@SerialEntry
	public TheCatacombs theCatacombs = new TheCatacombs();

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

	public static class TheCatacombs {

		@SerialEntry
		public boolean bossThornSpiritBearTimers = false;

		@SerialEntry
		public boolean bossSadanTerracottaTimers = false;

		@SerialEntry
		public WitherKing witherKing = new WitherKing();

		public static class WitherKing {

			@SerialEntry
			public boolean dragPrio = false;

			@SerialEntry
			public boolean dragPrioMessage = true;

			@SerialEntry
			public boolean dragPrioTitle = true;

			@SerialEntry
			public boolean showSpawnTime = false;

			@SerialEntry
			public boolean showDragBoundingBox = false;

			@SerialEntry
			public boolean showDragTargetLine = false;

			@SerialEntry
			public boolean showLastBreathTarget = false;
		}
	}
}
