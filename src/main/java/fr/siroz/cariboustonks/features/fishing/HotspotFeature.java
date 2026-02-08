package fr.siroz.cariboustonks.features.fishing;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.mixin.accessors.DustParticleOptionsAccessor;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class HotspotFeature extends Feature {

	private static final double DISTANCE_TO_HOTSPOT_IN_BLOCKS = 10;
	private static final Color BOBBER_IN = Colors.GREEN.withAlpha(0.5F);
	private static final Color BOBBER_OUT = Colors.RED.withAlpha(0.5F);

	private @Nullable Double hotspotRadius = null;
	private @Nullable Hotspot currentHotspot = null;
	private boolean bobberInHotspot = false;

	public HotspotFeature() {
		TickScheduler.getInstance().runRepeating(this::update, 2, TimeUnit.SECONDS);
		TickScheduler.getInstance().runRepeating(this::updateBobber, 500, TimeUnit.MILLISECONDS);
		RenderEvents.WORLD_RENDER_EVENT.register(this::render);
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

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void render(WorldRenderer renderer) {
		if (!isEnabled() || currentHotspot == null) return;

		if (hotspotRadius != null && hotspotRadius > 0D && hotspotRadius <= 16D) {
			renderer.submitThickCircle(
					currentHotspot.centerPos().subtract(0D, 2.5D, 0D), // 2
					hotspotRadius,
					1,
					32,
					bobberInHotspot ? BOBBER_IN : BOBBER_OUT,
					true
			);
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
	private boolean matchesSmoke(@NonNull ClientboundLevelParticlesPacket p) {
		return p.getCount() == 5 && p.getMaxSpeed() == 0f;
	}

	/**
	 * [STDOUT]: DUST:: color: -38476 scale:1.0 count: 0 speed: 1.0
	 */
	private boolean matchesDust(@NonNull ClientboundLevelParticlesPacket p, ParticleOptions params) {
		if (p.getCount() != 0 || p.getMaxSpeed() != 1f) return false;
		if (!(params instanceof DustParticleOptions effect)) return false;

		int color = ((DustParticleOptionsAccessor) effect).getColor();
		return color == -38476 && effect.getScale() == 1f;
	}

	private void handleParticle(Vec3 particlePos, ParticleType<?> particleType) {
		if (currentHotspot == null) return; // "false" ide warn

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

	private record Hotspot(ArmorStand entity, Vec3 centerPos, Optional<String> perk) {
	}
}
