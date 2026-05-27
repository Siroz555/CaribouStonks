package fr.siroz.cariboustonks.events;

import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockSeason;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonBoss;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerTier;
import fr.siroz.cariboustonks.core.skyblock.slayer.SlayerType;
import java.time.Instant;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Events related to SkyBlock "gameplay" interactions.
 */
public final class SkyBlockEvents {

	private SkyBlockEvents() {
	}

	/**
	 * Called when the client joins the SkyBlock
	 */
	public static final Event<Join> JOIN_EVENT = EventFactory.createArrayBacked(Join.class, listeners -> serverName -> {
		for (Join listener : listeners) {
			listener.onJoin(serverName);
		}
	});

	/**
	 * Called when the client leaves the SkyBlock
	 */
	public static final Event<Leave> LEAVE_EVENT = EventFactory.createArrayBacked(Leave.class, listeners -> () -> {
		for (Leave listener : listeners) {
			listener.onLeave();
		}
	});

	/**
	 * Called when the client changes of SkyBlock Island
	 */
	public static final Event<IslandChange> ISLAND_CHANGE_EVENT = EventFactory.createArrayBacked(IslandChange.class, listeners -> (location, serverId) -> {
		for (IslandChange listener : listeners) {
			listener.onIslandChange(location, serverId);
		}
	});

	public static final Event<SlayerBossSpawn> SLAYER_BOSS_SPAWN_EVENT = EventFactory.createArrayBacked(SlayerBossSpawn.class, listeners -> (type, tier) -> {
		for (SlayerBossSpawn listener : listeners) {
			listener.onSpawn(type, tier);
		}
	});

	public static final Event<SlayerMinibossSpawn> SLAYER_MINIBOSS_SPAWN_EVENT = EventFactory.createArrayBacked(SlayerMinibossSpawn.class, listeners -> (type, tier) -> {
		for (SlayerMinibossSpawn listener : listeners) {
			listener.onSpawn(type, tier);
		}
	});

	public static final Event<SlayerBossEnd> SLAYER_BOSS_END_EVENT = EventFactory.createArrayBacked(SlayerBossEnd.class, listeners -> (type, tier, instant) -> {
		for (SlayerBossEnd listener : listeners) {
			listener.onEnd(type, tier, instant);
		}
	});

	public static final Event<SlayerQuestStart> SLAYER_QUEST_START_EVENT = EventFactory.createArrayBacked(SlayerQuestStart.class, listeners -> (type, tier, afterUpdate) -> {
		for (SlayerQuestStart listener : listeners) {
			listener.onStart(type, tier, afterUpdate);
		}
	});

	public static final Event<SlayerQuestFail> SLAYER_QUEST_FAIL_EVENT = EventFactory.createArrayBacked(SlayerQuestFail.class, listeners -> (type, tier) -> {
		for (SlayerQuestFail listener : listeners) {
			listener.onFail(type, tier);
		}
	});

	public static final Event<DungeonStart> DUNGEON_START_EVENT = EventFactory.createArrayBacked(DungeonStart.class, listeners -> () -> {
		for (DungeonStart listener : listeners) {
			listener.onDungeonStart();
		}
	});

	public static final Event<DungeonBossSpawn> DUNGEON_BOSS_SPAWN_EVENT = EventFactory.createArrayBacked(DungeonBossSpawn.class, listeners -> boss -> {
		for (DungeonBossSpawn listener : listeners) {
			listener.onBossSpawn(boss);
		}
	});

	public static final Event<YearChange> YEAR_CHANGE_EVENT = EventFactory.createArrayBacked(YearChange.class, listeners -> year -> {
		for (YearChange listener : listeners) {
			listener.onYearChange(year);
		}
	});

	public static final Event<SeasonChange> SEASON_CHANGE_EVENT = EventFactory.createArrayBacked(SeasonChange.class, listeners -> season -> {
		for (SeasonChange listener : listeners) {
			listener.onSeasonChange(season);
		}
	});

	public static final Event<MonthChange> MONTH_CHANGE_EVENT = EventFactory.createArrayBacked(MonthChange.class, listeners -> month -> {
		for (MonthChange listener : listeners) {
			listener.onMonthChange(month);
		}
	});

	public static final Event<DayChange> DAY_CHANGE_EVENT = EventFactory.createArrayBacked(DayChange.class, listeners -> day -> {
		for (DayChange listener : listeners) {
			listener.onDayChange(day);
		}
	});

	public static final Event<HourChange> HOUR_CHANGE_EVENT = EventFactory.createArrayBacked(HourChange.class, listeners -> hour -> {
		for (HourChange listener : listeners) {
			listener.onHourChange(hour);
		}
	});

	@FunctionalInterface
	public interface Join {
		void onJoin(@NonNull String serverName);
	}

	@FunctionalInterface
	public interface Leave {
		void onLeave();
	}

	@FunctionalInterface
	public interface IslandChange {
		void onIslandChange(@NonNull IslandType islandType, String serverId);
	}

	@FunctionalInterface
	public interface SlayerBossSpawn {
		void onSpawn(@NonNull SlayerType type, @NonNull SlayerTier tier);
	}

	@FunctionalInterface
	public interface SlayerMinibossSpawn {
		void onSpawn(@NonNull SlayerType type, @NonNull SlayerTier tier);
	}

	@FunctionalInterface
	public interface SlayerBossEnd {
		void onEnd(@NonNull SlayerType type, @NonNull SlayerTier tier, @Nullable Instant startTime);
	}

	@FunctionalInterface
	public interface SlayerQuestStart {
		void onStart(@NonNull SlayerType type, @NonNull SlayerTier tier, boolean afterUpdate);
	}

	@FunctionalInterface
	public interface SlayerQuestFail {
		void onFail(@NonNull SlayerType type, @NonNull SlayerTier tier);
	}

	@FunctionalInterface
	public interface DungeonStart {
		void onDungeonStart();
	}

	@FunctionalInterface
	public interface DungeonBossSpawn {
		void onBossSpawn(@NonNull DungeonBoss boss);
	}

	@FunctionalInterface
	public interface YearChange {
		void onYearChange(int year);
	}

	@FunctionalInterface
	public interface SeasonChange {
		void onSeasonChange(@NonNull SkyBlockSeason season);
	}

	@FunctionalInterface
	public interface MonthChange {
		void onMonthChange(SkyBlockSeason.@NonNull Month month);
	}

	@FunctionalInterface
	public interface DayChange {
		void onDayChange(int day);
	}

	@FunctionalInterface
	public interface HourChange {
		void onHourChange(int hour);
	}
}
