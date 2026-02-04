package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.core.module.hud.HudConfig;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.awt.Color;

public class SlayerConfig {

	@SerialEntry
	public StatsHud statsHud = new StatsHud();

	@SerialEntry
	public boolean slayerBossCocoonedWarning = false;

	@SerialEntry
	public boolean bossSpawnAlert = true;

	@SerialEntry
	public boolean minibossSpawnAlert = true;

	@SerialEntry
	public boolean highlightBoss = true;

	@SerialEntry
	public Color highlightBossColor = Colors.RED.toAwtColor();

	@SerialEntry
	public boolean highlightMiniboss = true;

	@SerialEntry
	public Color highlightMinibossColor = Colors.RED.toAwtColor();

	@SerialEntry
	public boolean showStatsBreakdown = false;

	@SerialEntry
	public boolean showStatsInChat = false;

	@SerialEntry
	public TarantulaBoss tarantulaBoss = new TarantulaBoss();

	public static class StatsHud implements HudConfig {

		@SerialEntry
		public boolean enabled = false;

		@SerialEntry
		public int x = 250;

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

	public static class TarantulaBoss {

		@SerialEntry
		public boolean highlightBossEggs = true;

		@SerialEntry
		public Color highlightBossEggsColor = Colors.PURPLE.toAwtColor();

		@SerialEntry
		public boolean showCursorLineToBossEggs = false;
	}
}
