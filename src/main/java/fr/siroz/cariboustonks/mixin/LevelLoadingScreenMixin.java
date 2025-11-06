package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin {

	// Pas le Panorama, perso ça me dérange pas, car un black screen...
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$hideLoadingScreen(CallbackInfo ci) {
		if (ConfigManager.getConfig().vanilla.hideWorldLoadingScreen) {
			ci.cancel();
		}
	}
}
