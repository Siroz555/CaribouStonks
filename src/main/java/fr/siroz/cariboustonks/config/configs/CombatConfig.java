package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class CombatConfig {

	@SerialEntry
	public CocoonedMob cocoonedMob = new CocoonedMob();

	@SerialEntry
	public LowHealthWarning lowHealthWarning = new LowHealthWarning();

	public static class CocoonedMob {

		@SerialEntry
		public boolean cocoonedWarning = true;
	}

	public static class LowHealthWarning {

		@SerialEntry
		public boolean lowHealthWarningEnabled = true;

		@SerialEntry
		public int lowHealthWarningThreshold = 15;
	}
}
