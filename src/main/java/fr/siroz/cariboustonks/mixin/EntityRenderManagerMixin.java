package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderManager.class)
public abstract class EntityRenderManagerMixin {

	@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitFire(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/entity/state/EntityRenderState;Lorg/joml/Quaternionf;)V"))
	private boolean cariboustonks$cancelFireRendering(OrderedRenderCommandQueue instance, MatrixStack matrixStack, EntityRenderState entityRenderState, Quaternionf quaternionf) {
		return !ConfigManager.getConfig().vanilla.mob.hideFireOnEntities;
	}
}
