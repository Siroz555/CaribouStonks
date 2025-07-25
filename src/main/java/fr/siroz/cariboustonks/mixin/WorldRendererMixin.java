package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.manager.glowing.GlowingManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = WorldRenderer.class, priority = 1001)
@SuppressWarnings("ALL")
public abstract class WorldRendererMixin {

	@Unique
	private final GlowingManager glowingManager = CaribouStonks.managers().getManager(GlowingManager.class);

	@ModifyExpressionValue(method = {"getEntitiesToRender", "renderEntities"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"), require = 2)
	private boolean cariboustonks$shouldGlowMob(boolean original, @Local Entity entity) {
		return glowingManager.hasOrComputeEntity(entity) ? true : original;
	}

	@ModifyVariable(method = "renderEntities", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;setColor(IIII)V")), at = @At("STORE"), ordinal = 0)
	private int cariboustonks$getGlowColor(int color, @Local Entity entity) {
		return glowingManager.getEntityColorOrDefault(entity, color);
	}
}
