package fr.siroz.cariboustonks.core.module.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public record ParticleData(
		@NonNull Vec3 position,
		@NonNull ParticleType<?> type,
		int count,
		float maxSpeed
) {
}
