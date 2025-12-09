package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.event.InteractionEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

	@Inject(method = "interactBlockInternal", at = @At(value = "HEAD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlockInternal(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"), cancellable = true)
	public void cariboustonks$cancelBlockInteraction(ClientPlayerEntity player, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> info) {
		ItemStack heldItem = player.getStackInHand(hand);
		if (heldItem == null || heldItem.isEmpty()) {
			return; //return ActionResult.PASS;
		}

		if (!InteractionEvents.ALLOW_INTERACT_BLOCK.invoker().onInteract(heldItem)) {
			info.setReturnValue(ActionResult.FAIL);
		}
	}
}
