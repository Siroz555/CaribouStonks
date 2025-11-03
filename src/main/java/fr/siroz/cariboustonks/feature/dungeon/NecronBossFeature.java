package fr.siroz.cariboustonks.feature.dungeon;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.dungeon.DungeonBoss;
import fr.siroz.cariboustonks.manager.dungeon.DungeonManager;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class NecronBossFeature extends Feature {

	private static final String STORM_DEATH_MESSAGE = "[BOSS] Storm: I should have known that I stood no chance.";
	private static final String GOLDOR_TRIGGER_MESSAGE = "[BOSS] Goldor: Who dares trespass into my domain?";
	private static final String GOLDOR_DEATH_MESSAGE = "The Core entrance is opening!";
	private static final int GOLDOR_START_TIME = 104;
	private static final int GOLDOR_TICKS = 60;

	private final DungeonManager dungeonManager;

	private int goldorTicks = -1;
	private int goldorStartTime = -1;

	public NecronBossFeature() {
		this.dungeonManager = CaribouStonks.managers().getManager(DungeonManager.class);

		ChatEvents.MESSAGE_RECEIVED.register(this::onChatMessage);
		NetworkEvents.SERVER_TICK.register(this::onServerTick);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.DUNGEON
				&& dungeonManager.getBoss() == DungeonBoss.NECRON
				&& ConfigManager.getConfig().instance.theCatacombs.bossNecronPhase3TickTimers;
	}

	@Override
	protected void onClientJoinServer() {
		goldorTicks = -1;
		goldorStartTime = -1;
	}

	@Override
	protected void onClientTick() {
		if (goldorTicks >= 0 && isEnabled()) {
			String seconds = StonksUtils.DECIMAL_FORMAT.format(goldorTicks / 20f) + "s";
			Text message = Text.literal(seconds).formatted(getColor(goldorTicks));
			Client.showSubtitle(message, 0, 10, 0);
		}
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onChatMessage(@NotNull Text text) {
		if (!isEnabled()) return;

		String message = StonksUtils.stripColor(text.getString());
		switch (message) {
			case STORM_DEATH_MESSAGE -> goldorStartTime = GOLDOR_START_TIME;
			case GOLDOR_TRIGGER_MESSAGE -> goldorTicks = GOLDOR_TICKS;
			case GOLDOR_DEATH_MESSAGE -> {
				goldorTicks = -1;
				goldorStartTime = -1;
			}
			default -> {
			}
		}

	}

	@EventHandler(event = "NetworkEvents.SERVER_TICK")
	private void onServerTick() {
		if (goldorTicks >= 0) goldorTicks--; // tick tick
		if (goldorStartTime >= 0) goldorStartTime--; // tick tick
		if (goldorTicks == 0 && goldorStartTime <= 0) goldorTicks = GOLDOR_TICKS; // restart tick tick
	}

	private Formatting getColor(int goldorTicks) {
		if (goldorTicks <= 20) return Formatting.RED;
		if (goldorTicks <= 40) return Formatting.YELLOW;
		return Formatting.GREEN;
	}
}
