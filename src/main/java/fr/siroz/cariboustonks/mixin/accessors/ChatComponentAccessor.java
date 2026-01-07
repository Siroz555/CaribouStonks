package fr.siroz.cariboustonks.mixin.accessors;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> getVisibleMessages();

    @Accessor("allMessages")
    List<GuiMessage> getMessages();

	@Invoker
	double invokeGetScale();

	@Invoker
	int invokeGetLineHeight();

	@Invoker
	boolean invokeIsChatHidden();

	@Invoker
	int invokeGetWidth();

	@Accessor
	int getChatScrollbarPos();
}
