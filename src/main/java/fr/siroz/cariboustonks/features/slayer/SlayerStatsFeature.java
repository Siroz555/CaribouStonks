package fr.siroz.cariboustonks.features.slayer;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.annotation.Experimental;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.core.module.hud.element.HudElement;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.Mayor;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.election.Perk;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerManager;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerTier;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerType;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Experimental
public class SlayerStatsFeature extends Feature {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_slayer");
	private static final int MAX_RUNS_STORED = 11;
	private static final String ARROW = "⤷";

	private final SlayerManager slayerManager;
	private final HudElementBuilder hudBuilder = new HudElementBuilder();

	private final Deque<SlayerBossRun> runs = new ArrayDeque<>();
	private SlayerBossRun currentRun = null;
	private final Stats stats = new Stats();
	private boolean xpBuffActive = false;

	public SlayerStatsFeature() {
		this.slayerManager = CaribouStonks.skyBlock().getSlayerManager();

		SkyBlockEvents.SLAYER_BOSS_SPAWN_EVENT.register(this::onBossSpawn);
		SkyBlockEvents.SLAYER_MINIBOSS_SPAWN_EVENT.register(this::onMinibossSpawn);
		SkyBlockEvents.SLAYER_QUEST_START_EVENT.register(this::onQuestStart);
		SkyBlockEvents.SLAYER_QUEST_FAIL_EVENT.register((_type, _tier) -> this.currentRun = null);
		SkyBlockEvents.SLAYER_BOSS_END_EVENT.register(this::onBossEnd);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(HUD_ID)
				.hud(new MultiElementHud(
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
						this.config().slayer.statsHud,
						250,
						50
				))
				.build()
		);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@EventHandler(event = "SkyBlockEvents.SLAYER_BOSS_SPAWN_EVENT")
	private void onBossSpawn(@NonNull SlayerType type, @NonNull SlayerTier tier) {
		if (this.config().slayer.bossSpawnAlert) {
			Client.showTitle(Component.literal("Boss spawned!").withStyle(ChatFormatting.DARK_RED), 1, 15, 1);
		}
	}

	@EventHandler(event = "SkyBlockEvents.SLAYER_MINIBOSS_SPAWN_EVENT")
	private void onMinibossSpawn(@NonNull SlayerType type, @NonNull SlayerTier tier) {
		if (this.config().slayer.minibossSpawnAlert) {
			Client.showTitle(Component.literal("Miniboss spawned!").withStyle(ChatFormatting.RED), 1, 15, 1);
		}
	}

	@EventHandler(event = "SkyBlockEvents.SLAYER_QUEST_START_EVENT")
	private void onQuestStart(@NonNull SlayerType type, @NonNull SlayerTier tier, boolean afterUpdate) {
		// Permet de reset si le type de slayer change ou le tier
		if (!runs.isEmpty()) {
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

	@EventHandler(event = "SkyBlockEvents.SLAYER_BOSS_END_EVENT")
	private void onBossEnd(@NonNull SlayerType type, @NonNull SlayerTier tier, @Nullable Instant startTime) {
		if (currentRun != null && type != SlayerType.UNKNOWN && tier != SlayerTier.UNKNOWN) {
			currentRun.setBossSpawn(startTime);
			// SIROZ-NOTE FUTURE UPDATE
			//  (without -1350ms = wrong time (like skyblocker)
			//  -1350ms = OKAY
			//  Mixin detection = ~perfect
			// Le boss end est trigger par le message auto d'Hypixel APRES le kill.
			// Techniquement, le boss kill est quand la vie du boss est à 0.
			// La détection ne peut pas se faire lorsque le boss est remove,
			// car il y a un délai entre le boss avec 0 health est la suppression du boss coté client.
			// De plus le Mixin marche en 1.21.10 mais pas en 1.21.11
			// Ce délai est de ~1.3/1.5s. Le problème, c'est que je n'ai pas de Mixin qui me
			// permettrai de détecter la vie du boss à 0 en 1.21.11
			currentRun.setBossKill(Instant.now().minusMillis(1350));
			currentRun.setExpReward(slayerManager.getXpReward(type, tier));

			finalizeRun(currentRun);

			if (this.config().slayer.showStatsBreakdown) {
				showBreakdown(currentRun);
			}

			if (this.config().slayer.showStatsInChat) {
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

		hudBuilder.appendIconLine(currentRun.getSlayerType().getIcon(), Component.literal(currentRun.getSlayerType().getBossName() + " " + currentRun.getSlayerTier().getName()).withStyle(currentRun.getSlayerTier().getColor()));
		hudBuilder.appendSpace();
		hudBuilder.appendTableRow(Component.literal(ARROW + " Spawn Avg: ").withStyle(ChatFormatting.DARK_GREEN), Component.literal(stats.spawnAverage).withStyle(ChatFormatting.YELLOW), Component.empty());
		hudBuilder.appendTableRow(Component.literal(ARROW + " Kill Avg: ").withStyle(ChatFormatting.DARK_RED), Component.literal(stats.killAverage).withStyle(ChatFormatting.YELLOW), Component.empty());
		hudBuilder.appendTableRow(Component.literal(ARROW + " Boss/h: ").withStyle(ChatFormatting.RED), Component.literal(stats.bossPerHour).withStyle(ChatFormatting.YELLOW), Component.empty());
		hudBuilder.appendTableRow(Component.literal(ARROW + " XP/h: ").withStyle(ChatFormatting.AQUA), Component.literal(stats.xpPerHour).withStyle(ChatFormatting.YELLOW), Component.empty());
		if (xpBuffActive) {
			hudBuilder.appendLine(Component.empty()
					.append(Component.literal(" x1.25").withStyle(ChatFormatting.LIGHT_PURPLE))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal("Aatrox XP Buff").withStyle(ChatFormatting.DARK_AQUA))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY)));
		}
		hudBuilder.appendSpace();
		hudBuilder.appendLine(Component.literal("Session Count: " + stats.sessionCount).withStyle(ChatFormatting.YELLOW));

		return hudBuilder.build();
	}

	private void finalizeRun(SlayerBossRun run) {
		if (runs.size() >= MAX_RUNS_STORED) {
			runs.removeFirst();
		}
		// Update all stats
		updateStats();
		// Simply add this run to the list
		runs.addLast(run);
		// Update if the Aatrox XP Buff is present
		xpBuffActive = SkyBlockAPI.isMayorOrMinister(Mayor.AATROX, Perk.SLAYER_XP_BUFF);
	}

	private void showBreakdown(@NonNull SlayerBossRun currentRun) {
		Component message = Component.empty()
				.append(Component.literal("BREAKDOWN ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
				.append(Component.literal("Spawn: ").withStyle(ChatFormatting.GREEN))
				.append(Component.literal(currentRun.timeToSpawn() != null ? simpleFormatMillis(currentRun.timeToSpawn().toMillis()) : "N/A").withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(" Kill: ").withStyle(ChatFormatting.RED))
				.append(Component.literal(currentRun.timeToKill() != null ? simpleFormatMillis(currentRun.timeToKill().toMillis()) : "N/A").withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(" (Total: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(currentRun.cycleDuration() != null ? simpleFormatMillis(currentRun.cycleDuration().toMillis()) : "N/A").withStyle(ChatFormatting.YELLOW))
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
