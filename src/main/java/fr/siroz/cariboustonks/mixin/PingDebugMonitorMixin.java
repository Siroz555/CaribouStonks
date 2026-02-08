package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.events.NetworkEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PingDebugMonitor.class) // PingMeasurer
public abstract class PingDebugMonitorMixin {

	@ModifyArg(method = "onPongReceived", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/debugchart/LocalSampleLogger;logSample(J)V"))
	private long cariboustonks$onPingResultEvent(long ping) {
		if (Minecraft.getInstance().player != null) {
			NetworkEvents.PING_RESULT.invoker().onPingResult(ping);
		}

		return ping;
	}
}
