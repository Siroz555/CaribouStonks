package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.events.InteractionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class) // MinecraftClient
public abstract class MinecraftMixin {

	@Shadow
	@Nullable
	public LocalPlayer player;

	@WrapWithCondition(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;limitDisplayFPS(I)V"))
	private boolean cariboustonks$stopFpsLimiter(int fps) {
		return !ConfigManager.getConfig().vanilla.stopFpsLimiter;
	}

	// https://github.com/architectury/architectury-api/blob/1.19.2/fabric/src/main/java/dev/architectury/mixin/fabric/client/MixinMinecraft.java#L76
	@Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V", ordinal = 0))
	private void cariboustonks$leftClickAirEvent(CallbackInfoReturnable<Boolean> cir) {
		InteractionEvents.LEFT_CLICK_AIR_EVENT.invoker().onLeftClick(player, InteractionHand.MAIN_HAND);
	}

	// https://github.com/architectury/architectury-api/blob/1.19.2/fabric/src/main/java/dev/architectury/mixin/fabric/client/MixinMinecraft.java#L70
	/*@Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void rightClickAirEvent(CallbackInfo ci, Hand[] var1, int var2, int var3, Hand hand, ItemStack itemStack, EntityHitResult entityHitResult, Entity entity, ActionResult actionResult, BlockHitResult blockHitResult, int i, ActionResult actionResult2, ActionResult.Success success2) {
		if (itemStack.isEmpty() && (this.crosshairTarget == null || this.crosshairTarget.getType() == HitResult.Type.MISS))
			InteractionEvents.RIGHT_CLICK_AIR.invoker().onClick(player, Hand.MAIN_HAND);
	}*/
}
