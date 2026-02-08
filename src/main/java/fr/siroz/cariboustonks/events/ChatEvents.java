package fr.siroz.cariboustonks.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 * Events that are triggered based on chat-related interactions.
 */
public final class ChatEvents {

	private ChatEvents() {
	}

	/**
     * Called when the client received a message in the chat
     */
    public static final Event<MessageReceive> MESSAGE_RECEIVE_EVENT = EventFactory.createArrayBacked(MessageReceive.class, listeners -> text -> {
        for (MessageReceive listener : listeners) {
            listener.onMessage(text);
        }
    });

    @FunctionalInterface
    public interface MessageReceive {
        void onMessage(@NonNull Component text);
    }
}
