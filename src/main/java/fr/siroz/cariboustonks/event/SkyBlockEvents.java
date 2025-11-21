package fr.siroz.cariboustonks.event;

import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.manager.dungeon.DungeonBoss;
import fr.siroz.cariboustonks.manager.slayer.SlayerTier;
import fr.siroz.cariboustonks.manager.slayer.SlayerType;
import java.time.Instant;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Events related to SkyBlock "gameplay" interactions.
 */
public final class SkyBlockEvents {

	private SkyBlockEvents() {
	}

	/**
	 * Called when the client joins the SkyBlock
	 */
	@OnlySkyBlock
	public static final Event<@NotNull Join> JOIN = EventFactory.createArrayBacked(Join.class, listeners -> (serverName) -> {
		for (Join listener : listeners) {
			listener.onJoin(serverName);
		}
	});

	/**
	 * Called when the client leaves the SkyBlock
	 */
	@OnlySkyBlock
	public static final Event<@NotNull Leave> LEAVE = EventFactory.createArrayBacked(Leave.class, listeners -> () -> {
		for (Leave listener : listeners) {
			listener.onLeave();
		}
	});

	/**
	 * Called when the client changes of SkyBlock Island
	 */
	@OnlySkyBlock
	public static final Event<@NotNull IslandChange> ISLAND_CHANGE = EventFactory.createArrayBacked(IslandChange.class, listeners -> (location) -> {
		for (IslandChange listener : listeners) {
			listener.onIslandChange(location);
		}
	});

	@OnlySkyBlock
	public static final Event<@NotNull SlayerBossSpawn> SLAYER_BOSS_SPAWN = EventFactory.createArrayBacked(SlayerBossSpawn.class, listeners -> (type, tier) -> {
		for (SlayerBossSpawn listener : listeners) {
			listener.onSpawn(type, tier);
		}
	});

	@OnlySkyBlock
	public static final Event<@NotNull SlayerMinibossSpawn> SLAYER_MINIBOSS_SPAWN = EventFactory.createArrayBacked(SlayerMinibossSpawn.class, listeners -> (type, tier) -> {
		for (SlayerMinibossSpawn listener : listeners) {
			listener.onSpawn(type, tier);
		}
	});

	@OnlySkyBlock
	public static final Event<@NotNull SlayerBossDeath> SLAYER_BOSS_DEATH = EventFactory.createArrayBacked(SlayerBossDeath.class, listeners -> (type, tier, instant) -> {
		for (SlayerBossDeath listener : listeners) {
			listener.onDeath(type, tier, instant);
		}
	});

	@OnlySkyBlock
	public static final Event<@NotNull SlayerQuestStart> SLAYER_QUEST_START = EventFactory.createArrayBacked(SlayerQuestStart.class, listeners -> (type, tier, afterUpdate) -> {
		for (SlayerQuestStart listener : listeners) {
			listener.onStart(type, tier, afterUpdate);
		}
	});

	@OnlySkyBlock
	public static final Event<@NotNull SlayerQuestFail> SLAYER_QUEST_FAIL = EventFactory.createArrayBacked(SlayerQuestFail.class, listeners -> (type, tier) -> {
		for (SlayerQuestFail listener : listeners) {
			listener.onFail(type, tier);
		}
	});

	@OnlySkyBlock
	public static final Event<@NotNull DungeonStart> DUNGEON_START = EventFactory.createArrayBacked(DungeonStart.class, listeners -> () -> {
		for (DungeonStart listener : listeners) {
			listener.onDungeonStart();
		}
	});

	@OnlySkyBlock
	public static final Event<@NotNull DungeonBossSpawn> DUNGEON_BOSS_SPAWN = EventFactory.createArrayBacked(DungeonBossSpawn.class, listeners -> (boss) -> {
		for (DungeonBossSpawn listener : listeners) {
			listener.onBossSpawn(boss);
		}
	});

	@FunctionalInterface
	public interface Join {
		void onJoin(@NotNull String serverName);
	}

	@FunctionalInterface
	public interface Leave {
		void onLeave();
	}

	@FunctionalInterface
	public interface IslandChange {
		void onIslandChange(@NotNull IslandType islandType);
	}

	@FunctionalInterface
	public interface SlayerBossSpawn {
		void onSpawn(@NotNull SlayerType type, @NotNull SlayerTier tier);
	}

	@FunctionalInterface
	public interface SlayerMinibossSpawn {
		void onSpawn(@NotNull SlayerType type, @NotNull SlayerTier tier);
	}

	@FunctionalInterface
	public interface SlayerBossDeath {
		void onDeath(@NotNull SlayerType type, @NotNull SlayerTier tier, @Nullable Instant startTime);
	}

	@FunctionalInterface
	public interface SlayerQuestStart {
		void onStart(@NotNull SlayerType type, @NotNull SlayerTier tier, boolean afterUpdate);
	}

	@FunctionalInterface
	public interface SlayerQuestFail {
		void onFail(@NotNull SlayerType type, @NotNull SlayerTier tier);
	}

	@FunctionalInterface
	public interface DungeonStart {
		void onDungeonStart();
	}

	@FunctionalInterface
	public interface DungeonBossSpawn {
		void onBossSpawn(@NotNull DungeonBoss boss);
	}
}
