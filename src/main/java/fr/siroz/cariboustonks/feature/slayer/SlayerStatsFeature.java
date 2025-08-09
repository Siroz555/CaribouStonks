package fr.siroz.cariboustonks.feature.slayer;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.slayer.SlayerTier;
import fr.siroz.cariboustonks.manager.slayer.SlayerType;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.DoubleSummaryStatistics;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;

@ApiStatus.Experimental
public class SlayerStatsFeature extends Feature {

	private static final int MAX_RUNS_STORED = 25;

	private final Deque<SlayerBossRun> runs = new ArrayDeque<>();
	private SlayerBossRun currentRun = null;

	public SlayerStatsFeature() {
		SkyBlockEvents.SLAYER_BOSS_SPAWN.register(this::onBossSpawn);
		SkyBlockEvents.SLAYER_MINIBOSS_SPAWN.register(this::onMinibossSpawn);
		SkyBlockEvents.SLAYER_QUEST_START.register(this::onQuestStart);
		SkyBlockEvents.SLAYER_QUEST_FAIL.register((_type, _tier) -> this.currentRun = null);
		SkyBlockEvents.SLAYER_BOSS_DEATH.register(this::onBossDeath);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@EventHandler(event = "SkyBlockEvents.SLAYER_BOSS_SPAWN")
	private void onBossSpawn(@NotNull SlayerType type, @NotNull SlayerTier tier) {
		if (ConfigManager.getConfig().slayer.bossSpawnAlert) {
			Client.showTitle(Text.literal("Boss spawned!").formatted(Formatting.DARK_RED), 1, 20, 1);
		}
	}

	@EventHandler(event = "SkyBlockEvents.SLAYER_MINIBOSS_SPAWN")
	private void onMinibossSpawn(@NotNull SlayerType type, @NotNull SlayerTier tier) {
		if (ConfigManager.getConfig().slayer.minibossSpawnAlert) {
			Client.showTitle(Text.literal("Miniboss spawned!").formatted(Formatting.RED), 1, 20, 1);
		}
	}

	@EventHandler(event = "SkyBlockEvents.SLAYER_QUEST_START")
	private void onQuestStart(@NotNull SlayerType type, @NotNull SlayerTier tier, boolean afterUpdate) {
		if (!ConfigManager.getConfig().slayer.showStatsBreakdown) return;

		// Permet de reset si le type de slayer change ou le tier
		if (!runs.isEmpty() && afterUpdate) {
			if (runs.getFirst().getSlayerType() != type || runs.getFirst().getSlayerTier() != tier) {
				runs.clear();
			}
		}

		if (afterUpdate && currentRun != null) {
			currentRun.setSlayerType(type);
			currentRun.setSlayerTier(tier);
		} else {
			currentRun = new SlayerBossRun(type, tier);
			currentRun.setQuestStart(Instant.now());
		}
	}

	@EventHandler(event = "SkyBlockEvents.SLAYER_BOSS_DEATH")
	private void onBossDeath(@NotNull SlayerType type, @NotNull SlayerTier tier, @Nullable Instant startTime) {
		if (!ConfigManager.getConfig().slayer.showStatsBreakdown) return;

		if (currentRun != null && type != SlayerType.UNKNOWN && tier != SlayerTier.UNKNOWN) {
			currentRun.setBossSpawn(startTime);
			currentRun.setBossKill(Instant.now());
			currentRun.setExpReward((double) type.getExpPerTier()[tier.ordinal() - 1]); // -1 car UNKNOWN est en premier

			finalizeRun(currentRun);
			showBreakdown(currentRun);

			if (ConfigManager.getConfig().slayer.showStatsInChat) {
				showStats();
			}

			currentRun = null;
		}
	}

	private void finalizeRun(SlayerBossRun run) {
		if (runs.size() >= MAX_RUNS_STORED) {
			runs.removeFirst();
		}

		runs.addLast(run);
	}

	private void showBreakdown(@NotNull SlayerBossRun currentRun) {
		Text message = Text.empty()
				.append(Text.literal("BREAKDOWN ").formatted(Formatting.RED, Formatting.BOLD))
				.append(Text.literal("Spawn: ").formatted(Formatting.GREEN))
				.append(Text.literal(simpleFormatMillis(currentRun.timeToSpawn().toMillis())).formatted(Formatting.YELLOW))
				.append(Text.literal(" Kill: ").formatted(Formatting.RED))
				.append(Text.literal(simpleFormatMillis(currentRun.timeToKill().toMillis())).formatted(Formatting.YELLOW))
				.append(Text.literal(" (Total: ").formatted(Formatting.GRAY))
				.append(Text.literal(simpleFormatMillis(currentRun.cycleDuration().toMillis())).formatted(Formatting.YELLOW))
				.append(Text.literal(")").formatted(Formatting.GRAY));
		Client.sendMessage(message);
	}

	private void showStats() {
		OptionalDouble avgSpawn = averageSecondsToSpawn();
		OptionalDouble avgKill = averageSecondsToKill();
		OptionalDouble bph = bossesPerHour();
		OptionalDouble xph = expPerHour();

		String textSpawnAvg = avgSpawn.isPresent() ? formatDurationSeconds(Math.round(avgSpawn.getAsDouble())) : "N/A";
		String textKillAvg = avgKill.isPresent() ? formatDurationSeconds(Math.round(avgKill.getAsDouble())) : "N/A";
		String textBossPerHour = bph.isPresent() ? String.format("%.2f", bph.getAsDouble()) : "N/A";
		String textExpPerHour = xph.isPresent() ? StonksUtils.SHORT_FLOAT_NUMBERS.format(xph.getAsDouble()) : "N/A";

		Text message = Text.empty()
				.append(Text.literal("STATS ").formatted(Formatting.RED, Formatting.BOLD))
				.append(Text.literal("Spawn Avg: ").formatted(Formatting.DARK_GREEN))
				.append(Text.literal(textSpawnAvg).formatted(Formatting.YELLOW))
				.append(Text.literal(" Kill Avg: ").formatted(Formatting.DARK_RED))
				.append(Text.literal(textKillAvg).formatted(Formatting.YELLOW))
				.append(Text.literal(" Boss/h: ").formatted(Formatting.RED))
				.append(Text.literal(textBossPerHour).formatted(Formatting.YELLOW))
				.append(Text.literal(" EXP/h: ").formatted(Formatting.AQUA))
				.append(Text.literal(textExpPerHour).formatted(Formatting.YELLOW));
		Client.sendMessage(message);
	}

	public OptionalDouble averageSecondsToSpawn() {
		LongSummaryStatistics stats = runs.stream()
				.map(SlayerBossRun::timeToSpawn)
				.filter(Objects::nonNull)
				.mapToLong(Duration::getSeconds)
				.summaryStatistics();

		return stats.getCount() >= 2 ? OptionalDouble.of(stats.getAverage()) : OptionalDouble.empty();
	}

	public OptionalDouble averageSecondsToKill() {
		LongSummaryStatistics stats = runs.stream()
				.map(SlayerBossRun::timeToKill)
				.filter(Objects::nonNull)
				.mapToLong(Duration::getSeconds)
				.summaryStatistics();

		return stats.getCount() >= 2 ? OptionalDouble.of(stats.getAverage()) : OptionalDouble.empty();
	}

	public OptionalDouble averageCycleSeconds() {
		LongSummaryStatistics stats = runs.stream()
				.map(SlayerBossRun::cycleDuration)
				.filter(Objects::nonNull)
				.mapToLong(Duration::getSeconds)
				.summaryStatistics();

		return stats.getCount() >= 2 ? OptionalDouble.of(stats.getAverage()) : OptionalDouble.empty();
	}

	public OptionalDouble averageExpPerBoss() {
		DoubleSummaryStatistics stats = runs.stream()
				.mapToDouble(SlayerBossRun::getExpReward)
				.summaryStatistics();

		return stats.getCount() >= 2 ? OptionalDouble.of(stats.getAverage()) : OptionalDouble.empty();
	}

	/**
	 * bosses per hour = 3600 / averageCycleSeconds
	 */
	public OptionalDouble bossesPerHour() {
		OptionalDouble acs = averageCycleSeconds();
		if (acs.isPresent() && acs.getAsDouble() > 0) {
			return OptionalDouble.of(3600.0 / acs.getAsDouble());
		}

		return OptionalDouble.empty();
	}

	public OptionalDouble expPerHour() {
		OptionalDouble bph = bossesPerHour();
		OptionalDouble aEpb = averageExpPerBoss();
		if (bph.isPresent() && aEpb.isPresent()) {
			return OptionalDouble.of(bph.getAsDouble() * aEpb.getAsDouble());
		}

		return OptionalDouble.empty();
	}

	private String simpleFormatMillis(long millis) {
		return String.format("%.2fs", millis / 1000.0);
	}

	private String formatDurationSeconds(long seconds) {
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = seconds / 3600;
		if (h > 0) return String.format("%dh %02dm %02ds", h, m, s);
		if (m > 0) return String.format("%dm %02ds", m, s);
		return String.format("%ds", s);
	}
}
