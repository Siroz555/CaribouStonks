package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DownloadingTerrainScreen.class)
public abstract class DownloadingTerrainScreenMixin {

	@Inject(method = {"render", "renderBackground"}, at = @At("HEAD"), cancellable = true)
	private void cariboustonks$hideDownloadingTerrainScreen(CallbackInfo ci) {
		if (ConfigManager.getConfig().vanilla.hideWorldLoadingScreen) {
			ci.cancel();
		}
	}
}
