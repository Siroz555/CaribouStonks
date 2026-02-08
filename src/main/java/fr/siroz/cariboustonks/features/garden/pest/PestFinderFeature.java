package fr.siroz.cariboustonks.features.garden.pest;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.particle.ParticleData;
import fr.siroz.cariboustonks.core.module.particle.ParticleTracker;
import fr.siroz.cariboustonks.core.module.position.Position;
import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.module.waypoint.options.TextOption;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.InteractionEvents;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class PestFinderFeature extends Feature {

	private static final String PEST_PREFIX = "ൠ";
	private static final String VACUUM_ID_PREFIX = "VACUUM";

	private final Waypoint waypoint;
	private final ParticleTracker tracker;

	public PestFinderFeature() {
		this.waypoint = Waypoint.builder(builder -> {
			builder.enabled(false);
			builder.color(Colors.GREEN);
			builder.textOption(TextOption.builder()
					.withText(Component.literal("Guess").withStyle(ChatFormatting.GREEN))
					.withDistance(false)
					.build());
		});

		this.tracker = ParticleTracker.builder()
				.predictor(3)
				.trackingDuration(5000)
				.particleFilter(particle -> ParticleTypes.ANGRY_VILLAGER.equals(particle.type()))
				.onPositionPredicted(vec3 -> {
					waypoint.setEnabled(true);
					waypoint.updatePosition(Position.of(vec3));
				})
				.onTrackingReset(() -> this.waypoint.setEnabled(false))
				.build();

		InteractionEvents.LEFT_CLICK_AIR_EVENT.register(this::onLeftClickAir);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
		RenderEvents.WORLD_RENDER_EVENT.register(this::onWorldRender);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GARDEN
				&& this.config().farming.garden.pestsLocator;
	}

	@Override
	protected void onClientJoinServer() {
		waypoint.setEnabled(false);
		tracker.reset();
	}

	@EventHandler(event = "InteractionEvents.LEFT_CLICK_AIR_EVENT")
	private void onLeftClickAir(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isEnabled() && !stack.isEmpty() && SkyBlockAPI.getSkyBlockItemId(stack).contains(VACUUM_ID_PREFIX)) {
			tracker.startTracking();
		}
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ClientboundLevelParticlesPacket packet) {
		if (!isEnabled()) return;
		if (!tracker.isTracking()) return;

		tracker.handleParticle(new ParticleData(
				new Vec3(packet.getX(), packet.getY(), packet.getZ()),
				packet.getParticle().getType(),
				packet.getCount(),
				packet.getMaxSpeed()
		));
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void onWorldRender(WorldRenderer renderer) {
		if (!isEnabled()) return;
		if (CLIENT.player == null || CLIENT.level == null) return;

		if (waypoint.isEnabled()) {
			waypoint.getRenderer().render(renderer);
		}

		for (Entity entity : CLIENT.level.entitiesForRendering()) {
			if (!(entity instanceof ArmorStand as)) continue;
			if (!as.hasCustomName() || !as.getName().getString().startsWith(PEST_PREFIX)) continue;

			renderer.submitLineFromCursor(as.getEyePosition(), Colors.GREEN, 1f);
		}
	}
}
