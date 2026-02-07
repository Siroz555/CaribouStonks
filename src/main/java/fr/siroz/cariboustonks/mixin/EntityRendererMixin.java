package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.EntityGlowComponent;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.system.GlowingSystem;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

	@Unique
	private final GlowingSystem glowingSystem = CaribouStonks.systems().getSystem(GlowingSystem.class);

	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$shouldRenderEntity(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		if (SkyBlockAPI.isOnSkyBlock()) {
			if (!RenderEvents.ALLOW_RENDER_ENTITY_EVENT.invoker().allowRenderEntity(entity)) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void cariboustonks$updateOutlineColorWhenRenderStateUpdate(CallbackInfo ci, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) EntityRenderState state) {
		boolean hasModGlow = glowingSystem.hasOrComputeEntity(entity);
		boolean updateGlow = state.appearsGlowing() || hasModGlow;
		if (updateGlow && hasModGlow) {
			state.outlineColor = glowingSystem.getEntityColorOrDefault(entity, EntityGlowComponent.EntityGlowStrategy.DEFAULT);
		} else if (!updateGlow) {
			state.outlineColor = EntityRenderState.NO_OUTLINE;
		}
	}
}
