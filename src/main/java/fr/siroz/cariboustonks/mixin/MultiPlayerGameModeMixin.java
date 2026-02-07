package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.event.InteractionEvents;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

	@Inject(method = "performUseItemOn", at = @At(value = "HEAD"), cancellable = true)
	public void cariboustonks$cancelBlockInteraction(LocalPlayer player, InteractionHand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> info) {
		ItemStack heldItem = player.getItemInHand(hand);
		if (heldItem.isEmpty()) {
			return; //return ActionResult.PASS;
		}

		if (!InteractionEvents.ALLOW_INTERACT_BLOCK_EVENT.invoker().allowInteract(heldItem)) {
			info.setReturnValue(InteractionResult.FAIL);
		}
	}
}
