package fr.siroz.cariboustonks.mixin;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
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
    private static void onInit(CallbackInfo ci) {
        if (ConfigManager.getConfig().uiAndVisuals.beaconBeamWithNoDepthTest) {
            BEACON_BEAM_TRANSLUCENT = RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.BEACON_BEAM_SNIPPET)
                            .withLocation("pipeline/beacon_beam_translucent")
                            .withDepthWrite(false)
                            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                            .withBlend(BlendFunction.TRANSLUCENT) // <-
                            .build());
        }
    }
}
