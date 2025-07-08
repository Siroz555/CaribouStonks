package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.CaribouStonks;
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

	@Inject(method = "setStackInSlot", at = @At("TAIL"))
	private void cariboustonks$setStackInSlot(int slot, int revision, ItemStack stack, CallbackInfo ci) {
		containerOverlay.markHighlightsDirty();
	}

	@Inject(method = "updateSlotStacks", at = @At("TAIL"))
	private void cariboustonks$updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack, CallbackInfo ci) {
		containerOverlay.markHighlightsDirty();
	}
}
