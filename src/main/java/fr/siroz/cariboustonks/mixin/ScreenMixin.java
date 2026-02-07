package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.event.GuiEvents;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "onClose", at = @At("HEAD"))
    private void cariboustonks$onScreenCloseEvent(CallbackInfo ci) {
        GuiEvents.SCREEN_CLOSE_EVENT.invoker().onScreenClose((Screen) (Object) this);
    }
}
