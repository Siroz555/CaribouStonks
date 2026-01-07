package fr.siroz.cariboustonks.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Events that are triggered based on chat-related interactions.
 */
public final class ChatEvents {

	private ChatEvents() {
	}

	/**
     * Called when the client received a message in the chat
     */
    public static final Event<@NotNull MessageReceived> MESSAGE_RECEIVED = EventFactory.createArrayBacked(MessageReceived.class, listeners -> text -> {
        for (MessageReceived listener : listeners) {
            listener.onMessageReceived(text);
        }
    });

    @FunctionalInterface
    public interface MessageReceived {
        void onMessageReceived(@NotNull Component text);
    }
}
