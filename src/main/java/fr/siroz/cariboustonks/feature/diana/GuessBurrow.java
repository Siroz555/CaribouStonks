package fr.siroz.cariboustonks.feature.diana;

import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.util.math.bezier.ParticlePathPredictor;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

final class GuessBurrow {

	private final MythologicalRitualFeature mythologicalRitual;
	private final ParticlePathPredictor predictor;
	private long lastSpadeUsed = 0;

	GuessBurrow(MythologicalRitualFeature mythologicalRitual) {
		this.mythologicalRitual = mythologicalRitual;
		this.predictor = new ParticlePathPredictor(3);

		UseItemCallback.EVENT.register(this::onUseItem);
		NetworkEvents.PARTICLE_RECEIVED_PACKET.register(this::onParticleReceived);
	}

	void reset() {
		predictor.reset();
		lastSpadeUsed = 0;
	}

	@EventHandler(event = "UseItemCallback.EVENT")
	private ActionResult onUseItem(PlayerEntity player, World world, Hand hand) {
		if (mythologicalRitual.isEnabled()
				&& mythologicalRitual.isGuessEnabled()
				&& SkyBlockAPI.isHoldingItem("ANCESTRAL_SPADE")
		) {
			predictor.reset();
			lastSpadeUsed = System.currentTimeMillis();
		}

		return ActionResult.PASS;
	}

	@EventHandler(event = "NetworkEvents.PARTICLE_RECEIVED_PACKET")
	private void onParticleReceived(ParticleS2CPacket particle) {
		if (!mythologicalRitual.isEnabled() || !mythologicalRitual.isGuessEnabled()) {
			return;
		}

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastSpadeUsed > 3000) {
			return;
		}

		if (ParticleTypes.DRIPPING_LAVA.equals(particle.getParameters().getType())
				&& particle.getCount() == 2
				&& particle.getSpeed() == -0.5f
		) {
			Vec3d position = new Vec3d(particle.getX(), particle.getY(), particle.getZ());
			handleParticle(position);
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

		mythologicalRitual.onBurrowGuess(solved);
	}
}
