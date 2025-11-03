package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.WorldEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin implements BlockView {

	@Inject(method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$cancelSoundEvents(CallbackInfo ci, @Local(argsOnly = true) SoundEvent soundEvent) {
		if (SkyBlockAPI.isOnSkyBlock()) {
			if (!WorldEvents.ALLOW_SOUND.invoker().allowSound(soundEvent)) {
				ci.cancel();
			}
		}
	}

	@Inject(method = "removeEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onRemoved()V"))
	private void cariboustonks$onRemoveEntityEvent(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci, @Local Entity entity) {
		if (SkyBlockAPI.isOnSkyBlock() && entity instanceof ArmorStandEntity armorStand) {
			WorldEvents.ARMORSTAND_REMOVED.invoker().onRemove(armorStand);
		}
	}

	@Inject(method = "handleBlockUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z"))
	private void cariboustonks$handleBlockUpdate(CallbackInfo ci, @Local(argsOnly = true) BlockPos pos, @Share("old") LocalRef<BlockState> oldState) {
		oldState.set(getBlockState(pos));
	}

	@Inject(method = "handleBlockUpdate", at = @At("RETURN"))
	private void cariboustonks$handleBlockUpdateEvent(CallbackInfo ci, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state, @Share("old") LocalRef<BlockState> oldState) {
		if (pos != null) {
			WorldEvents.BLOCK_STATE_UPDATE.invoker().onBlockStateUpdate(pos, oldState.get(), state);
		}
	}
}
