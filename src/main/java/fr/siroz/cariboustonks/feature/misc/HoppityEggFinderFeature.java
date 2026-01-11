package fr.siroz.cariboustonks.feature.misc;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.manager.waypoint.options.TextOption;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.math.bezier.ParticlePathPredictor;
import fr.siroz.cariboustonks.util.position.Position;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HoppityEggFinderFeature extends Feature {

	private static final String EGG_LOCATOR_ITEM_ID = "EGGLOCATOR";

	private final ParticlePathPredictor predictor = new ParticlePathPredictor(3);

	private final Waypoint waypoint;
	private Vec3d guessPosition = null;
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
					.withText(Text.literal("Guess").withColor(Colors.GREEN.asInt()))
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
	private ActionResult onUseItem(PlayerEntity player, World _level, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (isEnabled() && !stack.isEmpty()) {
			if (EGG_LOCATOR_ITEM_ID.equals(SkyBlockAPI.getSkyBlockItemId(stack))) {
				predictor.reset();
				guessPosition = null;
				lastUsedFinder = System.currentTimeMillis();
			}
		}

		return ActionResult.PASS;
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ParticleS2CPacket particle) {
		if (!isEnabled()) return;

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastUsedFinder > 5000) {
			return;
		}

		if (ParticleTypes.HAPPY_VILLAGER.equals(particle.getParameters().getType())
				&& particle.getCount() == 1
				&& particle.getSpeed() == 0f
		) {
			Vec3d position = new Vec3d(particle.getX(), particle.getY(), particle.getZ());
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

	private void handleParticle(Vec3d position) {
		if (predictor.isEmpty()) {
			predictor.addPoint(position);
			return;
		}

		Vec3d lastPoint = predictor.getLastPoint();
		if (lastPoint == null) {
			return;
		}

		double distance = lastPoint.distanceTo(position);
		if (distance == 0.0D || distance > 3.0D) {
			return;
		}

		predictor.addPoint(position);

		Vec3d solved = predictor.solve();
		if (solved == null) {
			return;
		}

		guessPosition = solved;
	}
}
