package fr.siroz.cariboustonks.platform.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.events.InteractionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.InteractionHand;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Shadow
	@Nullable
	public LocalPlayer player;

	// https://github.com/architectury/architectury-api/blob/1.19.2/fabric/src/main/java/dev/architectury/mixin/fabric/client/MixinMinecraft.java#L76
	@Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V", ordinal = 0))
	private void cariboustonks$leftClickAirEvent(CallbackInfoReturnable<Boolean> cir) {
		InteractionEvents.LEFT_CLICK_AIR_EVENT.invoker().onLeftClick(player, InteractionHand.MAIN_HAND);
	}

	@WrapWithCondition(method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;stop()V"))
	private boolean cariboustonks$ignoreStopSoundFlag(SoundManager soundManager) {
		return !ConfigManager.getConfig().vanilla.uninterruptedMusic;
	}
}
