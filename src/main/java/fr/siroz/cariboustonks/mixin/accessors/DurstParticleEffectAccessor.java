package fr.siroz.cariboustonks.mixin.accessors;

import net.minecraft.particle.DustParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DustParticleEffect.class)
public interface DurstParticleEffectAccessor {

	@Accessor("color")
	int getColor();
}
