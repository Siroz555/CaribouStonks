package fr.siroz.cariboustonks.core.module.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public record ParticleData(
		@NonNull Vec3 position,
		@NonNull ParticleType<?> type,
		int count,
		float maxSpeed,
		float xMaxSpeed,
		float yMaxSpeed,
		float zMaxSpeed
) {

	@NonNull
	public static ParticleData of(@NonNull ClientboundLevelParticlesPacket packet) {
		return new ParticleData(
				new Vec3(packet.x(), packet.y(), packet.z()),
				packet.particle().getType(),
				packet.count(),
				packet.xMaxSpeed(), // maxSpeed "base"
				packet.xMaxSpeed(),
				packet.yMaxSpeed(),
				packet.zMaxSpeed()
		);
	}
}
