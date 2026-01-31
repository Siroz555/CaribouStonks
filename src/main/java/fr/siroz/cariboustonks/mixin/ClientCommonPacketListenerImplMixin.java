package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.system.network.NetworkSystem;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class) // ClientCommonNetworkHandler
public abstract class ClientCommonPacketListenerImplMixin {

	@Unique
	private final NetworkSystem networkSystem = CaribouStonks.systems().getSystem(NetworkSystem.class);

	@Inject(method = "handlePing", at = @At("RETURN"))
	private void cariboustonks$onServerTick(ClientboundPingPacket packet, CallbackInfo ci) {
		networkSystem.onServerTick(packet);
	}
}
