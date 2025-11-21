package fr.siroz.cariboustonks.mixin.accessors;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.GuiMessage;
import net.minecraft.util.ArrayListDeque;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {

    @Accessor("recentChat")
    ArrayListDeque<String> getMessageHistory();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> getVisibleMessages();

    @Accessor("allMessages")
    List<GuiMessage> getMessages();

// TODO
//    @Invoker("getMessageLineIndex")
//	int invokeGetMessageLineIndex(double chatLineX, double chatLineY);
//
//    @Invoker("toChatLineX")
//	double invokeToChatLineX(double x);
//
//    @Invoker("toChatLineY")
//	double invokeToChatLineY(double y);
}
