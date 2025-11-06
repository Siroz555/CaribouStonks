package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.event.InteractionEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

	@Shadow
	@Nullable
	public ClientPlayerEntity player;

	@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;limitDisplayFPS(I)V"))
	private boolean cariboustonks$stopFpsLimiter(int fps) {
		return !ConfigManager.getConfig().vanilla.stopFpsLimiter;
	}

	@Inject(method = "joinWorld", at = @At("TAIL"))
	private void cariboustonks$onJoinWorldEvent(ClientWorld world, CallbackInfo ci) {
		WorldEvents.JOIN.invoker().onJoinWorld(world);
	}

	// https://github.com/architectury/architectury-api/blob/1.19.2/fabric/src/main/java/dev/architectury/mixin/fabric/client/MixinMinecraft.java#L76
	@Inject(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;resetLastAttackedTicks()V", ordinal = 0))
	private void cariboustonks$leftClickAirEvent(CallbackInfoReturnable<Boolean> cir) {
		InteractionEvents.LEFT_CLICK_AIR.invoker().onClick(player, Hand.MAIN_HAND);
	}

	// https://github.com/architectury/architectury-api/blob/1.19.2/fabric/src/main/java/dev/architectury/mixin/fabric/client/MixinMinecraft.java#L70
	/*@Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void rightClickAirEvent(CallbackInfo ci, Hand[] var1, int var2, int var3, Hand hand, ItemStack itemStack, EntityHitResult entityHitResult, Entity entity, ActionResult actionResult, BlockHitResult blockHitResult, int i, ActionResult actionResult2, ActionResult.Success success2) {
		if (itemStack.isEmpty() && (this.crosshairTarget == null || this.crosshairTarget.getType() == HitResult.Type.MISS))
			InteractionEvents.RIGHT_CLICK_AIR.invoker().onClick(player, Hand.MAIN_HAND);
	}*/
}
