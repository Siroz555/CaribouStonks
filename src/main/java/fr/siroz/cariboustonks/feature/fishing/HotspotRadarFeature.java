package fr.siroz.cariboustonks.feature.fishing;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.particle.ParticleData;
import fr.siroz.cariboustonks.core.module.particle.ParticleTracker;
import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.module.waypoint.options.TextOption;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.position.Position;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HotspotRadarFeature extends Feature {

	private static final String HOTSPOT_RADAR_ITEM_ID = "HOTSPOT_RADAR";

	private final Waypoint waypoint;
	private final ParticleTracker tracker;

	public HotspotRadarFeature() {
		this.waypoint = Waypoint.builder(builder -> {
			builder.enabled(false);
			builder.color(Color.fromFormatting(ChatFormatting.LIGHT_PURPLE));
			builder.textOption(TextOption.builder()
					.withText(Component.literal("Guess").withStyle(ChatFormatting.LIGHT_PURPLE))
					.withDistance(false)
					.build());
		});

		this.tracker = ParticleTracker.builder()
				.predictor(3)
				.trackingDuration(2000)
				.particleFilter(particle -> ParticleTypes.FLAME.equals(particle.type())
						&& particle.count() == 1
						&& particle.maxSpeed() == 0f)
				.onPositionPredicted(vec3 -> {
					waypoint.setEnabled(true);
					waypoint.updatePosition(Position.of(vec3));
				})
				.onTrackingReset(() -> this.waypoint.setEnabled(false))
				.build();

		UseItemCallback.EVENT.register(this::onUseItem);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
		RenderEvents.WORLD_RENDER_EVENT.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& this.config().fishing.hotspotRadarGuess
				&& SkyBlockAPI.isOnIslands(IslandType.CRIMSON_ISLE, IslandType.BACKWATER_BAYOU);
	}

	@Override
	protected void onClientJoinServer() {
		reset();
	}

	public void reset() {
		waypoint.setEnabled(false);
		tracker.reset();
	}

	@EventHandler(event = "UseItemCallback.EVENT")
	private InteractionResult onUseItem(Player player, Level _world, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isEnabled() && !stack.isEmpty() && HOTSPOT_RADAR_ITEM_ID.equals(SkyBlockAPI.getSkyBlockItemId(stack))) {
			tracker.startTracking();
		}
		return InteractionResult.PASS;
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ClientboundLevelParticlesPacket particle) {
		if (!isEnabled()) return;
		if (!tracker.isTracking()) return;

		tracker.handleParticle(new ParticleData(
				new Vec3(particle.getX(), particle.getY(), particle.getZ()),
				particle.getParticle().getType(),
				particle.getCount(),
				particle.getMaxSpeed()
		));
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void render(WorldRenderer renderer) {
		if (isEnabled() && waypoint.isEnabled()) {
			waypoint.getRenderer().render(renderer);
		}
	}
}
