package fr.siroz.cariboustonks.feature.slayer;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.hud.Hud;
import fr.siroz.cariboustonks.manager.hud.HudProvider;
import fr.siroz.cariboustonks.manager.hud.MultiElementHud;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.manager.hud.element.HudElement;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import fr.siroz.cariboustonks.manager.slayer.SlayerTier;
import fr.siroz.cariboustonks.manager.slayer.SlayerType;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.Pair;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
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
public class SlayerStatsFeature extends Feature implements HudProvider {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_slayer");
	private static final int MAX_RUNS_STORED = 11;
	private static final String ARROW = "⤷";

	private final SlayerManager slayerManager;
	private final HudElementBuilder hudBuilder = new HudElementBuilder();

	private final Deque<SlayerBossRun> runs = new ArrayDeque<>();
	private SlayerBossRun currentRun = null;
	private final Stats stats = new Stats();

	public SlayerStatsFeature() {
		this.slayerManager = CaribouStonks.managers().getManager(SlayerManager.class);

		SkyBlockEvents.SLAYER_BOSS_SPAWN.register(this::onBossSpawn);
		SkyBlockEvents.SLAYER_MINIBOSS_SPAWN.register(this::onMinibossSpawn);
		SkyBlockEvents.SLAYER_QUEST_START.register(this::onQuestStart);
		SkyBlockEvents.SLAYER_QUEST_FAIL.register((_type, _tier) -> this.currentRun = null);
		SkyBlockEvents.SLAYER_BOSS_DEATH.register(this::onBossDeath);
		SkyBlockEvents.SLAYER_BOSS_END.register(this::onBossEnd);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@Override
	public @NotNull Pair<Identifier, Identifier> getAttachLayerAfter() {
		return Pair.of(VanillaHudElements.STATUS_EFFECTS, HUD_ID);
	}

	@Override
	public @NotNull Hud getHud() {
		return new MultiElementHud(
				() -> this.isEnabled() && slayerManager.isInQuest(),
				new HudElementTextBuilder()
						.append(Component.literal("Revenant Horror").withStyle(ChatFormatting.DARK_PURPLE))
						.appendSpace()
						.append(Component.literal("§c" + ARROW + " Spawn Avg: §e16.4s"))
						.append(Component.literal("§c" + ARROW + " Kill Avg: §e0.87s"))
						.append(Component.literal("§c" + ARROW + " Boss/h: §e211"))
						.append(Component.literal("§c" + ARROW + " XP/h: §e314K"))
						.appendSpace()
						.append(Component.literal("Session Count: 364").withStyle(ChatFormatting.YELLOW))
						.build(),
				this::getHudLines,
				ConfigManager.getConfig().slayer.statsHud,
				250,
				50
		);
	}

	@EventHandler(event = "SkyBlockEvents.SLAYER_BOSS_SPAWN")
	private void onBossSpawn(@NotNull SlayerType type, @NotNull SlayerTier tier) {
		if (ConfigManager.getConfig().slayer.bossSpawnAlert) {
			Client.showTitle(Component.literal("Boss spawned!").withStyle(ChatFormatting.DARK_RED), 1, 20, 1);
		}
	}

	@EventHandler(event = "SkyBlockEvents.SLAYER_MINIBOSS_SPAWN")
	private void onMinibossSpawn(@NotNull SlayerType type, @NotNull SlayerTier tier) {
		if (ConfigManager.getConfig().slayer.minibossSpawnAlert) {
			Client.showTitle(Component.literal("Miniboss spawned!").withStyle(ChatFormatting.RED), 1, 20, 1);
		}
	}

	@EventHandler(event = "SkyBlockEvents.SLAYER_QUEST_START")
	private void onQuestStart(@NotNull SlayerType type, @NotNull SlayerTier tier, boolean afterUpdate) {
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
	private void onBossDeath(@NotNull SlayerType type, @NotNull SlayerTier tier) {
		if (currentRun != null && type != SlayerType.UNKNOWN && tier != SlayerTier.UNKNOWN) {
			currentRun.setBossKill(Instant.now());
		}
	}

	@EventHandler(event = "SkyBlockEvents.SLAYER_BOSS_END")
	private void onBossEnd(@NotNull SlayerType type, @NotNull SlayerTier tier, @Nullable Instant startTime) {
		if (currentRun != null && type != SlayerType.UNKNOWN && tier != SlayerTier.UNKNOWN) {
			currentRun.setBossSpawn(startTime);
			currentRun.setExpReward(slayerManager.getXpReward(type, tier));

			finalizeRun(currentRun);

			if (ConfigManager.getConfig().slayer.showStatsBreakdown) {
				showBreakdown(currentRun);
			}

			if (ConfigManager.getConfig().slayer.showStatsInChat) {
				showStats();
			}

			currentRun = null;
		}
	}

	private List<? extends HudElement> getHudLines() {
		hudBuilder.clear();

		if (currentRun == null) {
			return hudBuilder.build();
		}

		hudBuilder.appendIconLine(currentRun.getSlayerType().getIcon(), Component.literal(currentRun.getSlayerType().getBossName()).withStyle(currentRun.getSlayerTier().getColor()));
		hudBuilder.appendSpace();
		hudBuilder.appendTableRow(Component.literal(ARROW + " Spawn Avg: ").withStyle(ChatFormatting.DARK_GREEN), Component.literal(stats.spawnAverage).withStyle(ChatFormatting.YELLOW), Component.empty());
		hudBuilder.appendTableRow(Component.literal(ARROW + " Kill Avg: ").withStyle(ChatFormatting.DARK_RED), Component.literal(stats.killAverage).withStyle(ChatFormatting.YELLOW), Component.empty());
		hudBuilder.appendTableRow(Component.literal(ARROW + " Boss/h: ").withStyle(ChatFormatting.RED), Component.literal(stats.bossPerHour).withStyle(ChatFormatting.YELLOW), Component.empty());
		hudBuilder.appendTableRow(Component.literal(ARROW + " XP/h: ").withStyle(ChatFormatting.AQUA), Component.literal(stats.xpPerHour).withStyle(ChatFormatting.YELLOW), Component.empty());
		hudBuilder.appendSpace();
		hudBuilder.appendLine(Component.literal("Session Count: " + stats.sessionCount).withStyle(ChatFormatting.YELLOW));

		return hudBuilder.build();
	}

	private void finalizeRun(SlayerBossRun run) {
		if (runs.size() >= MAX_RUNS_STORED) {
			runs.removeFirst();
		}

		updateStats();
		runs.addLast(run);
	}

	private void showBreakdown(@NotNull SlayerBossRun currentRun) {
		Component message = Component.empty()
				.append(Component.literal("BREAKDOWN ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
				.append(Component.literal("Spawn: ").withStyle(ChatFormatting.GREEN))
				.append(Component.literal(simpleFormatMillis(currentRun.timeToSpawn().toMillis())).withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(" Kill: ").withStyle(ChatFormatting.RED))
				.append(Component.literal(simpleFormatMillis(currentRun.timeToKill().toMillis())).withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(" (Total: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(simpleFormatMillis(currentRun.cycleDuration().toMillis())).withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
		Client.sendMessage(message);
	}

	private void showStats() {
		Component message = Component.empty()
				.append(Component.literal("STATS ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
				.append(Component.literal("Spawn Avg: ").withStyle(ChatFormatting.DARK_GREEN))
				.append(Component.literal(stats.spawnAverage).withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(" Kill Avg: ").withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(stats.killAverage).withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(" Boss/h: ").withStyle(ChatFormatting.RED))
				.append(Component.literal(stats.bossPerHour).withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(" XP/h: ").withStyle(ChatFormatting.AQUA))
				.append(Component.literal(stats.xpPerHour).withStyle(ChatFormatting.YELLOW));
		Client.sendMessage(message);
	}

	private void updateStats() {
		OptionalDouble avgSpawn = averageSecondsToSpawn();
		OptionalDouble avgKill = averageSecondsToKill();
		OptionalDouble bph = bossesPerHour();
		OptionalDouble xph = expPerHour();

		stats.sessionCount++;
		stats.spawnAverage = avgSpawn.isPresent() ? formatDurationSeconds(Math.round(avgSpawn.getAsDouble())) : "N/A";
		stats.killAverage = avgKill.isPresent() ? formatDurationSeconds(Math.round(avgKill.getAsDouble())) : "N/A";
		stats.bossPerHour = bph.isPresent() ? String.format("%.2f", bph.getAsDouble()) : "N/A";
		stats.xpPerHour = xph.isPresent() ? StonksUtils.SHORT_FLOAT_NUMBERS.format(xph.getAsDouble()) : "N/A";
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

	private static class Stats {
		int sessionCount = 0;
		String spawnAverage = "N/A";
		String killAverage = "N/A";
		String bossPerHour = "N/A";
		String xpPerHour = "N/A";
	}
}
