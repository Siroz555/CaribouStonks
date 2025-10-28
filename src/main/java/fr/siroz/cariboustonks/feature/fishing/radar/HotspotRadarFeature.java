package fr.siroz.cariboustonks.feature.fishing.radar;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.math.bezier.ParticlePathPredictor;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HotspotRadarFeature extends Feature {

	private static final String HOTSPOT_RADAR_ITEM_ID = "HOTSPOT_RADAR";

	private final HotspotRadarRenderer renderer;
	private final ParticlePathPredictor predictor = new ParticlePathPredictor(3);

	private Vec3d guessPosition = null;
	private long lastUsedRadar = 0;

	public HotspotRadarFeature() {
		UseItemCallback.EVENT.register(this::onUseItem);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
		WorldEvents.JOIN.register(world -> reset());

		this.renderer = new HotspotRadarRenderer(this);
		RenderEvents.WORLD_RENDER.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& ConfigManager.getConfig().fishing.hotspotRadarGuess
				&& SkyBlockAPI.isOnIslands(IslandType.CRIMSON_ISLE, IslandType.BACKWATER_BAYOU);
	}

	@Nullable
	Vec3d getGuessPosition() {
		return guessPosition;
	}

	public void reset() {
		predictor.reset();
		guessPosition = null;
		lastUsedRadar = 0;
	}

	@EventHandler(event = "UseItemCallback.EVENT")
	private ActionResult onUseItem(PlayerEntity player, World _world, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (isEnabled() && stack != null && !stack.isEmpty()) {
			if (HOTSPOT_RADAR_ITEM_ID.equals(SkyBlockAPI.getSkyBlockItemId(stack))) {
				predictor.reset();
				guessPosition = null;
				lastUsedRadar = System.currentTimeMillis();
			}
		}

		return ActionResult.PASS;
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ParticleS2CPacket particle) {
		if (!isEnabled()) {
			return;
		}

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastUsedRadar > 2000) {
			return;
		}

		if (ParticleTypes.FLAME.equals(particle.getParameters().getType())
				&& particle.getCount() == 1
				&& particle.getSpeed() == 0f
		) {
			Vec3d position = new Vec3d(particle.getX(), particle.getY(), particle.getZ());
			handleParticle(position);
		}
	}

	@EventHandler(event = "WorldRenderEvents.AFTER_TRANSLUCENT")
	private void render(WorldRenderer context) {
		renderer.render(context);
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
