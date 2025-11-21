package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

	@ModifyReturnValue(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At("RETURN"))
	private boolean cariboustonks$ShouldShowOwnNametag(boolean original, @Local(argsOnly = true) LivingEntity entity) {
		return entity instanceof LocalPlayer && ConfigManager.getConfig().vanilla.displayOwnNametagUsername || original;
	}
}
