package fr.siroz.cariboustonks.platform.mixin;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.mod.NetworkManager;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {

	@Unique
	private final NetworkManager networkManager = CaribouStonks.mod().getNetworkManager();

	@Inject(method = "handlePing", at = @At("RETURN"))
	private void cariboustonks$onServerTick(ClientboundPingPacket packet, CallbackInfo ci) {
		networkManager.onServerTick(packet);
	}
}
