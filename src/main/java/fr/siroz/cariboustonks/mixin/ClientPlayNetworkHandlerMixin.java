package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fr.siroz.cariboustonks.event.NetworkEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {

	protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(method = "onGameJoin", at = @At("TAIL"))
	private void cariboustonks$packetGameJoinEvent(GameJoinS2CPacket packet, CallbackInfo ci) {
		NetworkEvents.GAME_JOIN_PACKET.invoker().onGameJoin();
	}

	@Inject(method = "onWorldTimeUpdate", at = @At("TAIL"))
	private void cariboustonks$packetWorldTimeUpdateEvent(WorldTimeUpdateS2CPacket packet, CallbackInfo ci) {
		NetworkEvents.WORLD_TIME_UPDATE_PACKET.invoker().onWorldTimeUpdate();
	}

	@Inject(method = "onParticle", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$packetParticleEventCancellable(ParticleS2CPacket packet, CallbackInfo ci) {
		if (NetworkEvents.PARTICLE_PRE_RECEIVED_PACKET.invoker().onParticlePreReceived(packet)) {
			ci.cancel();
		}
	}

	@Inject(method = "onParticle", at = @At("RETURN"))
	private void cariboustonks$packetParticleEvent(ParticleS2CPacket packet, CallbackInfo ci) {
		NetworkEvents.PARTICLE_RECEIVED_PACKET.invoker().onParticleReceived(packet);
	}

	@Inject(method = "onPlaySound", at = @At(value = "RETURN"))
	private void cariboustonks$packetPlaySoundEvent(PlaySoundS2CPacket packet, CallbackInfo ci) {
		NetworkEvents.PLAY_SOUND_PACKET.invoker().onPlaySound(packet);
	}

	@Inject(method = "onEntityTrackerUpdate", at = @At("TAIL"))
	private void cariboustonks$packetEntityTrackerUpdateEvent(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity entity) {
		if (entity instanceof ArmorStandEntity armorStandEntity) {
			NetworkEvents.ARMORSTAND_UPDATE_PACKET.invoker().onArmorStandUpdate(armorStandEntity, false);
		}
	}

	@Inject(method = "onEntityEquipmentUpdate", at = @At("TAIL"))
	private void cariboustonks$packetEntityEquipmentUpdateEvent(EntityEquipmentUpdateS2CPacket packet, CallbackInfo ci, @Local Entity entity) {
		if (entity instanceof ArmorStandEntity armorStandEntity) {
			NetworkEvents.ARMORSTAND_UPDATE_PACKET.invoker().onArmorStandUpdate(armorStandEntity, true);
		}
	}
}
