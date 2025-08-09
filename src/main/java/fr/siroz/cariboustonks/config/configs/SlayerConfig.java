package fr.siroz.cariboustonks.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.util.colors.Colors;
import java.awt.Color;

public class SlayerConfig {

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

	public static class TarantulaBoss {

		@SerialEntry
		public boolean highlightBossEggs = true;

		@SerialEntry
		public Color highlightBossEggsColor = Colors.PURPLE.toAwtColor();

		@SerialEntry
		public boolean showCursorLineToBossEggs = false;
	}
}
