package fr.siroz.cariboustonks.platform.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import fr.siroz.cariboustonks.platform.rendering.gui.GuiRenderer;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PostPass.class)
public abstract class PostPassMixin {

	@ModifyReceiver(method = "lambda$addToFrame$1", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)V"))
	private RenderPass cariboustonks$applyBlurScissor(@NonNull RenderPass renderPass, @NonNull RenderPipeline pipeline) {
		Identifier id = pipeline.getLocation();
		if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && id.getPath().startsWith("blur")) {
			GuiRenderer.applyBlurScissorToRenderPass(renderPass);
		}
		return renderPass;
	}
}
