package fr.siroz.cariboustonks.feature.misc;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.waypoint.Waypoint;
import fr.siroz.cariboustonks.system.waypoint.options.TextOption;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.math.bezier.ParticlePathPredictor;
import fr.siroz.cariboustonks.util.position.Position;
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

	private final ParticlePathPredictor predictor = new ParticlePathPredictor(3);

	private final Waypoint waypoint;
	private Vec3 guessPosition = null;
	private long lastUsedFinder = 0;

	public HoppityEggFinderFeature() {
		UseItemCallback.EVENT.register(this::onUseItem);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
		RenderEvents.WORLD_RENDER.register(this::render);

		this.waypoint = Waypoint.builder(builder -> {
			builder.enabled(false);
			builder.color(Colors.GREEN);
			builder.type(Waypoint.Type.OUTLINED_HIGHLIGHT);
			builder.textOption(TextOption.builder()
					.withText(Component.literal("Guess").withColor(Colors.GREEN.asInt()))
					.withDistance(true)
					.build());
		});
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().misc.hoppityEggFinderGuess;
	}

	@Override
	protected void onClientJoinServer() {
		predictor.reset();
		guessPosition = null;
		lastUsedFinder = 0;
	}

	@EventHandler(event = "UseItemCallback.EVENT")
	private InteractionResult onUseItem(Player player, Level _level, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isEnabled() && !stack.isEmpty()) {
			if (EGG_LOCATOR_ITEM_ID.equals(SkyBlockAPI.getSkyBlockItemId(stack))) {
				predictor.reset();
				guessPosition = null;
				lastUsedFinder = System.currentTimeMillis();
			}
		}

		return InteractionResult.PASS;
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ClientboundLevelParticlesPacket particle) {
		if (!isEnabled()) return;

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastUsedFinder > 5000) {
			return;
		}

		if (ParticleTypes.HAPPY_VILLAGER.equals(particle.getParticle().getType())
				&& particle.getCount() == 1
				&& particle.getMaxSpeed() == 0f
		) {
			Vec3 position = new Vec3(particle.getX(), particle.getY(), particle.getZ());
			handleParticle(position);
		}
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	public void render(WorldRenderer renderer) {
		if (!isEnabled()) return;

		if (guessPosition != null) {
			waypoint.setEnabled(true);
			waypoint.updatePosition(Position.of(guessPosition));
			waypoint.getRenderer().render(renderer);
		}
	}

	private void handleParticle(Vec3 position) {
		if (predictor.isEmpty()) {
			predictor.addPoint(position);
			return;
		}

		Vec3 lastPoint = predictor.getLastPoint();
		if (lastPoint == null) {
			return;
		}

		double distance = lastPoint.distanceTo(position);
		if (distance == 0.0D || distance > 3.0D) {
			return;
		}

		predictor.addPoint(position);

		Vec3 solved = predictor.solve();
		if (solved == null) {
			return;
		}

		guessPosition = solved;
	}
}
