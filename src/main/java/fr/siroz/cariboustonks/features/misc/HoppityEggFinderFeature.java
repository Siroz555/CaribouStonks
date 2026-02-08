package fr.siroz.cariboustonks.features.misc;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.particle.ParticleData;
import fr.siroz.cariboustonks.core.module.particle.ParticleTracker;
import fr.siroz.cariboustonks.core.module.position.Position;
import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.module.waypoint.options.TextOption;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.DeveloperTools;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HoppityEggFinderFeature extends Feature {

	private static final String EGG_LOCATOR_ITEM_ID = "EGGLOCATOR";

	private final Waypoint waypoint;
	private final ParticleTracker tracker;

	public HoppityEggFinderFeature() {
		this.waypoint = Waypoint.builder(builder -> {
			builder.enabled(false);
			builder.color(Colors.GREEN);
			builder.type(Waypoint.Type.OUTLINED_HIGHLIGHT);
			builder.textOption(TextOption.builder()
					.withText(Component.literal("Guess").withColor(Colors.GREEN.asInt()))
					.withDistance(true)
					.build());
		});

		this.tracker = ParticleTracker.builder()
				.predictor(3)
				.trackingDuration(5000)
				.particleFilter(particle -> ParticleTypes.HAPPY_VILLAGER.equals(particle.type())
						&& particle.count() == 1
						&& particle.maxSpeed() == 0f)
				.onTrackingStarted(this::debug)
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
		return SkyBlockAPI.isOnSkyBlock() && this.config().misc.hoppityEggFinderGuess;
	}

	@Override
	protected void onClientJoinServer() {
		tracker.reset();
	}

	@EventHandler(event = "UseItemCallback.EVENT")
	private InteractionResult onUseItem(Player player, Level _level, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isEnabled() && !stack.isEmpty() && EGG_LOCATOR_ITEM_ID.equals(SkyBlockAPI.getSkyBlockItemId(stack))) {
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

	private void debug() {
		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info(":: onTrackingStarted :: {}", this.waypoint.isEnabled());

			Vec3 predicted = tracker.getPredictedPosition();
			if (predicted != null) {
				CaribouStonks.LOGGER.info(":: predicted :: {}", predicted);
			}
		}
	}
}
