package fr.siroz.cariboustonks.platform.mixin;

import fr.siroz.cariboustonks.platform.rendering.gui.GuiRenderer;
import fr.siroz.cariboustonks.platform.rendering.world.WorldRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Inject(method = "close", at = @At("TAIL"))
	private void cariboustonks$onGameRendererClose(CallbackInfo ci) {
		WorldRenderer.close();
	}

	@Inject(method = "processBlurEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;process(Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;)V", shift = At.Shift.AFTER))
	private void cariboustonks$onBlurRendered(CallbackInfo ci) {
		GuiRenderer.disableBlurScissor();
	}
}
