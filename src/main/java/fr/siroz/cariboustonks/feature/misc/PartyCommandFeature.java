package fr.siroz.cariboustonks.feature.misc;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.config.configs.MiscConfig;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.network.NetworkManager;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.position.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyCommandFeature extends Feature {

	private static final List<String> MAGIC_8BALL_ANSWERS = new ArrayList<>();
	private static final long COOLDOWN_MS = 750L;

	private long lastActionMs = 0L;

	public PartyCommandFeature() {
		ChatEvents.MESSAGE_RECEIVED.register(this::onChatMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().misc.partyCommands.enabled;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onChatMessage(@NotNull Text text) {
		if (!isEnabled()) return;

		String input = StonksUtils.stripColor(text.getString());
		if (!input.startsWith("Party >")) return;
		if (CLIENT.player == null || CLIENT.world == null) return;

		for (PartyCommand command : PartyCommand.values()) {
			if (command.getConfig().test(ConfigManager.getConfig().misc.partyCommands)) {
				Matcher matcher = command.getPattern().matcher(input);
				if (matcher.find()) {
					long now = System.currentTimeMillis();
					if (now - lastActionMs < COOLDOWN_MS) {
						break;
					}

					sendCommand(command, matcher);
					lastActionMs = now;
					break;
				}
			}
		}
	}

	private void sendCommand(PartyCommand command, Matcher matcher) {
		try {
			command.getAction().accept(matcher);
		} catch (Exception ex) {
			CaribouStonks.LOGGER.error("[PartyCommandFeature] Unable to handle {}", command.name(), ex);
		}
	}

	private enum PartyCommand {
		COORDS(Pattern.compile("Party > (\\[.+])? ?(.+) ?[ቾ⚒]?: !coords"), cmd -> cmd.coords, matcher -> {
			Position position = Position.of(Client.getCurrentPosition());
			Client.sendChatMessage("/pc " + position.asChatCoordinates(), true);
		}),
		WARP(Pattern.compile("Party > (\\[.+])? ?(.+) ?[ቾ⚒]?: !warp"), cmd -> cmd.warp, matcher -> {
			Client.sendChatMessage("/p warp", true); // -_-
		}),
		DICE(Pattern.compile("Party > (\\[.+])? ?(.+) ?[ቾ⚒]?: !dice"), cmd -> cmd.diceGame, matcher -> {
			int roll = (int) (1 + Math.floor(Math.random() * 6));
			String extra = roll == 1 ? " Sheeh!" : roll == 6 ? " Waw!" : "";
			String message = matcher.group(2) + " rolled a " + roll + "." + extra;
			Client.sendChatMessage("/pc " + message, true);
		}),
		EIGHT_BALL(Pattern.compile("Party > (\\[.+])? ?(.+) ?[ቾ⚒]?: !8ball"), cmd -> cmd.magic8Ball, matcher -> {
			int r = new Random().nextInt(MAGIC_8BALL_ANSWERS.size());
			String selected = MAGIC_8BALL_ANSWERS.get(r);
			Client.sendChatMessage("/pc " + selected, true);
		}),
		CF(Pattern.compile("Party > (\\[.+])? ?(.+) ?[ቾ⚒]?: !cf"), cmd -> cmd.coinFlip, matcher -> {
			if (new Random().nextBoolean()) {
				Client.sendChatMessage("/pc HEADS!", true);
			} else {
				Client.sendChatMessage("/pc TAILS!", true);
			}
		}),
		TPS(Pattern.compile("Party > (\\[.+])? ?(.+) ?[ቾ⚒]?: !tps"), cmd -> cmd.tps, matcher -> {
			float tps = CaribouStonks.managers().getManager(NetworkManager.class).getTickRate();
			String message = String.format("TPS: %.1f", tps);
			Client.sendChatMessage("/pc " + message, true);
		}),
		;

		private final Pattern pattern;
		private final Predicate<MiscConfig.PartyCommands> config;
		private final Consumer<Matcher> action;

		PartyCommand(Pattern pattern, Predicate<MiscConfig.PartyCommands> config, Consumer<Matcher> action) {
			this.pattern = pattern;
			this.config = config;
			this.action = action;
		}

		public Pattern getPattern() {
			return pattern;
		}

		public Predicate<MiscConfig.PartyCommands> getConfig() {
			return config;
		}

		public Consumer<Matcher> getAction() {
			return action;
		}
	}

	static {
		MAGIC_8BALL_ANSWERS.add("It is certain.");
		MAGIC_8BALL_ANSWERS.add("It is decidedly so.");
		MAGIC_8BALL_ANSWERS.add("Without a doubt.");
		MAGIC_8BALL_ANSWERS.add("Yes – definitely.");
		MAGIC_8BALL_ANSWERS.add("You may rely on it.");
		MAGIC_8BALL_ANSWERS.add("As I see it, yes.");
		MAGIC_8BALL_ANSWERS.add("Most likely.");
		MAGIC_8BALL_ANSWERS.add("Outlook good.");
		MAGIC_8BALL_ANSWERS.add("Yes.");
		MAGIC_8BALL_ANSWERS.add("No.");
		MAGIC_8BALL_ANSWERS.add("Reply hazy, try again.");
		MAGIC_8BALL_ANSWERS.add("Ask again later.");
		MAGIC_8BALL_ANSWERS.add("Better not tell you now.");
		MAGIC_8BALL_ANSWERS.add("Concentrate and ask again.");
		MAGIC_8BALL_ANSWERS.add("Don't count on it.");
		MAGIC_8BALL_ANSWERS.add("My reply is no.");
		MAGIC_8BALL_ANSWERS.add("My sources say no.");
		MAGIC_8BALL_ANSWERS.add("Outlook not so good.");
		MAGIC_8BALL_ANSWERS.add("Very doubtful.");
		MAGIC_8BALL_ANSWERS.add("Absolutely not.");
		MAGIC_8BALL_ANSWERS.add("Absolutely yes.");
		MAGIC_8BALL_ANSWERS.add("If you believe hard enough.");
		MAGIC_8BALL_ANSWERS.add("The stars say no.");
		MAGIC_8BALL_ANSWERS.add("The stars say yes.");
		MAGIC_8BALL_ANSWERS.add("Try again after coffee.");
		MAGIC_8BALL_ANSWERS.add("404: answer not found.");
		MAGIC_8BALL_ANSWERS.add("Sheesh... maybe.");
		MAGIC_8BALL_ANSWERS.add("Waw! Definitely.");
	}
}
