package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.event.CustomScreenEvents;
import fr.siroz.cariboustonks.manager.container.overlay.ContainerOverlayManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

	@Shadow
	@Nullable
	protected Slot focusedSlot;

	@Shadow
	@Final
	protected T handler;

	@Unique
	private final ContainerOverlayManager overlayManager = CaribouStonks.managers().getManager(ContainerOverlayManager.class);


	protected HandledScreenMixin(Text title) {
		super(title);
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "drawMouseoverTooltip", at = @At("HEAD"))
	private void cariboustonks$drawContainerOverlay(CallbackInfo ci, @Local(argsOnly = true) DrawContext context) {
		overlayManager.draw(context, (HandledScreen<GenericContainerScreenHandler>) (Object) this, this.handler.slots);
	}

	@Inject(at = @At("HEAD"), method = "keyPressed")
	public void cariboustonks$onKeyPressedEvent(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (this.client == null || this.client.player == null) {
			return;
		}

		if (this.focusedSlot != null && keyCode != 256 && !this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
			CustomScreenEvents.KEY_PRESSED.invoker().onKeyPressed(this, keyCode, scanCode, this.focusedSlot);
		}
	}
}
