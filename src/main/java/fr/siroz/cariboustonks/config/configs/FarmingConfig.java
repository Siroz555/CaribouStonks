package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class FarmingConfig {

	@SerialEntry
	public Garden garden = new Garden();

	public static class Garden {

		@SerialEntry
		public boolean pestsLocator = false;

		@SerialEntry
		public boolean highlightInfestedPlots = false;

		@SerialEntry
		public boolean disableWateringCanPlacement = false;

		@SerialEntry
		public boolean greenhouseGrowthStageReminder = true;
	}
}
