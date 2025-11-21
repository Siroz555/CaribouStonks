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

    /**
     * Called when the client clicks on a message in the chat
     */
    public static final Event<@NotNull MessageClicked> MESSAGE_CLICKED = EventFactory.createArrayBacked(MessageClicked.class, listeners -> (mouseX, mouseY) -> {
        for (MessageClicked listener : listeners) {
            listener.onMessageClicked(mouseX, mouseY);
        }
    });

    @FunctionalInterface
    public interface MessageReceived {
        void onMessageReceived(@NotNull Component text);
    }

    @FunctionalInterface
    public interface MessageClicked {
        void onMessageClicked(double mouseX, double mouseY);
    }
}
