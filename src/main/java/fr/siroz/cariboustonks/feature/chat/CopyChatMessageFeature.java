package fr.siroz.cariboustonks.feature.chat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.mixin.accessors.ChatComponentAccessor;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.math.MathUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

public class CopyChatMessageFeature extends Feature {

	public CopyChatMessageFeature() {
		ScreenEvents.AFTER_INIT.register((_mc, screen, _sw, _sh) -> {
			if (screen instanceof ChatScreen chat) {
				ScreenMouseEvents.afterMouseClick(chat).register(this::onMouseClick);
			}
		});
	}

	@Override
	public boolean isEnabled() {
		return this.config().chat.copyChat;
	}

	@EventHandler(event = "ScreenMouseEvents.afterMouseClick")
	private boolean onMouseClick(Screen screen, MouseButtonEvent event, boolean consumed) {
		if (!isEnabled()) return false;
		if (!Client.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)) return false;

		try {
			ChatComponentAccessor chatAccessor = ((ChatComponentAccessor) CLIENT.gui.getChat());
			double chatLineX = toChatLineX(event.x());
			double chatLineY = toChatLineY(event.y());
			int messageIndex = getMessageAt(chatLineX, chatLineY);

			List<GuiMessage> messages = chatAccessor.getMessages();
			if (messageIndex > -1 && messageIndex < messages.size()) {
				Component message = messages.get(messageIndex).content();
				String toClipboard = StonksUtils.stripColor(message.getString());
				Client.setToClipboard(toClipboard);
				return true;
			}
		} catch (Exception ex) {
			CaribouStonks.LOGGER.error("{} Unable to handle the clicked message", getShortName(), ex);
		}

		return false;
	}

	private double toChatLineX(double x) {
		ChatComponentAccessor chatAccessor = ((ChatComponentAccessor) CLIENT.gui.getChat());
		return x / chatAccessor.invokeGetScale() - 4.0d;
	}

	private double toChatLineY(double y) {
		ChatComponentAccessor chatAccessor = ((ChatComponentAccessor) CLIENT.gui.getChat());
		double height = CLIENT.getWindow().getGuiScaledHeight() - y - 40.0d;
		return height / (chatAccessor.invokeGetScale() * chatAccessor.invokeGetLineHeight());
	}

	private int getMessageAt(double chatLineX, double chatLineY) {
		ChatComponent chatHud = CLIENT.gui.getChat();
		ChatComponentAccessor chatAccessor = (ChatComponentAccessor) chatHud;
		if (!chatHud.isChatFocused() || chatAccessor.invokeIsChatHidden()) {
			return -1;
		}

		double maxWidth = MathUtils.floor(chatAccessor.invokeGetWidth() / chatAccessor.invokeGetScale());
		if (chatLineX < -4.0 || chatLineX > maxWidth) {
			return -1;
		}

		List<GuiMessage.Line> visible = chatAccessor.getVisibleMessages();
		List<GuiMessage> messages = chatAccessor.getMessages();
		// Position verticale
		int linesPerPage = Math.min(chatHud.getLinesPerPage(), visible.size());
		if (chatLineY < 0.0 || chatLineY >= linesPerPage) {
			return -1;
		}
		// Index de ligne
		int lineIndex = MathUtils.floor(chatLineY + chatAccessor.getChatScrollbarPos());
		if (lineIndex < 0 || lineIndex >= visible.size()) {
			return -1;
		}

		int lowerBound = findMessageEnd(visible, lineIndex);
		if (lowerBound < 0) {
			return -1;
		}

		int upperBound = findMessageStart(visible, lowerBound);
		String hoveredNormalized = buildNormalizedContent(visible, upperBound, lowerBound);

		return findMessageIndex(messages, hoveredNormalized);
	}

	private int findMessageEnd(List<GuiMessage.Line> visible, int startIndex) {
		int index = startIndex;
		while (index >= 0 && !visible.get(index).endOfEntry()) {
			index--;
		}
		return index;
	}

	private int findMessageStart(List<GuiMessage.Line> visible, int lowerBound) {
		for (int i = lowerBound + 1; i < visible.size(); i++) {
			if (visible.get(i).endOfEntry()) {
				return i - 1;
			}
		}
		return visible.size() - 1;
	}

	private String buildNormalizedContent(List<GuiMessage.Line> visible, int start, int end) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i >= end; i--) {
			visible.get(i).content().accept((index, style, codePoint) -> {
				if (!Character.isWhitespace(codePoint)) {
					sb.appendCodePoint(codePoint);
				}
				return true;
			});
		}
		return StringUtils.deleteWhitespace(StonksUtils.stripColor(sb.toString()));
	}

	private int findMessageIndex(List<GuiMessage> messages, String normalizedContent) {
		Map<String, Integer> normToIndex = new HashMap<>((int) (messages.size() / 0.75f) + 1);
		for (int i = 0; i < messages.size(); i++) {
			String stipped = StonksUtils.stripColor(messages.get(i).content().getString());
			String normalized = StringUtils.deleteWhitespace(stipped);
			normToIndex.putIfAbsent(normalized, i); // conserve le premier index
		}

		Integer found = normToIndex.get(normalizedContent);
		return found != null ? found : -1;
	}
}
