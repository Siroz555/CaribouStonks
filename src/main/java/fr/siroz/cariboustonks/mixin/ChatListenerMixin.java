package fr.siroz.cariboustonks.mixin;

import fr.siroz.cariboustonks.events.ChatEvents;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatListener.class, priority = 55)
public abstract class ChatListenerMixin {

	@Inject(method = "handleSystemMessage", at = @At("HEAD"))
	private void cariboustonks$onSystemMessageEvent(Component message, boolean remote, CallbackInfo ci) {
		// overlay (ActionBar) est séparé maintenant, on a remote a la place, jcp c'est quoi
		if (message != null) {
			ChatEvents.MESSAGE_RECEIVE_EVENT.invoker().onMessage(message);
		}
	}
}
