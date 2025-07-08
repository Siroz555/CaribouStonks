package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	protected LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	/*@Inject(method = "playBlockFallSound", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$stopFallSound(CallbackInfo ci) {
		ci.cancel();
	}*/

	@ModifyExpressionValue(method = "getHandSwingDuration", at = @At(value = "CONSTANT", args = "intValue=6"))
	private int cariboustonks$editSwingDuration(int original) {
		return shouldEnableSwingModifications()
				? ConfigManager.getConfig().vanilla.itemModelCustomization.swingDuration
				: original;
	}

	@ModifyExpressionValue(method = "getHandSwingDuration", at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectUtil;hasHaste(Lnet/minecraft/entity/LivingEntity;)Z"),
			@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z")
	}, require = 2)
	private boolean cariboustonks$ignoreMiningFatigueEffect(boolean original) {
		return (!shouldEnableSwingModifications() || !ConfigManager.getConfig().vanilla.itemModelCustomization.ignoreMiningEffects)
				&& original;
	}

	@Unique
	@SuppressWarnings("ConstantConditions")
	private boolean shouldEnableSwingModifications() {
		return ConfigManager.getConfig().vanilla.itemModelCustomization.enabled
				&& (Entity) this == MinecraftClient.getInstance().player;
	}
}
