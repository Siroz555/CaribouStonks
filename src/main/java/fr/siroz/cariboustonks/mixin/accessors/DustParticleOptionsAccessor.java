package fr.siroz.cariboustonks.mixin.accessors;

import net.minecraft.core.particles.DustParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DustParticleOptions.class)
public interface DustParticleOptionsAccessor {

	@Accessor("color")
	int getColor();
}
