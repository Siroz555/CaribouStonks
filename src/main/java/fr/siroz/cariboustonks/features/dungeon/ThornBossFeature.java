package fr.siroz.cariboustonks.features.dungeon;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonBoss;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonManager;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.WorldEvents;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ThornBossFeature extends Feature {
	private static final BlockPos SEA_LANTERN_TARGET = new BlockPos(7, 77, 34);
	private static final int MAX_KILLS = 30;
	private static final int SPIRIT_BEAR_SPAWN_DELAY = 69; // 68-70

	private final DungeonManager dungeonManager;

	private final Set<BlockPos> blockPositions = new HashSet<>();
	private int spawnTicks = -1;
	private int kills = 0;

	public ThornBossFeature() {
		this.dungeonManager = CaribouStonks.skyBlock().getDungeonManager();
		this.createBlockPositions();

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
		if (!isEnabled()) return;

		MutableComponent message = Component.empty();
		if (spawnTicks < 0) {
			message.append(Component.literal("" + kills).withStyle(ChatFormatting.YELLOW));
			message.append(Component.literal("/").withStyle(ChatFormatting.GRAY));
			message.append(Component.literal("" + MAX_KILLS).withStyle(ChatFormatting.GREEN));
		} else if (spawnTicks > 0) {
			String seconds = StonksUtils.DECIMAL_FORMAT.format(spawnTicks / 20f) + "s";
			message.append(Component.literal(seconds).withStyle(spawnTicks <= 20 ? ChatFormatting.RED : ChatFormatting.YELLOW));
		} else {
			message.append(Component.literal("Alive!").withStyle(ChatFormatting.GREEN));
		}

		PlayerContext.showSubtitle(message, 0, 5, 0);
	}

	@Override
	protected void onClientJoinServer() {
		spawnTicks = -1;
		kills = 0;
	}

	@EventHandler(event = "WorldEvents.BLOCK_STATE_UPDATE_EVENT")
	private void onBlockUpdate(@NonNull BlockPos pos, @Nullable BlockState oldState, @NonNull BlockState newState) {
		if (!isEnabled() || oldState == null || !blockPositions.contains(pos)) return;

		if (newState.getBlock().equals(Blocks.SEA_LANTERN) && oldState.getBlock().equals(Blocks.COAL_BLOCK)) {
			if (kills < MAX_KILLS) kills++;
			if (pos.equals(SEA_LANTERN_TARGET)) spawnTicks = SPIRIT_BEAR_SPAWN_DELAY;
		}

		if (newState.getBlock().equals(Blocks.COAL_BLOCK) && oldState.getBlock().equals(Blocks.SEA_LANTERN)) {
			if (kills > 0) kills--;
			if (pos.equals(SEA_LANTERN_TARGET)) spawnTicks = -1;
		}
	}

	@EventHandler(event = "NetworkEvents.SERVER_TICK")
	private void onServerTick() {
		if (spawnTicks > 0) {
			spawnTicks--;
		}
	}

	private void createBlockPositions() {
		blockPositions.add(new BlockPos(-2, 77, 33));
		blockPositions.add(new BlockPos(-7, 77, 32));
		blockPositions.add(new BlockPos(-13, 77, 28));
		blockPositions.add(new BlockPos(-17, 77, 24));
		blockPositions.add(new BlockPos(-21, 77, 18));
		blockPositions.add(new BlockPos(-23, 77, 13));
		blockPositions.add(new BlockPos(-24, 77, 7));
		blockPositions.add(new BlockPos(-24, 77, 2));
		blockPositions.add(new BlockPos(-23, 77, -4));
		blockPositions.add(new BlockPos(-21, 77, -9));
		blockPositions.add(new BlockPos(-17, 77, -14));
		blockPositions.add(new BlockPos(-12, 77, -19));
		blockPositions.add(new BlockPos(-6, 77, -22));
		blockPositions.add(new BlockPos(-1, 77, -23));
		blockPositions.add(new BlockPos(5, 77, -24));
		blockPositions.add(new BlockPos(10, 77, -24));
		blockPositions.add(new BlockPos(16, 77, -22));
		blockPositions.add(new BlockPos(21, 77, -19));
		blockPositions.add(new BlockPos(27, 77, -15));
		blockPositions.add(new BlockPos(30, 77, -10));
		blockPositions.add(new BlockPos(32, 77, -5));
		blockPositions.add(new BlockPos(34, 77, 1));
		blockPositions.add(new BlockPos(34, 77, 7));
		blockPositions.add(new BlockPos(33, 77, 12));
		blockPositions.add(new BlockPos(31, 77, 18));
		blockPositions.add(new BlockPos(28, 77, 23));
		blockPositions.add(new BlockPos(23, 77, 28));
		blockPositions.add(new BlockPos(18, 77, 31));
		blockPositions.add(new BlockPos(12, 77, 33));
		blockPositions.add(new BlockPos(7, 77, 34));
	}
}
