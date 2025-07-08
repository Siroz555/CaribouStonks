package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.event.ChatEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

	@ModifyExpressionValue(method = {"addVisibleMessage", "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", "addToMessageHistory"}, at = @At(value = "CONSTANT", args = "intValue=100"), require = 3)
	private int cariboustonks$increaseChatHistoryLength(int maxMessages) {
		return Math.max(Math.max(maxMessages, ConfigManager.getConfig().chat.chatHistoryLength), 100);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"))
	private void cariboustonks$onMouseClickEvent(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
		if (MinecraftClient.getInstance().currentScreen instanceof ChatScreen) {
			ChatEvents.MESSAGE_CLICKED.invoker().onMessageClicked(mouseX, mouseY);
		}
	}
}
