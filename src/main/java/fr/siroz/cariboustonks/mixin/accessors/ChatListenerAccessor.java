package fr.siroz.cariboustonks.mixin.accessors;

import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.time.Instant;

@Mixin(ChatListener.class)
public interface ChatListenerAccessor {

	@Invoker("logSystemMessage")
    void invokeAddToChatLog(Component message, Instant timestamp);
}
