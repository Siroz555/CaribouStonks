package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.event.CustomScreenEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {

	@Shadow
	@Nullable
	protected Slot focusedSlot;

	protected HandledScreenMixin(Text title) {
		super(title);
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
