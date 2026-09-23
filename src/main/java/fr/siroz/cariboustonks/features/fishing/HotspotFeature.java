package fr.siroz.cariboustonks.features.fishing;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.feature.FeatureManager;
import fr.siroz.cariboustonks.core.infrastructure.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.position.Position;
import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.module.waypoint.options.TextOption;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.events.WorldEvents;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.platform.context.WorldContext;
import fr.siroz.cariboustonks.platform.mixin.accessors.DustParticleOptionsAccessor;
import fr.siroz.cariboustonks.platform.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.MinecraftUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class HotspotFeature extends Feature {
	private static final double DISTANCE_TO_HOTSPOT = 30;
	private static final double DISTANCE_TO_HOTSPOT_SQ = DISTANCE_TO_HOTSPOT * DISTANCE_TO_HOTSPOT;
	private static final int HOTSPOT_THRESHOLD = 5; // Distance max (blocks) pour rattacher une particule
	private static final double HOTSPOT_THRESHOLD_SQ = HOTSPOT_THRESHOLD * HOTSPOT_THRESHOLD;
	private static final long HOTSPOT_MAX_LIFETIME_MS = TimeUnit.MINUTES.toMillis(4);
	private static final Color BOBBER_IN = Colors.GREEN.withAlpha(0.5F);
	private static final Color BOBBER_OUT = Colors.RED.withAlpha(0.5F);

	private @Nullable HotspotRadarFeature hotspotRadarFeature;
	private final Cache<UUID, Boolean> notified = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(4))
			.build();

	private final Map<Integer, Hotspot> hotspots = new HashMap<>();
	private @Nullable Hotspot renderedHotspot = null;
	private boolean bobberInHotspot = false;
	private boolean hasFishingRodInHotbar = false;

	public HotspotFeature() {
		TickScheduler.getInstance().runRepeating(this::updateBobber, 500, TimeUnit.MILLISECONDS);
		RenderEvents.WORLD_RENDER_EVENT.register(this::onWorldRender);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
		WorldEvents.ARMORSTAND_REMOVE_EVENT.register(this::onArmorStandRemoved);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& this.config().fishing.hotspotHighlight
				&& SkyBlockAPI.getIsland().hasTrait(IslandType.Trait.HOTSPOT_FISHING);
	}

	@Override
	protected void postInitialize(@NonNull FeatureManager features) {
		hotspotRadarFeature = features.getFeature(HotspotRadarFeature.class);
	}

	@Override
	protected void onClientJoinServer() {
		reset();
	}

	@Override
	protected void onSecondPassed() {
		if (!isEnabled() || MINECRAFT.player == null || MINECRAFT.level == null) {
			reset();
			return;
		}

		updateFishingRodInHotbarState();

		if (hasFishingRodInHotbar) {
			refreshHotspots();
			updateRenderedHotspot();
			cleanupHotspots();
		} else if (renderedHotspot != null) {
			renderedHotspot = null;
			bobberInHotspot = false;
		} else {
			reset();
		}
	}

	private void onArmorStandRemoved(@NonNull ArmorStand armorStand) {
		if (!isEnabled()) return;
		if (!hasFishingRodInHotbar) return;
		if (hotspots.isEmpty()) return;

		Hotspot hotspotRemoved = hotspots.remove(armorStand.getId());
		if (hotspotRemoved != null) {
			onHotspotExpired(hotspotRemoved);
		}
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void onWorldRender(WorldRenderer renderer) {
		if (!isEnabled()) return;

		Hotspot hotspot = renderedHotspot;
		if (hotspot == null) return;

		Double radius = hotspot.radius;
		if (radius == null) return;

		double distanceSqToPlayer = MinecraftUtils.squaredDistanceToIgnoringY(hotspot.centerPos, PlayerContext.position());
		if (distanceSqToPlayer >= DISTANCE_TO_HOTSPOT_SQ) return;

		if (radius > 0D && radius <= 16D) {
			renderer.submitCircle(
					hotspot.centerPos.subtract(0D, 1.5D, 0D), // 2
					radius,
					32,
					0.025f,
					bobberInHotspot ? BOBBER_IN : BOBBER_OUT,
					Direction.Axis.Y,
					true
			);
		}
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ClientboundLevelParticlesPacket particle) {
		if (!isEnabled() || hotspots.isEmpty()) return;

		ParticleOptions params = particle.particle();
		ParticleType<?> type = params.getType();
		// Future: (Predicate<ParticleS2CPacket>, Consumer<ParticleS2CPacket>)
		// pour itérer dessus pour rendre l'ajout de nouveaux handlers trivial.

		Vec3 particlePos = new Vec3(particle.x(), particle.y(), particle.z());

		if (ParticleTypes.SMOKE.equals(type) && matchesSmoke(particle)) {
			assignRadius(particlePos, 0D);
			return;
		}

		if (ParticleTypes.DUST.equals(type) && matchesDust(particle, params)) {
			assignRadius(particlePos, 0.2D);
		}
	}

	/**
	 * [STDOUT]: particle: smoke count: 5 speed: 0.0
	 */
	private boolean matchesSmoke(@NonNull ClientboundLevelParticlesPacket p) {
		return p.count() == 5 && p.xMaxSpeed() == 0f;
	}

	/**
	 * [STDOUT]: DUST:: color: -38476 scale:1.0 count: 0 speed: 1.0
	 */
	private boolean matchesDust(@NonNull ClientboundLevelParticlesPacket p, ParticleOptions params) {
		if (p.count() != 0 || p.xMaxSpeed() != 1f) return false;
		if (!(params instanceof DustParticleOptions effect)) return false;

		int color = ((DustParticleOptionsAccessor) effect).getColor();
		return color == -38476 && effect.getScale() == 1f;
	}

	private void refreshHotspots() {
		List<ArmorStand> closestEntities = WorldContext.findClosestEntities(
				ArmorStand.class,
				DISTANCE_TO_HOTSPOT,
				Entity::hasCustomName
		);
		if (closestEntities.isEmpty()) return;

		List<ArmorStand> hotspotStands = closestEntities.stream()
				.filter(as -> "HOTSPOT".equals(as.getName().getString()))
				.toList();
		if (hotspotStands.isEmpty()) return;

		for (ArmorStand armorStand : hotspotStands) {
			if (hotspots.containsKey(armorStand.getId())) continue;

			Vec3 centerPos = Vec3.atCenterOf(armorStand.blockPosition());
			Optional<Component> perk = findPerk(closestEntities, armorStand);
			Hotspot hotspot = new Hotspot(
					armorStand,
					centerPos,
					perk.orElse(Component.literal("?").withStyle(ChatFormatting.RED)),
					System.currentTimeMillis()
			);

			hotspots.put(armorStand.getId(), hotspot);
			onHotspotFound(hotspot);
		}
	}

	private Optional<Component> findPerk(List<ArmorStand> closestEntities, ArmorStand hotspotStand) {
		return closestEntities.stream()
				.filter(e -> e.getX() == hotspotStand.getX()
						&& e.getY() < hotspotStand.getY()
						&& hotspotStand.getY() - e.getY() <= 1
						&& e.getZ() == hotspotStand.getZ()
						&& e.getXRot() == hotspotStand.getXRot())
				.map(Entity::getName)
				.findFirst();
	}

	private void updateFishingRodInHotbarState() {
		if (MINECRAFT.player == null) {
			hasFishingRodInHotbar = false;
			return;
		}

		for (int slot = 0; slot <= 8; slot++) {
			ItemStack item = MINECRAFT.player.getInventory().getItem(slot);
			if (item.is(Items.FISHING_ROD)) {
				hasFishingRodInHotbar = true;
				return;
			}
		}

		hasFishingRodInHotbar = false;
	}

	private void cleanupHotspots() {
		long now = System.currentTimeMillis();
		Iterator<Map.Entry<Integer, Hotspot>> it = hotspots.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<Integer, Hotspot> entry = it.next();
			ArmorStand entity = entry.getValue().hotspotStand;
			Hotspot hotspot = entry.getValue();
			if (!entity.isAlive() || (now - hotspot.foundAtMillis) > HOTSPOT_MAX_LIFETIME_MS) {
				it.remove();
				onHotspotExpired(hotspot);
			}
		}
	}

	private void updateRenderedHotspot() {
		if (MINECRAFT.player == null || hotspots.isEmpty()) {
			if (renderedHotspot != null) {
				renderedHotspot = null;
				bobberInHotspot = false;
			}
			return;
		}

		Vec3 playerPos = MINECRAFT.player.position();
		Hotspot closest = null;
		double closestDistSq = Double.MAX_VALUE;

		for (Hotspot hotspot : hotspots.values()) {
			double distSq = hotspot.centerPos.distanceToSqr(playerPos);
			if (distSq < closestDistSq) {
				closestDistSq = distSq;
				closest = hotspot;
			}
		}

		if (closest != renderedHotspot) {
			renderedHotspot = closest;
			if (hotspotRadarFeature != null) {
				hotspotRadarFeature.reset();
			}
		}
	}

	private void updateBobber() {
		if (!isEnabled() || MINECRAFT.player == null) {
			bobberInHotspot = false;
			return;
		}

		Hotspot hotspot = renderedHotspot;
		if (hotspot == null) {
			bobberInHotspot = false;
			return;
		}

		Double radius = hotspot.radius;
		if (radius == null) {
			bobberInHotspot = false;
			return;
		}

		FishingHook bobber = MINECRAFT.player.fishing;
		if (bobber != null && bobber.isAlive() && bobber.getOwner() == MINECRAFT.player) {
			Vec3 bobberPos = bobber.position();
			double distanceToIgnoringY = MinecraftUtils.squaredDistanceToIgnoringY(hotspot.centerPos, bobberPos);
			bobberInHotspot = distanceToIgnoringY <= radius * radius;
		} else {
			bobberInHotspot = false;
		}
	}

	private void assignRadius(Vec3 particlePos, double correction) {
		Hotspot target = null;
		double bestDistSq = HOTSPOT_THRESHOLD_SQ;

		for (Hotspot hotspot : hotspots.values()) {
			if (hotspot.radius != null) continue;

			double distSq = hotspot.centerPos.distanceToSqr(particlePos);
			if (distSq <= bestDistSq) {
				bestDistSq = distSq;
				target = hotspot;
			}
		}

		if (target != null) {
			target.radius = target.centerPos.distanceTo(particlePos) - correction;
		}
	}

	private void onHotspotFound(Hotspot hotspot) {
		UUID hotspotId = hotspot.hotspotStand.getUUID();

		Boolean removed = notified.getIfPresent(hotspotId);
		if (removed != null) return;
		notified.put(hotspotId, false);

		if (!this.config().fishing.hotspotHighlightFoundAnnouncer) return;

		Position position = Position.of(hotspot.hotspotStand.blockPosition());
		TextColor color = MinecraftUtils.findStyle(hotspot.perk).getColor();
		if (color == null) color = TextColor.WHITE;

		Waypoint.builder(position)
				.type(Waypoint.Type.BEAM)
				.color(Color.fromTextColor(color))
				.timeout(5, TimeUnit.SECONDS)
				.resetBetweenWorlds(true)
				.proximityReset(true)
				.textOption(TextOption.builder()
						.withText(hotspot.perk)
						.scaleAdjustment(5)
						.build())
				.buildAndRegister();

		Component message = Component.literal("HOTSPOT NEARBY! ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
		PlayerContext.sendMessage(Component.empty().append(message).append(hotspot.perk));

		String shareInfo = position.asChatCoordinates() + " | " + hotspot.perk.getString() + " Hotspot @" + StonksUtils.generateRandomId();
		Component shareMessage = Component.empty()
				.append(Component.literal("[Share to PARTY chat]").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD)
						.withStyle(style -> style
								.withClickEvent(new ClickEvent.RunCommand("/pc " + shareInfo))
								.withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to share to PARTY chat").withStyle(ChatFormatting.YELLOW)))))
				.append(Component.literal(" or ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal("[Share to ALL chat]").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
						.withStyle(style -> style
								.withClickEvent(new ClickEvent.RunCommand("/achat " + shareInfo))
								.withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to share to ALL chat").withStyle(ChatFormatting.YELLOW)))));
		PlayerContext.sendMessage(shareMessage);
	}

	private void onHotspotExpired(Hotspot hotspot) {
		if (renderedHotspot == hotspot) {
			renderedHotspot = null;
			bobberInHotspot = false;

			UUID hotspotId = hotspot.hotspotStand.getUUID();
			Boolean removed = notified.getIfPresent(hotspotId);
			if (removed == null || removed) return;
			notified.put(hotspotId, true);

			if (!this.config().fishing.hotspotHighlightGoneAnnouncer) return;

			Component base = Component.literal("HOTSPOT ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
			PlayerContext.sendMessage(Component.empty()
					.append(base)
					.append(hotspot.perk)
					.append(Component.literal(" is gone!").withStyle(ChatFormatting.RED))
			);
			PlayerContext.showSubtitle(Component.empty()
							.append(base)
							.append(Component.literal("GONE!").withStyle(ChatFormatting.RED)),
					0, 20, 0
			);
		}
	}

	private void reset() {
		hasFishingRodInHotbar = false;
		hotspots.clear();
		notified.invalidateAll();
		renderedHotspot = null;
		bobberInHotspot = false;
	}

	private static final class Hotspot {
		private final ArmorStand hotspotStand;
		private final Vec3 centerPos;
		private final Component perk;
		private final long foundAtMillis;
		private volatile @Nullable Double radius;

		private Hotspot(ArmorStand hotspotStand, Vec3 centerPos, Component perk, long foundAtMillis) {
			this.hotspotStand = hotspotStand;
			this.centerPos = centerPos;
			this.perk = perk;
			this.foundAtMillis = foundAtMillis;
		}
	}
}
