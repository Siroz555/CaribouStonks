package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.manager.network.NetworkManager;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin {

	@Unique
	private final NetworkManager networkManager = CaribouStonks.managers().getManager(NetworkManager.class);

	@Inject(method = "onPing", at = @At("RETURN"))
	private void cariboustonks$onServerTick(CommonPingS2CPacket packet, CallbackInfo ci) {
		networkManager.onServerTick(packet);
	}
}
