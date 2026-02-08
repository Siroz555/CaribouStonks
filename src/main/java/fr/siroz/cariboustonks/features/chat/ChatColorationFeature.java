package fr.siroz.cariboustonks.features.chat;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
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
		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock(); // en vrai de partout ?
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onMessage(Component message) {
		if (!isEnabled()) return;

		String plain = StonksUtils.stripColor(message.getString());
		if (plain.startsWith("Party >")) { // Party
			if (this.config().chat.chatParty.chatPartyColored) {
				queueMessage(message, this.config().chat.chatParty.chatPartyColor.getRGB());
			}
		} else if (plain.startsWith("Guild >")) { // Guild
			if (this.config().chat.chatGuild.chatGuildColored) {
				Matcher guildJoinLeaveMatcher = GUILD_JOIN_LEAVE_PATTERN.matcher(plain);
				if (guildJoinLeaveMatcher.matches()) return;

				queueMessage(message, this.config().chat.chatGuild.chatGuildColor.getRGB());
			}
		}
	}

	private void queueMessage(Component original, int color) {
		MutableComponent newText = original.copy();
		((MutableComponent) newText.getSiblings().getLast()).withColor(color);

		// Parce que mon Mixin de ChatEvents est invoké trop tot ?
		//client.inGameHud.getChatHud().addMessage(message);
		((ChatListenerAccessor) CLIENT.getChatListener()).invokeAddToChatLog(newText, Instant.now());
		CLIENT.getNarrator().saySystemQueued(newText);
	}
}
