package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class CombatConfig {

	@SerialEntry
	public CocoonedMob cocoonedMob = new CocoonedMob();

	@SerialEntry
	public LowHealthWarning lowHealthWarning = new LowHealthWarning();

	@SerialEntry
	public SecondLife secondLife = new SecondLife();

	public static class CocoonedMob {

		@SerialEntry
		public boolean cocoonedWarning = false;

		@SerialEntry
		public boolean cocoonedWarningTitle = true;

		@SerialEntry
		public boolean cocoonedWarningSound = true;

		@SerialEntry
		public boolean cocoonedWarningBeam = false;
	}

	public static class LowHealthWarning {

		@SerialEntry
		public boolean lowHealthWarningEnabled = true;

		@SerialEntry
		public int lowHealthWarningThreshold = 15;
	}

	public static class SecondLife {

		@SerialEntry
		public boolean spiritMaskUsed = false;

		@SerialEntry
		public boolean spiritMaskBack = false;
	}
}
