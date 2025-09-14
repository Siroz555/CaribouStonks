package fr.siroz.cariboustonks.feature.fishing;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.feature.fishing.radar.HotspotRadarFeature;
import fr.siroz.cariboustonks.util.PositionUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HotspotFeature extends Feature {

	private static final double DISTANCE_TO_HOTSPOT_IN_BLOCKS = 10;

	private final HotspotRenderer renderer;

	private Double hotspotRadius = null;
	private Hotspot currentHotspot = null;
	private boolean bobberInHotspot = false;

	public HotspotFeature() {
		this.renderer = new HotspotRenderer(this);

		WorldEvents.JOIN.register(world -> reset());
		TickScheduler.getInstance().runRepeating(this::update, 2, TimeUnit.SECONDS);
		TickScheduler.getInstance().runRepeating(this::updateBobber, 500, TimeUnit.MILLISECONDS);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& ConfigManager.getConfig().fishing.hotspotHighlight
				&& SkyBlockAPI.isOnIslands(IslandType.CRIMSON_ISLE, IslandType.BACKWATER_BAYOU);
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
		if (!isEnabled() || CLIENT.player == null || CLIENT.world == null) {
			return;
		}

		ItemStack item = CLIENT.player.getInventory().getSelectedStack();
		if (item == null || item.isEmpty() || !item.isOf(Items.FISHING_ROD)) {
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

		FishingBobberEntity bobber = CLIENT.player.fishHook;
		if (bobber != null && bobber.isAlive() && bobber.getOwner() == CLIENT.player) {
			Vec3d bobberPos = bobber.getPos();
			//bobberInHotspot = currentHotspot.centerPos().squaredDistanceTo(bobberPos) <= hotspotRadius * hotspotRadius;
			double distanceToIgnoringY = PositionUtils.squaredDistanceToIgnoringY(currentHotspot.centerPos(), bobberPos);
			bobberInHotspot = distanceToIgnoringY <= hotspotRadius * hotspotRadius;
		} else {
			bobberInHotspot = false;
		}
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ParticleS2CPacket particle) {
		if (!isEnabled() || currentHotspot == null || hotspotRadius != null) {
			return;
		}

		// [STDOUT]: particle: smoke count: 5 speed: 0.0
		if (ParticleTypes.SMOKE.equals(particle.getParameters().getType())
				&& particle.getCount() == 5
				&& particle.getSpeed() == 0f
		) {
			Vec3d particlePos = new Vec3d(particle.getX(), particle.getY(), particle.getZ());
			hotspotRadius = currentHotspot.centerPos().distanceTo(particlePos);
		}
	}

	@EventHandler(event = "WorldRenderEvents.AFTER_TRANSLUCENT")
	private void render(WorldRenderContext context) {
		renderer.render(context);
	}

	private Optional<Hotspot> findClosestHotspotInRange(@Nullable Entity entity) {
		if (CLIENT.world == null || entity == null) {
			return Optional.empty();
		}

		List<ArmorStandEntity> armorStands = CLIENT.world.getEntitiesByClass(
				ArmorStandEntity.class,
				entity.getBoundingBox().expand(DISTANCE_TO_HOTSPOT_IN_BLOCKS),
				Entity::hasCustomName
		);

		ArmorStandEntity closestHotspotArmorStand = armorStands.stream()
				.filter(as -> "HOTSPOT".equals(as.getName().getString()))
				.min(Comparator.comparingDouble(as -> as.squaredDistanceTo(entity)))
				.orElse(null);

		if (closestHotspotArmorStand == null) {
			return Optional.empty();
		}

		Optional<String> perk = armorStands.stream()
				.filter(e -> e.getX() == closestHotspotArmorStand.getX()
						&& e.getY() < closestHotspotArmorStand.getY()
						&& closestHotspotArmorStand.getY() - e.getY() <= 1
						&& e.getZ() == closestHotspotArmorStand.getZ()
						&& e.getPitch() == closestHotspotArmorStand.getPitch())
				.map(e -> e.getName().getString())
				.findFirst();

		Vec3d centerPos = closestHotspotArmorStand.getBlockPos().toCenterPos();

		return Optional.of(new Hotspot(closestHotspotArmorStand, centerPos, perk));
	}

	private void reset() {
		currentHotspot = null;
		hotspotRadius = null;
		bobberInHotspot = false;
	}
}
