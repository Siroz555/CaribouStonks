package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class MiningConfig {

	@SerialEntry
	public Mineshaft mineshaft = new Mineshaft();

	public static class Mineshaft {

		@SerialEntry
		public boolean corpseFinder = false;
	}
}
