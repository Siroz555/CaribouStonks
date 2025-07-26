package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.WorldEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

	@Inject(method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$cancelSoundEvents(CallbackInfo ci, @Local(argsOnly = true) SoundEvent soundEvent) {
		if (SkyBlockAPI.isOnSkyBlock()) {
			if (WorldEvents.SOUND_CANCELLABLE.invoker().onSound(soundEvent)) {
				ci.cancel();
			}
		}
	}
}
