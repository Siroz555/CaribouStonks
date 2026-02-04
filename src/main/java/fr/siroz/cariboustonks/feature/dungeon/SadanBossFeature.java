package fr.siroz.cariboustonks.feature.dungeon;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonBoss;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SadanBossFeature extends Feature {

	private static final int TERRACOTTA_TICKS = 240;
	private static final String GIANT_TRIGGER_MESSAGE = "[BOSS] Sadan: ENOUGH";

	@Nullable
	private Set<Terracotta> terracottaFlowerPots = null;
	private boolean inBoss = false;

	public SadanBossFeature() {
		SkyBlockEvents.DUNGEON_BOSS_SPAWN.register(this::onDungeonBossSpawn);
		ChatEvents.MESSAGE_RECEIVED.register(this::onChatMessage);
		NetworkEvents.SERVER_TICK.register(this::onServerTick);
		WorldEvents.BLOCK_STATE_UPDATE.register(this::onBlockUpdate);
		RenderEvents.WORLD_RENDER.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.DUNGEON
				&& ConfigManager.getConfig().instance.theCatacombs.bossSadanTerracottaTimers;
	}

	@EventHandler(event = "SkyBlockEvents.DUNGEON_BOSS_SPAWN")
	private void onDungeonBossSpawn(@NotNull DungeonBoss boss) {
		if (boss == DungeonBoss.SADAN) {
			inBoss = true;
		}
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onChatMessage(@NotNull Component text) {
		if (inBoss && text.getString().equals(GIANT_TRIGGER_MESSAGE)) {
			terracottaFlowerPots = null;
		}
	}

	@EventHandler(event = "NetworkEvents.SERVER_TICK")
	private void onServerTick() {
		if (!inBoss || terracottaFlowerPots == null) return;

		Iterator<Terracotta> iterator = terracottaFlowerPots.iterator();
		while (iterator.hasNext()) {
			Terracotta terracotta = iterator.next();
			terracotta.tick();
			if (terracotta.getTicks() <= 0) {
				iterator.remove();
			}
		}
	}

	@EventHandler(event = "WorldEvents.BLOCK_STATE_UPDATE")
	private void onBlockUpdate(@NotNull BlockPos pos, @Nullable BlockState oldState, @NotNull BlockState newState) {
		if (!inBoss || !isEnabled()) return;

		// Le .equals(Blocks.FLOWER_POT) marche pas
		if (newState.getBlock() instanceof FlowerPotBlock) {
			if (terracottaFlowerPots == null) {
				terracottaFlowerPots = new HashSet<>();
			}
			terracottaFlowerPots.add(new Terracotta(pos, TERRACOTTA_TICKS));
		}
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	public void render(WorldRenderer renderer) {
		if (!inBoss || !isEnabled() || terracottaFlowerPots == null) return;

		for (Terracotta terracotta : terracottaFlowerPots) {
			if (terracotta.getPos() == null || terracotta.getTicks() <= 0) {
				continue;
			}

			Component message = getTimeFrom(terracotta.getTicks());
			renderer.submitText(message, terracotta.getPos().getCenter(), 1.5f, true);
		}
	}

	@Override
	protected void onClientJoinServer() {
		inBoss = false;
		terracottaFlowerPots = null;
	}

	private Component getTimeFrom(int ticks) {
		String seconds = StonksUtils.DECIMAL_FORMAT.format(ticks / 20f) + "s";
		return Component.literal(seconds).withStyle(ticks <= 20 ? ChatFormatting.RED : ChatFormatting.YELLOW);
	}

	private static class Terracotta {
		private final BlockPos pos;
		private int ticks;

		Terracotta(BlockPos pos, int ticks) {
			this.pos = pos;
			this.ticks = ticks;
		}

		public void tick() {
			ticks--;
		}

		public BlockPos getPos() {
			return pos;
		}

		public int getTicks() {
			return ticks;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) return false;
			Terracotta that = (Terracotta) o;
			return Objects.equals(pos, that.pos);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(pos);
		}
	}
}
