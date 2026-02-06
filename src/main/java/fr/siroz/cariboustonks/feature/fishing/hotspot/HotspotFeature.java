package fr.siroz.cariboustonks.feature.fishing.hotspot;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.feature.fishing.radar.HotspotRadarFeature;
import fr.siroz.cariboustonks.mixin.accessors.DustParticleOptionsAccessor;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HotspotFeature extends Feature {

	private static final double DISTANCE_TO_HOTSPOT_IN_BLOCKS = 10;

	private Double hotspotRadius = null;
	private Hotspot currentHotspot = null;
	private boolean bobberInHotspot = false;

	public HotspotFeature() {
		HotspotRenderer renderer = new HotspotRenderer(this);
		RenderEvents.WORLD_RENDER.register(renderer::render);

		TickScheduler.getInstance().runRepeating(this::update, 2, TimeUnit.SECONDS);
		TickScheduler.getInstance().runRepeating(this::updateBobber, 500, TimeUnit.MILLISECONDS);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& this.config().fishing.hotspotHighlight
				&& SkyBlockAPI.isOnIslands(IslandType.CRIMSON_ISLE, IslandType.BACKWATER_BAYOU);
	}

	@Override
	protected void onClientJoinServer() {
		reset();
	}

	@Nullable
	Hotspot getCurrentHotspot() {
		return currentHotspot;
	}

	@Nullable
	Double getHotspotRadius() {
		return hotspotRadius;
	}

	boolean isBobberInHotspot() {
		return bobberInHotspot;
	}

	private void update() {
		if (!isEnabled() || CLIENT.player == null || CLIENT.level == null) {
			return;
		}

		ItemStack item = Client.getHeldItem();
		if (item == null || item.isEmpty() || !item.is(Items.FISHING_ROD)) {
			reset();
			return;
		}

		if (currentHotspot == null) {
			currentHotspot = findClosestHotspotInRange(CLIENT.player).orElse(null);
			if (currentHotspot != null) {
				// dep -_-
				CaribouStonks.features().getFeature(HotspotRadarFeature.class).reset();
			}
		} else {
			Optional<Hotspot> newHotspot = findClosestHotspotInRange(CLIENT.player);
			if (newHotspot.isEmpty()) {
				reset();
			}
		}
	}

	private void updateBobber() {
		if (!isEnabled() || CLIENT.player == null) return;
		if (currentHotspot == null || hotspotRadius == null) return;

		FishingHook bobber = CLIENT.player.fishing;
		if (bobber != null && bobber.isAlive() && bobber.getOwner() == CLIENT.player) {
			Vec3 bobberPos = bobber.position();
			double distanceToIgnoringY = StonksUtils.squaredDistanceToIgnoringY(currentHotspot.centerPos(), bobberPos);
			bobberInHotspot = distanceToIgnoringY <= hotspotRadius * hotspotRadius;
		} else {
			bobberInHotspot = false;
		}
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ClientboundLevelParticlesPacket particle) {
		if (!isEnabled() || currentHotspot == null || hotspotRadius != null) {
			return;
		}

		ParticleOptions params = particle.getParticle();
		ParticleType<?> type = params.getType();
		// Future: (Predicate<ParticleS2CPacket>, Consumer<ParticleS2CPacket>)
		// pour itérer dessus pour rendre l'ajout de nouveaux handlers trivial.

		if (ParticleTypes.SMOKE.equals(type) && matchesSmoke(particle)) {
			handleParticle(new Vec3(particle.getX(), particle.getY(), particle.getZ()), ParticleTypes.SMOKE);
			return;
		}

		if (ParticleTypes.DUST.equals(type) && matchesDust(particle, params)) {
			handleParticle(new Vec3(particle.getX(), particle.getY(), particle.getZ()), ParticleTypes.DUST);
		}
	}

	/**
	 * [STDOUT]: particle: smoke count: 5 speed: 0.0
	 */
	private boolean matchesSmoke(@NotNull ClientboundLevelParticlesPacket p) {
		return p.getCount() == 5 && p.getMaxSpeed() == 0f;
	}

	/**
	 * [STDOUT]: DUST:: color: -38476 scale:1.0 count: 0 speed: 1.0
	 */
	private boolean matchesDust(@NotNull ClientboundLevelParticlesPacket p, ParticleOptions params) {
		if (p.getCount() != 0 || p.getMaxSpeed() != 1f) return false;
		if (!(params instanceof DustParticleOptions effect)) return false;

		int color = ((DustParticleOptionsAccessor) effect).getColor();
		return color == -38476 && effect.getScale() == 1f;
	}

	private void handleParticle(Vec3 particlePos, ParticleType<?> particleType) {
		hotspotRadius = currentHotspot.centerPos().distanceTo(particlePos);
		if (particleType == ParticleTypes.DUST) {
			hotspotRadius -= - 0.2D;
		}
	}

	private Optional<Hotspot> findClosestHotspotInRange(@Nullable Entity entity) {
		if (CLIENT.level == null || entity == null) {
			return Optional.empty();
		}

		List<ArmorStand> armorStands = CLIENT.level.getEntitiesOfClass(
				ArmorStand.class,
				entity.getBoundingBox().inflate(DISTANCE_TO_HOTSPOT_IN_BLOCKS),
				Entity::hasCustomName
		);

		ArmorStand closestHotspotArmorStand = armorStands.stream()
				.filter(as -> "HOTSPOT".equals(as.getName().getString()))
				.min(Comparator.comparingDouble(as -> as.distanceToSqr(entity)))
				.orElse(null);

		if (closestHotspotArmorStand == null) {
			return Optional.empty();
		}

		Optional<String> perk = armorStands.stream()
				.filter(e -> e.getX() == closestHotspotArmorStand.getX()
						&& e.getY() < closestHotspotArmorStand.getY()
						&& closestHotspotArmorStand.getY() - e.getY() <= 1
						&& e.getZ() == closestHotspotArmorStand.getZ()
						&& e.getXRot() == closestHotspotArmorStand.getXRot())
				.map(e -> e.getName().getString())
				.findFirst();

		Vec3 centerPos = closestHotspotArmorStand.blockPosition().getCenter();

		return Optional.of(new Hotspot(closestHotspotArmorStand, centerPos, perk));
	}

	private void reset() {
		currentHotspot = null;
		hotspotRadius = null;
		bobberInHotspot = false;
	}
}
