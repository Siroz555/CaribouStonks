package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.core.module.hud.HudConfig;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarPriceType;
import java.util.concurrent.TimeUnit;

public class HuntingConfig {

	@SerialEntry
	public boolean attributeInfos = false;

	@SerialEntry
	public TrackingShards trackingShards = new TrackingShards();

	public static class TrackingShards {

		@SerialEntry
		public int minPreWarmCatch = 6;

		@SerialEntry
		public long inactivityResetMs = TimeUnit.MINUTES.toMillis(2);

		@SerialEntry
		public BazaarPriceType priceType = BazaarPriceType.BUY;

		@SerialEntry
		public Hud hud = new Hud();

		public static class Hud implements HudConfig {

			@SerialEntry
			public boolean enabled = true;

			@SerialEntry
			public int x = 100;

			@SerialEntry
			public int y = 50;

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
