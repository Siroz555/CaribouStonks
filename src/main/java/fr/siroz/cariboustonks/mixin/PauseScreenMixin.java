package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.screen.CaribouStonksMenuScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class) // GameMenuScreen
public abstract class PauseScreenMixin extends Screen {

	protected PauseScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "createPauseMenu", at = @At("RETURN"))
	private void cariboustonks$addButtonForCaribouStonksMenu(CallbackInfo ci) {
		this.addRenderableWidget(Button.builder(Component.literal("CaribouStonks"), (button) -> {
			this.minecraft.setScreen(new CaribouStonksMenuScreen());
		}).bounds(this.width - 105, this.height - 25, 100, 20).build());
	}
}
