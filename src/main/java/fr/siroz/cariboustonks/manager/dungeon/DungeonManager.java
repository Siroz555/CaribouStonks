package fr.siroz.cariboustonks.manager.dungeon;

import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.manager.Manager;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public final class DungeonManager implements Manager {

	private DungeonBoss boss = DungeonBoss.UNKNOWN;
	private DungeonClass dungeonClass = DungeonClass.UNKNOWN;

	public DungeonManager() {
		ChatEvents.MESSAGE_RECEIVED.register(this::onMessage);
		ClientPlayConnectionEvents.JOIN.register((_h, _s, _c) -> this.reset());
	}

	public DungeonBoss getBoss() {
		return boss;
	}

	public boolean isInBoss() {
		return boss != DungeonBoss.UNKNOWN;
	}

	public DungeonClass getDungeonClass() {
		return dungeonClass;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onMessage(@NotNull Component text) {
		if (!SkyBlockAPI.isOnSkyBlock()) return;
		if (SkyBlockAPI.getIsland() != IslandType.DUNGEON) return;

		String message = StonksUtils.stripColor(text.getString());
		//if (message.equals("[NPC] Mort: You should find it useful if you get lost.")) {
		if (message.equals("[NPC] Mort: Here, I found this map when I first entered the dungeon.")) {
			SkyBlockEvents.DUNGEON_START.invoker().onDungeonStart();
		}

		updateDungeonClass(message);
		updateBoss(message);
	}

	private void updateBoss(String message) {
		DungeonBoss newBoss = DungeonBoss.fromTriggerBossMessage(message);
		if (!isInBoss() && newBoss != DungeonBoss.UNKNOWN) {
			boss = newBoss;
			SkyBlockEvents.DUNGEON_BOSS_SPAWN.invoker().onBossSpawn(newBoss);
		}
	}

	private void updateDungeonClass(String message) {
		String playerName = Client.getPlayerName();
		if (playerName == null) return;

		for (DungeonClass dgClass : DungeonClass.values()) {
			String byTag = "[" + dgClass.getName() + "]";
			String bySelection = playerName + " selected the " + dgClass.getName() + " Class!";
			String byMilestone = dgClass.getName() + " Milestone";
			if (message.startsWith(byTag) || message.equals(bySelection) || message.startsWith(byMilestone)) {
				dungeonClass = dgClass;
				break;
			}
		}
	}

	private void reset() {
		boss = DungeonBoss.UNKNOWN;
		dungeonClass = DungeonClass.UNKNOWN;
	}
}
