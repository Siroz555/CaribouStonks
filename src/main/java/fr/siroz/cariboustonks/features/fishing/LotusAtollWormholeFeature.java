package fr.siroz.cariboustonks.features.fishing;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.position.Position;
import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.module.waypoint.options.TextOption;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.platform.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.MinecraftUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class LotusAtollWormholeFeature extends Feature {
	private static final int CLUSTER_THRESHOLD = 4; // Distance max (blocks) pour rattacher une particule à un cluster
	private static final double CLUSTER_THRESHOLD_SQ = CLUSTER_THRESHOLD * CLUSTER_THRESHOLD;
	private static final double VANISH_CLUSTER_THRESHOLD = 8; // Distance max (blocks) pour mettre en vanish un cluster
	private static final double VANISH_CLUSTER_THRESHOLD_SQ = VANISH_CLUSTER_THRESHOLD * VANISH_CLUSTER_THRESHOLD;
	private static final int MAX_PARTICLES = 64; // Cap pour la somme cumulative | 64 -> 128 ?
	private static final long STALE_TIMEOUT_MS = 2000; // Délai avant de remove
	private static final int MIN_PARTICLES_TO_SHOW = 12; // Min de particules avant d'afficher le waypoint

	private final List<WormholeCluster> clusters = new ArrayList<>();

	public LotusAtollWormholeFeature() {
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
		RenderEvents.WORLD_RENDER_EVENT.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.LOTUS_ATOLL
				&& this.config().fishing.lotusAtoll.wormholeFinder;
	}

	@Override
	protected void onClientJoinServer() {
		if (!clusters.isEmpty()) {
			clusters.forEach(c -> c.waypoint.setEnabled(false));
			clusters.clear();
		}
	}

	@Override
	protected void onSecondPassed() {
		if (!isEnabled()) return;
		if (clusters.isEmpty()) return;

		for (WormholeCluster cluster : clusters) {
			if (cluster.waypoint.isEnabled()) {
				double distance = MinecraftUtils.squaredDistanceToIgnoringY(cluster.getExactCenter(), PlayerContext.position());
				cluster.canBeRender = distance > VANISH_CLUSTER_THRESHOLD_SQ;
			}
		}
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ClientboundLevelParticlesPacket packet) {
		if (!isEnabled()) return;

		boolean isBlackHoleParticle = ParticleTypes.PORTAL.equals(packet.getParticle().getType())
				&& packet.getCount() == 5
				&& packet.getMaxSpeed() == 0.25f;
		if (isBlackHoleParticle) {
			try {
				handleParticle(packet.getX(), packet.getY(), packet.getZ());
			} catch (Exception _) {
			}
		}
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void render(WorldRenderer renderer) {
		if (!isEnabled()) return;
		if (clusters.isEmpty()) return;

		// Nettoyage des clusters
		clusters.removeIf(cluster -> {
			if (cluster.isStale()) {
				cluster.waypoint.setEnabled(false);
				return true;
			}
			return false;
		});

		if (clusters.isEmpty()) return;

		for (WormholeCluster c : clusters) {
			if (c.waypoint.isEnabled() && c.canBeRender) {
				c.waypoint.getRenderer().render(renderer);
			}
		}
	}

	private void handleParticle(double x, double y, double z) {
		// Chercher le cluster le plus proche dans le seuil
		WormholeCluster nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (WormholeCluster cluster : clusters) {
			double distance = cluster.distanceToSq(x, y, z);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearest = cluster;
			}
		}

		if (nearest != null && nearestDistance <= CLUSTER_THRESHOLD_SQ) {
			nearest.addParticle(x, y, z);
		} else {
			// Nouveau
			clusters.add(new WormholeCluster(createWaypoint(), x, y, z));
		}
	}

	private Waypoint createWaypoint() {
		return Waypoint.builder(builder -> {
			builder.enabled(false);
			builder.color(Color.fromTextColor(TextColor.LIGHT_PURPLE));
			builder.type(Waypoint.Type.BEAM);
			builder.textOption(TextOption.builder()
					.withText(Component.literal("Wormhole").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
					.scaleAdjustment(7f)
					.withThroughBlocks(true)
					.build());
		});
	}

	private static class WormholeCluster {
		final Waypoint waypoint;
		boolean canBeRender;
		private double sumX;
		private double sumY;
		private double sumZ;
		private int count;
		private double centerX;
		private double centerY;
		private double centerZ;
		private long lastUpdateTime;

		WormholeCluster(Waypoint waypoint, double x, double y, double z) {
			this.waypoint = waypoint;
			this.canBeRender = true;
			this.addParticle(x, y, z);
		}

		public @NonNull Vec3 getExactCenter() {
			// ntm, j'ai mis 1h pour ces valeurs
			return new Vec3(centerX + 0.15, centerY, centerZ /*- 0.05*/);
		}

		private void addParticle(double x, double y, double z) {
			// Somme cumulative max, après le centre est stable
			if (count < MAX_PARTICLES) {
				sumX += x;
				sumY += y;
				sumZ += z;
				count++;
				// Une seule fois le calcul au lieu de le faire dans le distanceToSq
				centerX = sumX / count;
				centerY = sumY / count;
				centerZ = sumZ / count;
			}
			lastUpdateTime = System.currentTimeMillis();

			if (count >= MIN_PARTICLES_TO_SHOW) {
				waypoint.updatePosition(Position.of(
						(int) Math.round(sumX / count),
						(int) Math.round(sumY / count),
						(int) Math.round(sumZ / count)
				));
				waypoint.setEnabled(true);
			}
		}

		private double distanceToSq(double x, double y, double z) {
			if (count == 0) return Double.MAX_VALUE;
			double dx = centerX - x;
			double dy = centerY - y;
			double dz = centerZ - z;
			return dx * dx + dy * dy + dz * dz;
		}

		private boolean isStale() {
			return System.currentTimeMillis() - lastUpdateTime > STALE_TIMEOUT_MS;
		}
	}
}
