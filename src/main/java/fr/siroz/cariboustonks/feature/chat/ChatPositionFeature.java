package fr.siroz.cariboustonks.feature.chat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.config.configs.UIAndVisualsConfig;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.diana.MythologicalRitualFeature;
import fr.siroz.cariboustonks.manager.command.CommandComponent;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.waypoint.options.TextOption;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.cooldown.Cooldown;
import fr.siroz.cariboustonks.util.position.Position;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatPositionFeature extends Feature {

	private static final Pattern SIMPLE_COORDS_PATTERN = Pattern.compile(
			"(?<playerName>.*): (?<x>-?[0-9]+) (?<y>[0-9]+) (?<z>-?[0-9]+)");

	private static final Pattern SIMPLE_COMMA_COORDS_PATTERN = Pattern.compile(
			"(?<playerName>.*): (?<x>-?[0-9]+), (?<y>[0-9]+), (?<z>-?[0-9]+)");

	private static final Pattern GENERIC_POSITION_PATTERN = Pattern.compile(
			"(?<playerName>.*): x: (?<x>-?[0-9]+), y: (?<y>[0-9]+), z: (?<z>-?[0-9]+)");

	private static final List<Pattern> PATTERNS = List.of(
			SIMPLE_COORDS_PATTERN,
			SIMPLE_COMMA_COORDS_PATTERN,
			GENERIC_POSITION_PATTERN
	);

	private static final Cooldown COOLDOWN = Cooldown.of(10, TimeUnit.SECONDS);

	public ChatPositionFeature() {
		ChatEvents.MESSAGE_RECEIVED.register(this::onMessage);

		addComponent(CommandComponent.class, dispatcher -> {
			dispatcher.register(ClientCommandManager.literal(CaribouStonks.NAMESPACE)
					.then(ClientCommandManager.literal("sharePosition")
							.executes(context -> shareCurrentPosition(context.getSource())))
			);
			dispatcher.register(ClientCommandManager.literal("sendCoords")
					.executes(context -> shareCurrentPosition(context.getSource())));
		});
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& ConfigManager.getConfig().uiAndVisuals.sharedPositionWaypoint.enabled;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onMessage(Text text) {
		if (!isEnabled()) {
			return;
		}

		String message = text.getString();
		if (message.startsWith("[CaribouStonks]")) {
			return;
		}

		for (Pattern pattern : PATTERNS) {
			Matcher matcher = pattern.matcher(message);
			if (matcher.find()) {
				try {
					String playerNameRaw = matcher.namedGroups().containsKey("playerName")
							? matcher.group("playerName") : "";
					String playerName = playerNameRaw.isEmpty()
							? "" : playerNameRaw.substring(playerNameRaw.lastIndexOf(' ') + 1);
					String x = matcher.group("x");
					String y = matcher.group("y");
					String z = matcher.group("z");
					createWaypoint(playerName, x, y, z);
				} catch (Exception ex) {
					CaribouStonks.LOGGER.error("[ChatPositionFeature] Unable to parse a chat waypoint", ex);
				}

				break;
			}
		}
	}

	private int shareCurrentPosition(@NotNull FabricClientCommandSource source) {
		if (COOLDOWN.test()) {
			Position position = Position.of(source.getPosition());
			String area = "";
			if (ConfigManager.getConfig().uiAndVisuals.sharedPositionWaypoint.shareWithArea) {
				area = " | " + SkyBlockAPI.getArea().orElse("");
			}
			String message = position.asChatCoordinates() + area;
			Client.sendChatMessage(message);
		} else {
			source.sendFeedback(CaribouStonks.prefix().get()
					.append(Text.literal("Command on cooldown!").formatted(Formatting.RED)));
		}

		return 1;
	}

	private void createWaypoint(String playerName, String x, String y, String z) {
		if (x.isEmpty() && y.isEmpty() && z.isEmpty()) {
			return;
		}

		try {
			int positionX = Integer.parseInt(x);
			int positionY = Integer.parseInt(y);
			int positionZ = Integer.parseInt(z);
			Position position = Position.of(positionX, positionY, positionZ);

			boolean foundInquisitor = CaribouStonks.features().getFeature(MythologicalRitualFeature.class)
					.onPlayerFoundInquisitor(playerName, position);

			UIAndVisualsConfig.SharedPositionWaypoint config = ConfigManager.getConfig().uiAndVisuals.sharedPositionWaypoint;
			int showTime = foundInquisitor ? 60 : config.showTime;
			Color color = config.rainbow ? Colors.RAINBOW : Color.fromInt(config.color.getRGB());

			Text waypointName = playerName.isEmpty()
					? Text.literal("- ? -").formatted(Formatting.YELLOW, Formatting.BOLD)
					: Text.literal(playerName).formatted(Formatting.AQUA);

			Waypoint.builder(position)
					.color(color)
					.timeout(showTime, TimeUnit.SECONDS)
					.resetBetweenWorlds(true)
					.textOption(TextOption.builder()
							.withText(waypointName)
							.withDistance(true)
							.build())
					.buildAndRegister();
		} catch (Exception ex) { // NumberFormatException
			CaribouStonks.LOGGER.error("[ChatPositionFeature] Unable to create waypoint", ex);
		}
	}
}
