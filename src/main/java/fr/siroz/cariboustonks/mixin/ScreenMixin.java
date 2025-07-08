package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.event.CustomScreenEvents;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "close", at = @At("HEAD"))
    private void cariboustonks$onScreenCloseEvent(CallbackInfo ci) {
        CustomScreenEvents.CLOSE.invoker().onClose((Screen) (Object) this);
    }
}
