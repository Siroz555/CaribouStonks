package fr.siroz.cariboustonks.features.dungeon;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.dungeon.DungeonBoss;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.events.WorldEvents;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.platform.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.MinecraftUtil;
import fr.siroz.cariboustonks.util.Ticks;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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

public class WitherKingDragonFeature extends Feature {
	private static final String PHASE_5_TRIGGER_1 = "[BOSS] Wither King: You... again?";
	private static final String PHASE_5_TRIGGER_2 = "[BOSS] Wither King: Ohhh?";
	private static final int SPAWN_COOLDOWN_TICKS = Ticks.from(5, TimeUnit.SECONDS);

	private final Map<WitherKingDragon, DragonStatus> statuses = new EnumMap<>(WitherKingDragon.class);
	private boolean isPhase5 = false;

	public WitherKingDragonFeature() {
		for (WitherKingDragon dragon : WitherKingDragon.VALUES) statuses.put(dragon, new DragonStatus());

		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onMessage);
		NetworkEvents.SERVER_TICK.register(this::onServerTick);
		RenderEvents.WORLD_RENDER_EVENT.register(this::render);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticle);
		SkyBlockEvents.DUNGEON_START_EVENT.register(this::reset);
		WorldEvents.BLOCK_STATE_UPDATE_EVENT.register(this::onBlockStateUpdate);
	}

	@Override
	public boolean isEnabled() {
		return this.skyBlock().location().island() == IslandType.DUNGEON
				&& this.skyBlock().getDungeonManager().getBoss() == DungeonBoss.NECRON;
	}

	@Override
	protected void onClientJoinServer() {
		reset();
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onMessage(@NonNull Component text) {
		if (!isEnabled()) return;

		String message = MinecraftUtil.stripColor(text.getString());
		if (message.equals(PHASE_5_TRIGGER_1) || message.equals(PHASE_5_TRIGGER_2)) {
			isPhase5 = true;
		}
	}

	@EventHandler(event = "NetworkEvents.SERVER_TICK")
	private void onServerTick() {
		if (isActive()) for (DragonStatus status : statuses.values()) status.tick();
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void render(WorldRenderer renderer) {
		if (!isActive()) return;

		var config = this.config().instance.theCatacombs.witherKing;
		if (!config.showDragBoundingBox
				&& !config.showLastBreathTarget
				&& !config.showDragTargetLine
				&& !config.showSpawnTime
		) {
			return;
		}

		WitherKingDragon next = config.showDragTargetLine ? nextToSpawn() : null;

		for (WitherKingDragon dragon : WitherKingDragon.VALUES) {
			DragonStatus status = statuses.get(dragon);
			if (!status.isSpawning()) continue;

			if (config.showDragBoundingBox) {
				renderer.submitOutline(dragon.getBox(), dragon.getColor(), 2f, true);
			}

			if (config.showLastBreathTarget) {
				renderer.submitCircle(Vec3.atCenterOf(dragon.getLbPos()), 1.5d, 16, 0.02f, dragon.getColor(), Direction.Axis.Y, false);
			}

			if (dragon == next) {
				renderer.submitLineFromCursor(Vec3.atCenterOf(dragon.getSpawnPos()), dragon.getColor(), 1f);
			}

			if (config.showSpawnTime) {
				int timeUntilSpawn = status.getTicksUntilSpawn() * Ticks.MILLISECONDS_PER_TICK;
				Component spawnText = Component.literal(timeUntilSpawn + " ms").withColor(colorFor(timeUntilSpawn));
				renderer.submitText(spawnText, Vec3.atCenterOf(dragon.getSpawnPos()), 8.5f, true);
			}
		}
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticle(ClientboundLevelParticlesPacket particle) {
		if (!isActive()) return;
		if (!particle.particle().getType().equals(ParticleTypes.FLAME)
				|| particle.count() != 20 || particle.xMaxSpeed() != 0f
				|| particle.y() != 19.0
				|| particle.xDist() != 2f || particle.yDist() != 3f || particle.zDist() != 2f
				|| particle.x() % 1 != 0.0 || particle.z() % 1 != 0.0
		) {
			return;
		}

		WitherKingDragon dragon = WitherKingDragon.bySpawnArea(particle.x(), particle.z());
		if (dragon == null) return;

		DragonStatus status = statuses.get(dragon);
		if (status.isSpawning()) return;

		status.startSpawning();
		announceSpawn(dragon);
	}

	@EventHandler(event = "WorldEvents.BLOCK_STATE_UPDATE_EVENT")
	private void onBlockStateUpdate(@NonNull BlockPos pos, @Nullable BlockState oldState, @NonNull BlockState newState) {
		if (!isActive()) return;
		if (!newState.isAir()) return;

		for (WitherKingDragon dragon : WitherKingDragon.VALUES) {
			if (dragon.getStatuePos().equals(pos)) {
				statuses.get(dragon).markDead();
				return;
			}
		}
	}

	private void announceSpawn(@NonNull WitherKingDragon dragon) {
		String dragonName = dragon.getName().toUpperCase(Locale.ENGLISH);
		int color = dragon.getColor().asInt();

		if (this.config().instance.theCatacombs.witherKing.showSpawnTitle) {
			PlayerContext.showTitleAndSubtitle(
					Component.literal(dragonName).withColor(color).withStyle(ChatFormatting.BOLD),
					Component.literal("is Spawning!").withColor(color),
					0, 30, 0
			);
			PlayerContext.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 5f, 1f);
		}

		if (this.config().instance.theCatacombs.witherKing.showSpawnMessage) {
			PlayerContext.sendMessageWithPrefix(Component.empty()
					.append(Component.literal(dragonName).withColor(color).withStyle(ChatFormatting.BOLD))
					.append(Component.literal("is Spawning!").withColor(color))
			);
		}
	}

	private @Nullable WitherKingDragon nextToSpawn() {
		WitherKingDragon next = null;
		int minTicks = Integer.MAX_VALUE;

		for (WitherKingDragon dragon : WitherKingDragon.VALUES) {
			DragonStatus status = statuses.get(dragon);
			if (status.isSpawning() && status.getTicksUntilSpawn() < minTicks) {
				next = dragon;
				minTicks = status.getTicksUntilSpawn();
			}
		}
		return next;
	}

	private boolean isActive() {
		return isPhase5 && isEnabled();
	}

	private int colorFor(long ms) {
		if (ms <= 1_000L) return Colors.RED_RGB;
		if (ms <= 2_000L) return Colors.GOLD_RGB;
		if (ms <= 3_000L) return Colors.YELLOW_RGB;
		return Colors.GREEN_RGB;
	}

	private void reset() {
		isPhase5 = false;
		statuses.values().forEach(DragonStatus::reset);
	}

	private static final class DragonStatus {

		private enum State {
			DEAD,
			SPAWNING,
			ALIVE
		}

		private State state = State.DEAD;
		private int ticksUntilSpawn = 0;

		public boolean isSpawning() {
			return state == State.SPAWNING;
		}

		public int getTicksUntilSpawn() {
			return ticksUntilSpawn;
		}

		public void startSpawning() {
			state = State.SPAWNING;
			ticksUntilSpawn = SPAWN_COOLDOWN_TICKS;
		}

		public void tick() {
			if (state != State.SPAWNING) return;
			if (--ticksUntilSpawn <= 0) state = State.ALIVE;
		}

		public void markDead() {
			state = State.DEAD;
			ticksUntilSpawn = 0;
		}

		public void reset() {
			markDead();
		}
	}
}
