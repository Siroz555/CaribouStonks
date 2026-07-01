package fr.siroz.cariboustonks.features.misc.bestiary;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.tablist.TabLine;
import fr.siroz.cariboustonks.core.skyblock.tablist.TabWidget;
import fr.siroz.cariboustonks.util.TimeUtils;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class BestiaryTrackerFeature extends Feature {
	private static final String BESTIARY_TAB_LIST_NAME = "Bestiary";
	private final Map<String, BestiaryMobStats> trackedMobs = new LinkedHashMap<>();
	@Nullable
	private List<BestiaryMobStats> active;

	public BestiaryTrackerFeature() {
		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(CaribouStonks.identifier("bestiary_tracker"))
				.hud(new MultiElementHud("bestiary_tracker",
						() -> this.isEnabled() && !this.trackedMobs.isEmpty(),
						preview -> preview
								.appendTitle(Component.literal("§6§l❃ Bestiary Tracker"))
								.appendSpace()
								.appendLine(Component.literal("§fGhost §b18000/20000 §a2669§7/h §e26m 42s")),
						this::getHudLines,
						this.config().misc.bestiaryTracker.trackerHud,
						20,
						100
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().misc.bestiaryTracker.trackerHud.enabled;
	}

	@Override
	protected void onSecondPassed() {
		if (!isEnabled()) {
			reset();
			return;
		}

		CaribouStonks.skyBlock()
				.getTabListManager()
				.get(BESTIARY_TAB_LIST_NAME)
				.ifPresentOrElse(this::processWidget, this::reset);
	}

	private void processWidget(TabWidget widget) {
		Instant now = Instant.now();

		for (TabLine line : widget.getLines()) {
			BestiaryEntry.parse(line).ifPresent(entry -> {
				if (entry.isMaxed()) {
					trackedMobs.remove(entry.mobName());
					return;
				}

				trackedMobs.compute(entry.mobName(), (_, stats) -> {
					if (stats == null) return new BestiaryMobStats(entry, now);
					stats.update(entry, now);
					return stats;
				});
			});
		}

		trackedMobs.values().removeIf(stats -> stats.isStale(now));

		active = trackedMobs.values().stream()
				.filter(BestiaryMobStats::isActive)
				.sorted(Comparator.comparingDouble((BestiaryMobStats s) -> s.getKillsPerHour(now)).reversed())
				.limit(this.config().misc.bestiaryTracker.maxDisplayedEntries)
				.toList();
	}

	private void getHudLines(HudElementBuilder builder) {
		if (active == null || active.isEmpty()) return;

		builder.appendTitle(Component.literal("❃ Bestiary Tracker").withColor(Colors.GOLD_RGB).withStyle(ChatFormatting.BOLD));

		Instant now = Instant.now();
		for (BestiaryMobStats stats : active) {
			// Pas de table row pour éviter de trop gros décalages,
			// vu que les noms des mobs peuvent être très long comme court...
			builder.appendLine(Component.empty()
							.append(stats.getLineComponent())
							.append(Component.literal(" "))
							.append(appendRate(stats, now))
							.append(Component.literal(" "))
							.append(appendEta(stats, now)))
					.appendSpace();
		}
	}

	private void reset() {
		trackedMobs.clear();
		active = null;
	}

	private Component appendRate(BestiaryMobStats stats, Instant now) {
		if (!stats.hasEnoughDataForStats()) {
			return Component.literal("...").withColor(Colors.GRAY_RGB);
		}

		return Component.literal("" + stats.getKillsPerHour(now)).withColor(Colors.GREEN_RGB)
				.append(Component.literal("/h").withColor(Colors.GRAY_RGB));
	}

	private Component appendEta(BestiaryMobStats stats, Instant now) {
		if (!stats.hasEnoughDataForStats()) {
			return Component.literal("...").withColor(Colors.GRAY_RGB);
		}

		return stats.getEtaInstant(now)
				.map(TimeUtils::getDurationFormatted)
				.map(formatted -> Component.literal(formatted).withColor(Colors.YELLOW_RGB))
				.orElse(Component.literal("--").withColor(Colors.GRAY_RGB));
	}
}
