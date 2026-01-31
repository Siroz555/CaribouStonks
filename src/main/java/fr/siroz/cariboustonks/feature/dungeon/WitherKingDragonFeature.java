package fr.siroz.cariboustonks.feature.dungeon;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.skyblock.IslandType;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.skyblock.dungeon.DungeonBoss;
import fr.siroz.cariboustonks.skyblock.dungeon.DungeonManager;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.Ticks;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This feature controls various aspects in phase 5 of the Master Mode Catacombs Floor 7.
 * The same features as in version 1.8, such as the unique DragPrio.
 */
public class WitherKingDragonFeature extends Feature {

	private static final String PHASE_5_TRIGGER_1 = "[BOSS] Wither King: You... again?";
	private static final String PHASE_5_TRIGGER_2 = "[BOSS] Wither King: Ohhh?";

	private final DungeonManager dungeonManager;

	private boolean isPhase5 = false;
	private final List<WitherKingDragon> dragons = new ArrayList<>();
	private int totalSpawned = 0;
	@Nullable
	private WitherKingDragon target = null;

	public WitherKingDragonFeature() {
		this.dungeonManager = CaribouStonks.skyBlock().getDungeonManager();

		ChatEvents.MESSAGE_RECEIVED.register(this::onMessage);
		NetworkEvents.SERVER_TICK.register(this::onServerTick);
		RenderEvents.WORLD_RENDER.register(this::render);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticle);
		SkyBlockEvents.DUNGEON_START.register(this::reset);
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

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onMessage(@NotNull Component text) {
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
			if (dragon.getSpawnTime() >= 0) {
				dragon.tick();
			}
		}
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	public void render(WorldRenderer renderer) {
		if (!isPhase5) return;
		if (!isEnabled()) return;

		boolean canShowBoundingBox = ConfigManager.getConfig().instance.theCatacombs.witherKing.showDragBoundingBox;
		boolean canShowLastBreathTarget = ConfigManager.getConfig().instance.theCatacombs.witherKing.showLastBreathTarget;
		boolean canShowSpawnTime = ConfigManager.getConfig().instance.theCatacombs.witherKing.showSpawnTime;

		for (WitherKingDragon dragon : WitherKingDragon.VALUES) {
			if (canShowBoundingBox) {
				renderer.submitOutline(dragon.getBox(), dragon.getColor(), 2f, true);
			}

			if (canShowLastBreathTarget) {
				renderer.submitCircle(dragon.getLbPos().getCenter(), 1.5d, 16, 0.02f, dragon.getColor(), Direction.Axis.Y, false);
			}

			if (target != null && target.getName().equals(dragon.getName())) { // .equals avec un spawn time different pas confiance
				renderer.submitLineFromCursor(target.getText().getCenter(), target.getColor(), 1f);
			}

			if (canShowSpawnTime && dragon.getSpawnTime() > 0) {
				int timeUntilSpawn = dragon.getSpawnTime() * Ticks.MILLISECONDS_PER_TICK;
				Component spawnText = Component.literal(timeUntilSpawn + " ms").withStyle(colorFor(timeUntilSpawn));
				renderer.submitText(spawnText, dragon.getText().getCenter(), 8.5f, true);
			}
		}
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticle(ClientboundLevelParticlesPacket packet) {
		if (!isPhase5) return;
		if (!isEnabled()) return;
		if (!packet.getParticle().getType().equals(ParticleTypes.ENCHANT)) return;

		for (WitherKingDragon dragon : WitherKingDragon.VALUES) {
			// Les box sont alignés au niveau du sol des chests, le Y est ajusté pour detect qu'en hauteur.
			// Réduit la box si les positions correspondent au Purple.
			// Cette approche est différentes des versions classiques de la 1.8, car il faut gérer les state.
			AABB box = AABB.encapsulatingFullBlocks(dragon.getPos1().offset(0, 14, 0), dragon.getPos2());
			box.deflate(dragon.getPos1().getX() == 41 ? 11 : 0, 0, dragon.getPos1().getZ() == 112 ? 0 : 11);
			// Si dans le box
			if (box.contains(packet.getX(), packet.getY(), packet.getZ())) {
				if (dragon.getSpawnTime() <= 0 && !dragon.isSpawned()) {
					dragon.spawn();
					onDragonSpawn(dragon);
				}
			}
		}
	}

	private void onDragonSpawn(WitherKingDragon dragon) {
		if (!ConfigManager.getConfig().instance.theCatacombs.witherKing.dragPrio) {
			return;
		}

		try {
			totalSpawned++;
			if (totalSpawned <= 2) {
				dragons.add(dragon);
				if (totalSpawned == 2 && dragons.size() == 2) {
					WitherKingDragon targetDragon = getTargetPriority(dragons.getFirst(), dragons.getLast());
					announceSpawn(targetDragon, true);
				}
			} else {
				announceSpawn(dragon, false);
			}
		} catch (Exception ignored) { // Tellement useless mais si c'est le crash :/
		}
	}

	private void announceSpawn(@NotNull WitherKingDragon dragon, boolean split) {
		String dragonName = dragon.getName().toUpperCase(Locale.ENGLISH);
		int color = dragon.getColor().asInt();

		if (ConfigManager.getConfig().instance.theCatacombs.witherKing.showDragTargetLine) {
			target = dragon;
			TickScheduler.getInstance().runLater(() -> target = null, 3, TimeUnit.SECONDS);
		}

		if (ConfigManager.getConfig().instance.theCatacombs.witherKing.dragPrioTitle) {
			Client.showTitleAndSubtitle(
					Component.literal(dragonName).withColor(color).withStyle(ChatFormatting.BOLD),
					Component.literal("is Spawning!").withColor(color),
					0, 30, 0
			);
			Client.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 5f, 1f);
		}

		if (ConfigManager.getConfig().instance.theCatacombs.witherKing.dragPrioMessage) {
			if (split) {
				Client.sendMessageWithPrefix(Component.literal(dragonName + " is Spawning! (Split Prio)").withColor(color));
			} else {
				Client.sendMessageWithPrefix(Component.literal(dragonName + " is Spawning!").withColor(color));
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
		dragons.clear();
		totalSpawned = 0;
		target = null;
		WitherKingDragon.resetAll();
	}
}
