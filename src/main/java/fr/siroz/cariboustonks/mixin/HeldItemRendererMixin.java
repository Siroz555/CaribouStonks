package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.config.configs.VanillaConfig;
import fr.siroz.cariboustonks.screen.HeldItemViewConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

	@Inject(method = "renderFirstPersonItem" , at = @At("HEAD"))
	private void cariboustonks$changeHeldItemForConfigScreen(CallbackInfo ci, @Local(argsOnly = true) LocalRef<Hand> hand, @Local(argsOnly = true, ordinal = 2) LocalFloatRef swingProgress, @Local(argsOnly = true) LocalRef<ItemStack> stack, @Local(argsOnly = true, ordinal = 3) LocalFloatRef equipProgress, @Local(argsOnly = true) LocalIntRef light) {
		if (MinecraftClient.getInstance().currentScreen instanceof HeldItemViewConfigScreen heldItemViewConfigScreen) {
			hand.set(heldItemViewConfigScreen.getHand());
			swingProgress.set(0f);
			stack.set(heldItemViewConfigScreen.getPreviewItem());
			equipProgress.set(0f);
			light.set(LightmapTextureManager.MAX_LIGHT_COORDINATE);
		}
	}
}
