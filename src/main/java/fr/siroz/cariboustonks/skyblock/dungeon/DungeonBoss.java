package fr.siroz.cariboustonks.skyblock.dungeon;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

/**
 * Enum representing the different Dungeon bosses. <a href="https://wiki.hypixel.net/Dungeons">Hypixel Wiki Dungeons</a>
 */
public enum DungeonBoss {

	BONZO(DungeonType.THE_CATACOMBS, "[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable."),
	SCARF(DungeonType.THE_CATACOMBS, "[BOSS] Scarf: This is where the journey ends for you, Adventurers."),
	PROFESSOR(DungeonType.THE_CATACOMBS, "[BOSS] The Professor: I was burdened with terrible news recently..."),
	THORN(DungeonType.THE_CATACOMBS, "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!"),
	LIVID(DungeonType.THE_CATACOMBS, "[BOSS] Livid: Welcome, you've arrived right on time. I am Livid, the Master of Shadows."),
	SADAN(DungeonType.THE_CATACOMBS, "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!"),
	NECRON(DungeonType.THE_CATACOMBS, "[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!"),
	UNKNOWN(DungeonType.UNKNOWN, "");

	private static final Map<String, DungeonBoss> BY_TRIGGER_BOSS_MESSAGE = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(DungeonBoss::getTriggerBossMessage, Function.identity()));

	private final DungeonType dungeonType;
	private final String triggerBossMessage;

	DungeonBoss(DungeonType dungeonType, String triggerBossMessage) {
		this.dungeonType = dungeonType;
		this.triggerBossMessage = triggerBossMessage;
	}

	public DungeonType getDungeonType() {
		return dungeonType;
	}

	public String getTriggerBossMessage() {
		return triggerBossMessage;
	}

	public static DungeonBoss fromTriggerBossMessage(@Nullable String name) {
		if (name == null) return UNKNOWN;
		return BY_TRIGGER_BOSS_MESSAGE.getOrDefault(name, UNKNOWN);
	}
}
