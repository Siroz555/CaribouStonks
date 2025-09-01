package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import fr.siroz.cariboustonks.util.render.GuiRenderUtils;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PostEffectPass.class)
public abstract class PostEffectPassMixin {

	@Contract("_, _ -> param1")
	@ModifyReceiver(method = "method_67884", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)V"))
	private RenderPass cariboustonks$applyBlurScissor(RenderPass renderPass, @NotNull RenderPipeline pipeline) {
		Identifier id = pipeline.getLocation();
		if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && id.getPath().startsWith("blur")) {
			GuiRenderUtils.applyBlurScissorToRenderPass(renderPass);
		}

		return renderPass;
	}
}
