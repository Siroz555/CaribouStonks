package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.WorldEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class) // ClientWorld
public abstract class ClientLevelMixin implements BlockGetter {

	@Inject(method = "playSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZJ)V", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$cancelSoundEvents(CallbackInfo ci, @Local(argsOnly = true) SoundEvent soundEvent) {
		if (SkyBlockAPI.isOnSkyBlock()) {
			if (!WorldEvents.ALLOW_SOUND.invoker().allowSound(soundEvent)) {
				ci.cancel();
			}
		}
	}

	@Inject(method = "removeEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;onClientRemoval()V"))
	private void cariboustonks$onRemoveEntityEvent(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci, @Local Entity entity) {
		if (SkyBlockAPI.isOnSkyBlock() && entity instanceof ArmorStand armorStand) {
			WorldEvents.ARMORSTAND_REMOVED.invoker().onRemove(armorStand);
		}
	}

	@Inject(method = "setServerVerifiedBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"))
	private void cariboustonks$handleBlockUpdate(CallbackInfo ci, @Local(argsOnly = true) BlockPos pos, @Share("old") LocalRef<BlockState> oldState) {
		oldState.set(getBlockState(pos));
	}

	@Inject(method = "setServerVerifiedBlockState", at = @At("RETURN"))
	private void cariboustonks$handleBlockUpdateEvent(CallbackInfo ci, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state, @Share("old") LocalRef<BlockState> oldState) {
		if (pos != null) {
			WorldEvents.BLOCK_STATE_UPDATE.invoker().onBlockStateUpdate(pos, oldState.get(), state);
		}
	}
}
