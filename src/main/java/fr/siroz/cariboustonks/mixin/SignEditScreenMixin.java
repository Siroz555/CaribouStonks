package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSignEditScreen.class)
public abstract class SignEditScreenMixin extends Screen {

	@Shadow
	public abstract void onClose();

	protected SignEditScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "keyPressed", at = @At("HEAD"))
	private void cariboustonks$onKeyPressedEvent(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
		if (input.input() != 257) return;
		if (!SkyBlockAPI.isOnSkyBlock() || !ConfigManager.getConfig().general.stonks.bazaarSignEditEnterValidation) return;
		this.onClose();
	}
}
