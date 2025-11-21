package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.util.render.gui.SplashTextSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	@Shadow
	@Nullable
	private SplashRenderer splash;

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "init")
	protected void cariboustonks$coucou(CallbackInfo ci) {
		SplashTextSupplier.getInstance().get().ifPresent(text -> this.splash = text);
	}
}
