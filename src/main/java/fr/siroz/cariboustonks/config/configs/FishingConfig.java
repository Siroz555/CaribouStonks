package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.core.module.hud.HudConfig;

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

	@SerialEntry
	public LotusAtoll lotusAtoll = new LotusAtoll();

	public static class LotusAtoll {

		@SerialEntry
		public boolean wormholeFinder = false;

		@SerialEntry
		public BuffHud buffHud = new BuffHud();

		@SerialEntry
		public boolean buffExpiredWarn = false;

		public static class BuffHud implements HudConfig {

			@SerialEntry
			public boolean enabled = false;

			@SerialEntry
			public int x = 100;

			@SerialEntry
			public int y = 100;

			@SerialEntry
			public float scale = 1f;

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
			public boolean shouldRender() {
				return this.enabled;
			}
		}
	}
}
