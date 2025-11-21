package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class) // EntityRenderManager
public abstract class EntityRenderDispatcherMixin {

	@WrapWithCondition(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitFlame(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lorg/joml/Quaternionf;)V"))
	private boolean cariboustonks$cancelFireRendering(SubmitNodeCollector instance, PoseStack matrixStack, EntityRenderState entityRenderState, Quaternionf quaternionf) {
		return !ConfigManager.getConfig().vanilla.mob.hideFireOnEntities;
	}
}
