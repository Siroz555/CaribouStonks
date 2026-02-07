package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.event.ChatEvents;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatListener.class, priority = 55) // MessageHandler
public abstract class ChatListenerMixin {

	@Inject(method = "handleSystemMessage", at = @At("HEAD"))
	private void cariboustonks$onChatMessageEvent(Component message, boolean overlay, CallbackInfo ci) {
		if (!overlay && message != null) {
			ChatEvents.MESSAGE_RECEIVE_EVENT.invoker().onMessage(message);
		}
	}
}
