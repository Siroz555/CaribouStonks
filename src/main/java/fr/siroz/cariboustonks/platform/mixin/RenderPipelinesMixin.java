package fr.siroz.cariboustonks.platform.mixin;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPipelines.class)
public abstract class RenderPipelinesMixin {

    @Final
    @Mutable
    @Shadow
    public static RenderPipeline BEACON_BEAM_TRANSLUCENT;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onInit(CallbackInfo ci) { // SIROZ-NOTE: Optional DepthStencilState (LevelRenderer)
        if (ConfigManager.getConfig().uiAndVisuals.beaconBeamWithNoDepthTest) {
            BEACON_BEAM_TRANSLUCENT = RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.BEACON_BEAM_SNIPPET)
                            .withLocation("pipeline/beacon_beam_translucent")
							.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
							.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                            .build());
        }
    }
}
