package fr.siroz.cariboustonks.mixin.accessors;

import dev.isxander.yacl3.config.v2.impl.ConfigClassHandlerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ConfigClassHandlerImpl.class, remap = false)
public interface ConfigClassHandlerImplAccessor {

	@Accessor("instance")
	void setInstance(Object instance);
}
