package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import fr.siroz.cariboustonks.rendering.gui.GuiRenderer;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PostPass.class) // PostEffectPass
public abstract class PostPassMixin {

	@ModifyReceiver(method = "method_67884", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)V"))
	private RenderPass cariboustonks$applyBlurScissor(RenderPass renderPass, @NonNull RenderPipeline pipeline) {
		Identifier id = pipeline.getLocation();
		if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && id.getPath().startsWith("blur")) {
			GuiRenderer.applyBlurScissorToRenderPass(renderPass);
		}

		return renderPass;
	}
}
