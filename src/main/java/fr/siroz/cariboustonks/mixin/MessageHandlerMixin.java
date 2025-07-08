package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.event.ChatEvents;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MessageHandler.class, priority = 555)
public abstract class MessageHandlerMixin {

	@Inject(method = "onGameMessage", at = @At("HEAD"))
	private void cariboustonks$onChatMessageEvent(Text message, boolean overlay, CallbackInfo ci) {
		if (!overlay && message != null && message.getString() != null) {
			ChatEvents.MESSAGE_RECEIVED.invoker().onMessageReceived(message);
		}
	}
}
