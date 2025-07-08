package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.event.NetworkEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PingMeasurer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PingMeasurer.class)
public abstract class PingMeasurerMixin {

	@ModifyArg(method = "onPingResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/MultiValueDebugSampleLogImpl;push(J)V"))
	private long cariboustonks$onPingResultEvent(long ping) {
		if (MinecraftClient.getInstance().player != null) {
			NetworkEvents.PING_RESULT.invoker().onPingResult(ping);
		}

		return ping;
	}
}
