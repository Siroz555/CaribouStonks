package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class FishingConfig {

	@SerialEntry
	public boolean fishCaughtWarning = false;

	@SerialEntry
	public boolean hotspotRadarGuess = false;

	@SerialEntry
	public boolean hotspotHighlight = false;
}
