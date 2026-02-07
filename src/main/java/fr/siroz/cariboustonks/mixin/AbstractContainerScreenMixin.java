package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.event.GuiEvents;
import fr.siroz.cariboustonks.system.ContainerOverlaySystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class) // HandledScreen
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {

	@Shadow
	@Nullable
	protected Slot hoveredSlot;

	@Shadow
	@Final
	protected T menu;

	@Unique
	private final ContainerOverlaySystem overlayManager = CaribouStonks.systems().getSystem(ContainerOverlaySystem.class);

	protected AbstractContainerScreenMixin(Component title) {
		super(title);
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "renderTooltip", at = @At("HEAD"))
	private void cariboustonks$drawContainerOverlay(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics context) {
		overlayManager.draw(context, (AbstractContainerScreen<ChestMenu>) (Object) this, this.menu.slots);
	}

	@Inject(method = "keyPressed", at = @At("HEAD"))
	private void cariboustonks$onKeyPressedEvent(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
		if (this.minecraft.player == null) {
			return;
		}

		if (this.hoveredSlot != null && input.input() != 256 && !this.minecraft.options.keyInventory.matches(input)) {
			GuiEvents.SCREEN_KEY_PRESS_EVENT.invoker().onKeyPressed(this, input, this.hoveredSlot);
		}
	}
}
