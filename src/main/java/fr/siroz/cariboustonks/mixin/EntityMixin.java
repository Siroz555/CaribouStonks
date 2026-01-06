package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.manager.slayer.SlayerManager;
import java.util.UUID;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	protected UUID uuid;

	@Unique
	private final SlayerManager slayerManager = CaribouStonks.managers().getManager(SlayerManager.class);

	@Inject(method = "onRemove", at = @At("TAIL"))
	private void cariboustonks$onRemoveEntityInvoke(Entity.RemovalReason reason, CallbackInfo ci) {
		slayerManager.invokeEntityBossDeath(uuid);
	}
}
