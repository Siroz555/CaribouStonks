package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class FishingConfig {

	@SerialEntry
	public boolean bobberTimerDisplay = false;

	@SerialEntry
	public boolean fishCaughtWarning = false;

	@SerialEntry
	public boolean hotspotRadarGuess = false;

	@SerialEntry
	public boolean hotspotHighlight = false;

	@SerialEntry
	public boolean rareSeaCreatureWarning = false;

	@SerialEntry
	public boolean rareSeaCreatureSound = false;
}
