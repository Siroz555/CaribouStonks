package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.core.module.hud.HudConfig;

public class EventConfig {

	@SerialEntry
	public HoppityHunt hoppityHunt = new HoppityHunt();

	public static class HoppityHunt {

		@SerialEntry
		public boolean eggFinderGuess = false;

		@SerialEntry
		public HuntHud huntHud = new HuntHud();

		@SerialEntry
		public boolean huntNotification = false;

		public static class HuntHud implements HudConfig {

			@SerialEntry
			public boolean showHud = false;

			@SerialEntry
			public int x = 150;

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
				return this.showHud;
			}
		}
	}
}
