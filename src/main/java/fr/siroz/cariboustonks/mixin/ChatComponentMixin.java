package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fr.siroz.cariboustonks.config.ConfigManager;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatComponent.class) // ChatHud
public abstract class ChatComponentMixin {

	@ModifyExpressionValue(method = {"addMessageToDisplayQueue", "addMessageToQueue", "addRecentChat"}, at = @At(value = "CONSTANT", args = "intValue=100"), require = 3)
	private int cariboustonks$increaseChatHistoryLength(int maxMessages) {
		return Math.max(Math.max(maxMessages, ConfigManager.getConfig().chat.chatHistoryLength), 100);
	}
}
