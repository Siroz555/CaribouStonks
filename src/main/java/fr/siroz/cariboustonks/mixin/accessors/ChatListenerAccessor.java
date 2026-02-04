package fr.siroz.cariboustonks.mixin.accessors;

import java.time.Instant;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatListener.class)
public interface ChatListenerAccessor {

	@Invoker("logSystemMessage")
    void invokeAddToChatLog(Component message, Instant timestamp);
}
