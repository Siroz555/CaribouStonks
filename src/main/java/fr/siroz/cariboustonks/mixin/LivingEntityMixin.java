package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	protected LivingEntityMixin(EntityType<?> type, Level world) {
		super(type, world);
	}

	@ModifyExpressionValue(method = "getCurrentSwingDuration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/SwingAnimation;duration()I"))
	private int cariboustonks$editSwingDuration(int original) {
		return shouldEnableSwingModifications()
				? ConfigManager.getConfig().vanilla.itemModelCustomization.swingDuration
				: original;
	}

	@ModifyExpressionValue(method = "getCurrentSwingDuration", at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectUtil;hasDigSpeed(Lnet/minecraft/world/entity/LivingEntity;)Z"),
			@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z")
	}, require = 2)
	private boolean cariboustonks$ignoreMiningFatigueEffect(boolean original) {
		return (!shouldEnableSwingModifications() || !ConfigManager.getConfig().vanilla.itemModelCustomization.ignoreMiningEffects)
				&& original;
	}

	@Unique
	@SuppressWarnings("ConstantConditions")
	private boolean shouldEnableSwingModifications() {
		return ConfigManager.getConfig().vanilla.itemModelCustomization.enabled
				&& (Entity) this == Minecraft.getInstance().player;
	}
}
