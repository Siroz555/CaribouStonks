package fr.siroz.cariboustonks.mixin.accessors;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.class)
public interface DisplayEntityDataScaleAccessor {

	@Accessor("DATA_SCALE_ID")
	static EntityDataAccessor<Vector3fc> getDataScale() {
		throw new UnsupportedOperationException();
	}
}
