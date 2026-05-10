package fr.siroz.cariboustonks.platform.mixin;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.systems.ContainerOverlaySystem;
import java.util.List;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

	@Unique
	private final ContainerOverlaySystem overlaySystem = CaribouStonks.systems().getSystem(ContainerOverlaySystem.class);

	@Inject(method = "setItem", at = @At("TAIL"))
	private void cariboustonks$setStackInSlot(int slot, int stateId, ItemStack itemStack, CallbackInfo ci) {
		overlaySystem.markHighlightsDirty();
	}

	@Inject(method = "initializeContents", at = @At("TAIL"))
	private void cariboustonks$updateSlotStacks(int stateId, List<ItemStack> items, ItemStack carried, CallbackInfo ci) {
		overlaySystem.markHighlightsDirty();
	}
}
