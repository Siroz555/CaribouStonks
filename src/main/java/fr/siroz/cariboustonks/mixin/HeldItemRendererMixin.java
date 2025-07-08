package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.config.configs.VanillaConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

	@Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
	private void cariboustonks$renderCustomHeldItem(CallbackInfo ci, @Local(argsOnly = true) Hand hand, @Local(argsOnly = true) MatrixStack matrices) {
		if (ConfigManager.getConfig().vanilla.itemModelCustomization.enabled) {

			VanillaConfig.ItemModelCustomization.CustomHand config = switch (hand) {
				case MAIN_HAND -> ConfigManager.getConfig().vanilla.itemModelCustomization.mainHand;
				case OFF_HAND -> ConfigManager.getConfig().vanilla.itemModelCustomization.offHand;
			};

			// C'est pas logique, même pour l'avoir utilisé, c'est pas "utile"
			/*if (config.xRotation != 0 || config.yRotation != 0 || config.zRotation != 0) {
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(config.xRotation));
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(config.yRotation));
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(config.zRotation));
			}*/

			if (config.scale != 1f) {
				matrices.scale(config.scale, config.scale, config.scale);
			}

			if (config.x != 0 || config.y != 0 || config.z != 0) {
				matrices.translate(config.x, config.y, config.z);
			}
		}
	}

	@ModifyExpressionValue(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"))
	private float cariboustonks$changeAnimationProgress(float original) {
		return ConfigManager.getConfig().vanilla.itemModelCustomization.enabled ? 1f : original;
	}
}
