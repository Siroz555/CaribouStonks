package fr.siroz.cariboustonks.features.dungeon;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonBoss;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonManager;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.events.WorldEvents;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.platform.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.Ticks;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This feature controls various aspects in phase 5 of the Master Mode Catacombs Floor 7.
 * The same features as in version 1.8, such as the unique DragPrio.
 */
public class WitherKingDragonFeature extends Feature {
	private static final String PHASE_5_TRIGGER_1 = "[BOSS] Wither King: You... again?";
	private static final String PHASE_5_TRIGGER_2 = "[BOSS] Wither King: Ohhh?";

	private final DungeonManager dungeonManager;

	private boolean isPhase5 = false;
	@Nullable
	private WitherKingDragon targetDragon = null;

	public WitherKingDragonFeature() {
		this.dungeonManager = CaribouStonks.skyBlock().getDungeonManager();

		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onMessage);
		NetworkEvents.SERVER_TICK.register(this::onServerTick);
		RenderEvents.WORLD_RENDER_EVENT.register(this::render);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticle);
		SkyBlockEvents.DUNGEON_START_EVENT.register(this::reset);
		WorldEvents.BLOCK_STATE_UPDATE_EVENT.register(this::onBlockStateUpdate);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.DUNGEON
				&& dungeonManager.getBoss() == DungeonBoss.NECRON;
	}

	@Override
	protected void onClientJoinServer() {
		reset();
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onMessage(@NonNull Component text) {
		if (!isEnabled()) return;

		String message = StonksUtils.stripColor(text.getString());
		if (message.equals(PHASE_5_TRIGGER_1) || message.equals(PHASE_5_TRIGGER_2)) {
			isPhase5 = true;
		}
	}

	@EventHandler(event = "NetworkEvents.SERVER_TICK")
	private void onServerTick() {
		if (!isPhase5) return;
		if (!isEnabled()) return;

		for (WitherKingDragon dragon : WitherKingDragon.VALUES) {
			if (dragon.getTimeToSpawn() > 0) dragon.tick();
			else if (dragon.getState() == WitherKingDragon.State.SPAWNING) dragon.setAlive();
		}
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void render(WorldRenderer renderer) {
		if (!isPhase5) return;
		if (!isEnabled()) return;

		boolean canShowBoundingBox = this.config().instance.theCatacombs.witherKing.showDragBoundingBox;
		boolean canShowLastBreathTarget = this.config().instance.theCatacombs.witherKing.showLastBreathTarget;
		boolean canShowTargetLine = this.config().instance.theCatacombs.witherKing.showDragTargetLine;
		boolean canShowSpawnTime = this.config().instance.theCatacombs.witherKing.showSpawnTime;
		if (!canShowBoundingBox && !canShowLastBreathTarget && !canShowTargetLine && !canShowSpawnTime) return;

		for (WitherKingDragon dragon : WitherKingDragon.VALUES) {
			if (canShowBoundingBox && dragon.getTimeToSpawn() > 0) {
				renderer.submitOutline(dragon.getBox(), dragon.getColor(), 2f, true);
			}

			if (canShowLastBreathTarget && dragon.getTimeToSpawn() > 0) {
				renderer.submitCircle(Vec3.atCenterOf(dragon.getLbPos()), 1.5d, 16, 0.02f, dragon.getColor(), Direction.Axis.Y, false);
			}

			if (canShowTargetLine && targetDragon != null && targetDragon.getName().equals(dragon.getName()) && targetDragon.getTimeToSpawn() > 0) {
				renderer.submitLineFromCursor(Vec3.atCenterOf(targetDragon.getText()), targetDragon.getColor(), 1f);
			}

			if (canShowSpawnTime && dragon.getTimeToSpawn() > 0) {
				int timeUntilSpawn = dragon.getTimeToSpawn() * Ticks.MILLISECONDS_PER_TICK;
				Component spawnText = Component.literal(timeUntilSpawn + " ms").withStyle(colorFor(timeUntilSpawn));
				renderer.submitText(spawnText, Vec3.atCenterOf(dragon.getText()), 8.5f, true);
			}
		}
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticle(ClientboundLevelParticlesPacket particle) {
		if (!isPhase5) return;
		if (!isEnabled()) return;
		if (!particle.getParticle().getType().equals(ParticleTypes.FLAME)
				|| particle.getCount() != 20 || particle.getMaxSpeed() != 0f
				|| particle.getY() != 19.0
				|| particle.getXDist() != 2f || particle.getYDist() != 3f || particle.getZDist() != 2f
				|| particle.getX() % 1 != 0.0 || particle.getZ() % 1 != 0.0
		) {
			return;
		}

		try {
			int spawned = 0;
			List<WitherKingDragon> dragons = new ArrayList<>();

			for (WitherKingDragon dragon : WitherKingDragon.VALUES) {
				spawned += dragon.getTimesSpawned();

				if (dragon.getState() == WitherKingDragon.State.SPAWNING) {
					if (!dragons.contains(dragon)) dragons.add(dragon);
					continue;
				}

				if (!dragon.isInXRange(particle.getX()) || !dragon.isInZRange(particle.getZ())) {
					continue;
				}

				announceSpawn(dragon, false);

				dragon.setState(WitherKingDragon.State.SPAWNING);
				dragon.setTimeToSpawn(WitherKingDragon.SPAWN_COOLDOWN_TICKS);
				dragons.add(dragon);
			}

			if (!dragons.isEmpty() && (dragons.size() == 2 || spawned >= 2) && targetDragon == null) {
				targetDragon = getTargetPriority(dragons.getFirst(), dragons.getLast());
				announceSpawn(targetDragon, true);
			}
		} catch (Exception _) {
		}
	}

	@EventHandler(event = "WorldEvents.BLOCK_STATE_UPDATE_EVENT")
	private void onBlockStateUpdate(@NonNull BlockPos pos, @Nullable BlockState oldState, @NonNull BlockState newState) {
		if (!isEnabled()) return;
		if (!isPhase5) return;
		if (!newState.isAir()) return;

		for (WitherKingDragon dragon : WitherKingDragon.VALUES) {
			if (dragon.getStatuePos().equals(pos)) {
				dragon.setState(WitherKingDragon.State.DEAD);
				if (targetDragon != null && targetDragon.getName().equals(dragon.getName())) {
					targetDragon = null;
				}
			}
		}
	}

	private void announceSpawn(@NonNull WitherKingDragon dragon, boolean split) {
		if (!this.config().instance.theCatacombs.witherKing.dragPrio) return;

		String dragonName = dragon.getName().toUpperCase(Locale.ENGLISH);
		int color = dragon.getColor().asInt();

		if (this.config().instance.theCatacombs.witherKing.dragPrioTitle) {
			PlayerContext.showTitleAndSubtitle(
					Component.literal(dragonName).withColor(color).withStyle(ChatFormatting.BOLD),
					Component.literal("is Spawning!").withColor(color),
					0, 30, 0
			);
			PlayerContext.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 5f, 1f);
		}

		if (this.config().instance.theCatacombs.witherKing.dragPrioMessage) {
			if (split) {
				PlayerContext.sendMessageWithPrefix(Component.literal(dragonName + " is Spawning! (Split Prio)").withColor(color));
			} else {
				PlayerContext.sendMessageWithPrefix(Component.literal(dragonName + " is Spawning!").withColor(color));
			}
		}
	}

	private WitherKingDragon getTargetPriority(WitherKingDragon first, WitherKingDragon second) {
		if (isArcherTeam()) return first.getArchPriority() > second.getArchPriority() ? first : second;
		else return first.getBersPriority() > second.getBersPriority() ? first : second;
	}

	private boolean isArcherTeam() {
		return switch (dungeonManager.getDungeonClass()) {
			case ARCHER, TANK -> true;
			default -> false;
		};
	}

	private ChatFormatting colorFor(long ms) {
		if (ms <= 1_000L) return ChatFormatting.RED;
		if (ms <= 2_000L) return ChatFormatting.GOLD;
		if (ms <= 3_000L) return ChatFormatting.YELLOW;
		return ChatFormatting.GREEN;
	}

	private void reset() {
		isPhase5 = false;
		targetDragon = null;
		WitherKingDragon.resetAll();
	}
}
