package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ItemEvents;
import fr.siroz.cariboustonks.manager.container.overlay.ContainerOverlayManager;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

	@Unique
	private final ContainerOverlayManager containerOverlay = CaribouStonks.managers().getManager(ContainerOverlayManager.class);

	@Inject(method = "setStackInSlot", at = @At("HEAD"))
	private void cariboustonks$onItemPickupEvent(int slot, int revision, ItemStack stack, CallbackInfo ci) {
		if (SkyBlockAPI.isOnSkyBlock() && stack != null && !stack.isEmpty()) {
			// < 9 useless, >= 45 not in the player inventory
			int inventorySlot = slot;
			if (inventorySlot < 9 || inventorySlot >= 45) {
				return;
			}

			// Hotbar slots are at the end of the ids instead of at the start like in the inventory main stacks
			if (inventorySlot >= 36) {
				inventorySlot = inventorySlot - 36;
			}

			ItemEvents.PICKUP.invoker().onPickup(inventorySlot, stack);
		}
	}

	@Inject(method = "setStackInSlot", at = @At("TAIL"))
	private void cariboustonks$setStackInSlot(int slot, int revision, ItemStack stack, CallbackInfo ci) {
		containerOverlay.markHighlightsDirty();
	}

	@Inject(method = "updateSlotStacks", at = @At("TAIL"))
	private void cariboustonks$updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack, CallbackInfo ci) {
		containerOverlay.markHighlightsDirty();
	}
}
