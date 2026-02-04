package fr.siroz.cariboustonks.feature.chat;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.config.configs.ChatConfig;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.mixin.accessors.ChatListenerAccessor;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ChatColorationFeature extends Feature {

	//private static final Pattern PARTY_PATTERN = Pattern.compile("Party > (\\[.+])? ?(.+) ?[ቾ⚒]?: (.+)$");
	private static final Pattern GUILD_JOIN_LEAVE_PATTERN = Pattern.compile("^Guild > [^ ]+ (joined|left)\\.$");

	public ChatColorationFeature() {
		ChatEvents.MESSAGE_RECEIVED.register(this::onMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock(); // en vrai de partout ?
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onMessage(Component message) {
		if (!isEnabled()) {
			return;
		}

		String plain = StonksUtils.stripColor(message.getString());
		if (plain.startsWith("Party >")) { // Party
			ChatConfig.ChatParty config = ConfigManager.getConfig().chat.chatParty;
			if (!config.chatPartyColored) {
				return;
			}

			MutableComponent newText = message.copy();
			((MutableComponent) newText.getSiblings().getLast()).withColor(config.chatPartyColor.getRGB());
			sendMessageToBypassEvents(newText);

		} else if (plain.startsWith("Guild >")) { // Guild
			ChatConfig.ChatGuild config = ConfigManager.getConfig().chat.chatGuild;
			if (!config.chatGuildColored) {
				return;
			}

			Matcher guildJoinLeaveMatcher = GUILD_JOIN_LEAVE_PATTERN.matcher(plain);
			if (guildJoinLeaveMatcher.matches()) {
				return;
			}

			MutableComponent newText = message.copy();
			((MutableComponent) newText.getSiblings().getLast()).withColor(config.chatGuildColor.getRGB());
			sendMessageToBypassEvents(newText);
		}
	}

	private void sendMessageToBypassEvents(Component message) {
		// Parce que mon Mixin de ChatEvents est invoké trop tot ?
		//client.inGameHud.getChatHud().addMessage(message);
		((ChatListenerAccessor) CLIENT.getChatListener()).invokeAddToChatLog(message, Instant.now());
		CLIENT.getNarrator().saySystemQueued(message);
	}
}
