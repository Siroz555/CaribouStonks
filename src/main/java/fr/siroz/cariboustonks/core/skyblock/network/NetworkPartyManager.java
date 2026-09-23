package fr.siroz.cariboustonks.core.skyblock.network;

import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.platform.context.ClientContext;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class NetworkPartyManager {
	private static final Pattern YOU_JOINED_PATTERN = Pattern.compile("You have joined (?<name>.*)'s? party!");
	private static final Pattern OTHERS_JOINED_PATTERN = Pattern.compile("(?<name>[^:]+) joined the party\\.");
	private static final Pattern OTHERS_IN_PATTERN = Pattern.compile("You'll be partying with: (?<names>.*)");
	private static final Pattern OTHERS_LEFT_PATTERN = Pattern.compile("(?<name>[^:]+) has left the party\\.");
	private static final Pattern OTHER_KICKED_PATTERN = Pattern.compile("(?<name>[^:]+) has been removed from the party\\.");
	private static final Pattern OFFLINE_KICKED_PATTERN = Pattern.compile("Kicked (?<name>.*) because they were offline\\.");
	private static final Pattern DISCONNECTED_PATTERN = Pattern.compile("(?<name>[^:]+) was removed from your party because they disconnected\\.");
	private static final Pattern TRANSFERRED_PATTERN = Pattern.compile("The party was transferred to (?<newOwner>.*) by (?<name>.*)");
	private static final Pattern TRANSFERRED_ON_LEAVE_PATTERN = Pattern.compile("The party was transferred to (?<newOwner>.*) because (?<name>.*) left");
	private static final Pattern DISBANDED_PATTERN = Pattern.compile("[^:]+ has disbanded the party!");
	private static final Pattern KICKED_PATTERN = Pattern.compile("You have been kicked from the party by .*");
	private static final Pattern PARTY_FINDER_DUNGEON_PATTERN = Pattern.compile("Party Finder > (?<name>.*?) joined the dungeon group! \\(.* Level \\d+\\)");
	private static final Pattern PARTY_FINDER_KUUDRA_PATTERN = Pattern.compile("Party Finder > (?<name>.*?) joined the group! \\(Combat Level \\d+\\)");

	private static final List<Pattern> PARTY_FINDER_PATTERNS = List.of(
			PARTY_FINDER_DUNGEON_PATTERN, PARTY_FINDER_KUUDRA_PATTERN
	);
	private static final List<Pattern> REMOVE_PATTERNS = List.of(
			OTHERS_LEFT_PATTERN, OTHER_KICKED_PATTERN, OFFLINE_KICKED_PATTERN
	);
	private static final Set<String> PARTY_LEFT_MESSAGES = Set.of(
			"You left the party.",
			"The party was disbanded because all invites expired and the party was empty.",
			"You are not currently in a party.",
			"You are not in a party.",
			"The party was disbanded because the party leader disconnected."
	);

	private final List<String> members = new ArrayList<>();
	private @Nullable String leader = null;
	private @Nullable String lastLeader = null;

	public NetworkPartyManager() {
		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::handleChatMessageListener);
	}

	public boolean isInParty() {
		return !members.isEmpty();
	}

	public @Nullable String getLeader() {
		return leader;
	}

	private void handleChatMessageListener(@NonNull Component component) {
		String message = StonksUtils.stripColor(component.getString());

		if (handleJoin(message) || handleLeave(message) || handleTransfer(message)) return;
		handlePartyLeft(message);
	}

	private boolean handleJoin(String message) {
		Matcher matcher = YOU_JOINED_PATTERN.matcher(message);
		if (matcher.matches()) {
			String name = cleanPlayerName(matcher.group("name"));
			leader = name;
			addPlayer(name);
			return true;
		}

		matcher = OTHERS_JOINED_PATTERN.matcher(message);
		if (matcher.matches()) {
			if (members.isEmpty()) leader = ClientContext.getPlayerName();
			addPlayer(cleanPlayerName(matcher.group("name")));
			return true;
		}

		matcher = OTHERS_IN_PATTERN.matcher(message);
		if (matcher.matches()) {
			for (String name : matcher.group("names").split(", ")) {
				addPlayer(cleanPlayerName(name));
			}
			return true;
		}

		for (Pattern pattern : PARTY_FINDER_PATTERNS) {
			matcher = pattern.matcher(message);
			if (matcher.matches()) {
				addPlayer(cleanPlayerName(matcher.group("name")));
				return true;
			}
		}
		return false;
	}

	private boolean handleLeave(String message) {
		for (Pattern pattern : REMOVE_PATTERNS) {
			Matcher matcher = pattern.matcher(message);
			if (matcher.matches()) {
				String name = cleanPlayerName(matcher.group("name"));
				members.remove(name);
				if (name.equals(lastLeader)) lastLeader = null;
				return true;
			}
		}

		Matcher matcher = DISCONNECTED_PATTERN.matcher(message);
		if (matcher.matches()) {
			members.remove(cleanPlayerName(matcher.group("name")));
			return true;
		}
		return false;
	}

	private boolean handleTransfer(String message) {
		Matcher matcher = TRANSFERRED_ON_LEAVE_PATTERN.matcher(message);
		if (matcher.matches()) {
			leader = cleanPlayerName(matcher.group("newOwner"));
			members.remove(cleanPlayerName(matcher.group("name")));
			return true;
		}

		matcher = TRANSFERRED_PATTERN.matcher(message);
		if (matcher.matches()) {
			leader = cleanPlayerName(matcher.group("newOwner"));
			lastLeader = cleanPlayerName(matcher.group("name"));
			return true;
		}
		return false;
	}

	private void handlePartyLeft(String message) {
		if (DISBANDED_PATTERN.matcher(message).matches()
				|| KICKED_PATTERN.matcher(message).matches()
				|| PARTY_LEFT_MESSAGES.contains(message)
		) {
			members.clear();
			leader = null;
			lastLeader = null;
		}
	}

	private void addPlayer(String playerName) {
		if (!members.contains(playerName) && !playerName.equals(ClientContext.getPlayerName())) {
			members.add(playerName);
		}
	}

	private static String cleanPlayerName(String raw) {
		String[] parts = raw.trim().split(" ");
		String name = parts.length > 1 ? parts[1] : parts[0];
		return name.endsWith("'s") ? name.substring(0, name.length() - 2) : name;
	}
}
