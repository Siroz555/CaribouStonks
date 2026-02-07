package fr.siroz.cariboustonks.feature.dungeon;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonBoss;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonManager;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ThornBossFeature extends Feature {

	private static final BlockPos SEA_LANTERN_TARGET = new BlockPos(7, 77, 34);
	private static final int SPIRIT_BEAR_SPAWN_DELAY = 70;

	private final DungeonManager dungeonManager;
	private int spawnTicks = 0;

	public ThornBossFeature() {
		this.dungeonManager = CaribouStonks.skyBlock().getDungeonManager();

		WorldEvents.BLOCK_STATE_UPDATE_EVENT.register(this::onBlockUpdate);
		NetworkEvents.SERVER_TICK.register(this::onServerTick);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.DUNGEON
				&& dungeonManager.getBoss() == DungeonBoss.THORN
				&& this.config().instance.theCatacombs.bossThornSpiritBearTimers;
	}

	@Override
	protected void onClientTick() {
		if (spawnTicks > 0 && isEnabled()) {
			String seconds = StonksUtils.DECIMAL_FORMAT.format(spawnTicks / 20f) + "s";
			Component message = Component.literal(seconds).withStyle(spawnTicks <= 20 ? ChatFormatting.RED : ChatFormatting.YELLOW);
			Client.showTitle(message, 0, 5, 0);
		}
	}

	@Override
	protected void onClientJoinServer() {
		spawnTicks = 0;
	}

	@EventHandler(event = "WorldEvents.BLOCK_STATE_UPDATE_EVENT")
	private void onBlockUpdate(@NonNull BlockPos pos, @Nullable BlockState oldState, @NonNull BlockState newState) {
		if (isEnabled() && pos.equals(SEA_LANTERN_TARGET) && newState.getBlock().equals(Blocks.SEA_LANTERN)) {
			spawnTicks = SPIRIT_BEAR_SPAWN_DELAY;
		}
	}

	@EventHandler(event = "NetworkEvents.SERVER_TICK")
	private void onServerTick() {
		if (spawnTicks > 0) {
			spawnTicks--;
		}
	}
}
