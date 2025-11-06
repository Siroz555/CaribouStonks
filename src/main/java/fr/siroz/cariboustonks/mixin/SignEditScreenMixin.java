package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSignEditScreen.class)
public abstract class SignEditScreenMixin extends Screen {

	@Shadow
	public abstract void close();

	protected SignEditScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "keyPressed", at = @At("HEAD"))
	private void cariboustonks$onKeyPressedEvent(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
		if (input.getKeycode() != 257) return;
		if (!SkyBlockAPI.isOnSkyBlock() || !ConfigManager.getConfig().general.stonks.bazaarSignEditEnterValidation) return;
		this.close();
	}
}
