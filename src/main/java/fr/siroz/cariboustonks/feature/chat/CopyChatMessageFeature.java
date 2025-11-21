package fr.siroz.cariboustonks.feature.chat;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.minecraft.client.GuiMessage;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class CopyChatMessageFeature extends Feature {

	public CopyChatMessageFeature() {
		ChatEvents.MESSAGE_CLICKED.register(this::onChatClick);
	}

	@Override
	public boolean isEnabled() {
		return ConfigManager.getConfig().chat.copyChat;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_CLICKED")
	private void onChatClick(double mouseX, double mouseY) {
		if (!isEnabled()) {
			return;
		}

		if (!Client.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			return;
		}

		GuiMessage message = getMessageAt(mouseX, mouseY);
		if (message != null) {
			String toClipboard = StonksUtils.stripColor(message.content().getString());
			CLIENT.keyboardHandler.setClipboard(toClipboard);
		}
	}

	private @Nullable GuiMessage getMessageAt(double x, double y) {
		return null;
//		try {
//			ChatHudAccessor accessor = (ChatHudAccessor) MinecraftClient.getInstance().inGameHud.getChatHud();
//			int lineSelected = accessor.invokeGetMessageLineIndex(
//					accessor.invokeToChatLineX(x),
//					accessor.invokeToChatLineY(y)
//			);
//
//			if (lineSelected == -1) {
//				return null;
//			}
//
//			List<Integer> indexesOfEntryEnds = IntStream.range(0, accessor.getVisibleMessages().size())
//					.filter(index -> accessor.getVisibleMessages().get(index).endOfEntry())
//					.boxed()
//					.toList();
//
//			int indexOfMessageEntryEnd = indexesOfEntryEnds
//					.stream()
//					.filter(index -> index <= lineSelected)
//					.reduce((a, b) -> b)
//					.orElse(-1);
//
//			if (indexOfMessageEntryEnd == -1) {
//				return null;
//			}
//
//			int indexOfMessage = indexesOfEntryEnds.indexOf(indexOfMessageEntryEnd);
//			return accessor.getMessages().get(indexOfMessage);
//
//		} catch (Exception exception) { // index out of bounds.. = ???
//			CaribouStonks.LOGGER.error("[CopyChatMessageFeature] Error to get ChatHudLine", exception);
//			return null;
//		}
	}
}
