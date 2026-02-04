package fr.siroz.cariboustonks.feature.fishing.radar;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.math.bezier.ParticlePathPredictor;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HotspotRadarFeature extends Feature {

	private static final String HOTSPOT_RADAR_ITEM_ID = "HOTSPOT_RADAR";

	private final HotspotRadarRenderer renderer;
	private final ParticlePathPredictor predictor = new ParticlePathPredictor(3);

	private Vec3 guessPosition = null;
	private long lastUsedRadar = 0;

	public HotspotRadarFeature() {
		UseItemCallback.EVENT.register(this::onUseItem);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);

		this.renderer = new HotspotRadarRenderer(this);
		RenderEvents.WORLD_RENDER.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& ConfigManager.getConfig().fishing.hotspotRadarGuess
				&& SkyBlockAPI.isOnIslands(IslandType.CRIMSON_ISLE, IslandType.BACKWATER_BAYOU);
	}

	@Override
	protected void onClientJoinServer() {
		reset();
	}

	@Nullable
    Vec3 getGuessPosition() {
		return guessPosition;
	}

	public void reset() {
		predictor.reset();
		guessPosition = null;
		lastUsedRadar = 0;
	}

	@EventHandler(event = "UseItemCallback.EVENT")
	private InteractionResult onUseItem(Player player, Level _world, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isEnabled() && !stack.isEmpty()) {
			if (HOTSPOT_RADAR_ITEM_ID.equals(SkyBlockAPI.getSkyBlockItemId(stack))) {
				predictor.reset();
				guessPosition = null;
				lastUsedRadar = System.currentTimeMillis();
			}
		}

		return InteractionResult.PASS;
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ClientboundLevelParticlesPacket particle) {
		if (!isEnabled()) {
			return;
		}

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastUsedRadar > 2000) {
			return;
		}

		if (ParticleTypes.FLAME.equals(particle.getParticle().getType())
				&& particle.getCount() == 1
				&& particle.getMaxSpeed() == 0f
		) {
			Vec3 position = new Vec3(particle.getX(), particle.getY(), particle.getZ());
			handleParticle(position);
		}
	}

	@EventHandler(event = "WorldRenderEvents.AFTER_TRANSLUCENT")
	private void render(WorldRenderer context) {
		renderer.render(context);
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
