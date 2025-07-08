package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.screen.CaribouStonksMenuScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

	protected GameMenuScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "initWidgets", at = @At("RETURN"))
	private void cariboustonks$addButtonForCaribouStonksMenu(CallbackInfo ci) {
		this.addDrawableChild(ButtonWidget.builder(Text.literal("CaribouStonks"), (button) -> {
			if (this.client != null) {
				this.client.setScreen(new CaribouStonksMenuScreen());
			}
		}).dimensions(this.width - 105, this.height - 25, 100, 20).build());
	}
}
