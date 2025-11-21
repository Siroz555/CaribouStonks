package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fr.siroz.cariboustonks.event.NetworkEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class) // ClientPlayNetworkHandler
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {

	protected ClientPacketListenerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(method = "handleLogin", at = @At("TAIL"))
	private void cariboustonks$packetGameJoinEvent(ClientboundLoginPacket packet, CallbackInfo ci) {
		NetworkEvents.GAME_JOIN_PACKET.invoker().onGameJoin();
	}

	@Inject(method = "handleSetTime", at = @At("TAIL"))
	private void cariboustonks$packetWorldTimeUpdateEvent(ClientboundSetTimePacket packet, CallbackInfo ci) {
		NetworkEvents.WORLD_TIME_UPDATE_PACKET.invoker().onWorldTimeUpdate();
	}

	@Inject(method = "handleParticleEvent", at = @At("HEAD"), cancellable = true)
	private void cariboustonks$packetParticleEventCancellable(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
		if (NetworkEvents.PARTICLE_PRE_RECEIVED_PACKET.invoker().onParticlePreReceived(packet)) {
			ci.cancel();
		}
	}

	@Inject(method = "handleParticleEvent", at = @At("RETURN"))
	private void cariboustonks$packetParticleEvent(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
		NetworkEvents.PARTICLE_RECEIVED_PACKET.invoker().onParticleReceived(packet);
	}

	@Inject(method = "handleSoundEvent", at = @At(value = "RETURN"))
	private void cariboustonks$packetPlaySoundEvent(ClientboundSoundPacket packet, CallbackInfo ci) {
		NetworkEvents.PLAY_SOUND_PACKET.invoker().onPlaySound(packet);
	}

	@Inject(method = "handleSetEntityData", at = @At("TAIL"))
	private void cariboustonks$packetEntityTrackerUpdateEvent(ClientboundSetEntityDataPacket packet, CallbackInfo ci, @Local Entity entity) {
		if (entity instanceof ArmorStand armorStandEntity) {
			NetworkEvents.ARMORSTAND_UPDATE_PACKET.invoker().onArmorStandUpdate(armorStandEntity, false);
		}
	}

	@Inject(method = "handleSetEquipment", at = @At("TAIL"))
	private void cariboustonks$packetEntityEquipmentUpdateEvent(ClientboundSetEquipmentPacket packet, CallbackInfo ci, @Local Entity entity) {
		if (entity instanceof ArmorStand armorStandEntity) {
			NetworkEvents.ARMORSTAND_UPDATE_PACKET.invoker().onArmorStandUpdate(armorStandEntity, true);
		}
	}
}
