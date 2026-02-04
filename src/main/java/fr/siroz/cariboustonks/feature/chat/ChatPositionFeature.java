package fr.siroz.cariboustonks.feature.chat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.config.configs.UIAndVisualsConfig;
import fr.siroz.cariboustonks.core.component.CommandComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.module.waypoint.options.TextOption;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.cooldown.Cooldown;
import fr.siroz.cariboustonks.util.position.Position;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

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

		this.addComponent(CommandComponent.class, CommandComponent.builder()
				.namespaced("sendCoords", ctx -> {
					return shareCurrentPosition(ctx.getSource());
				})
				.standalone("sendCoords", builder -> {
					builder.executes(ctx -> shareCurrentPosition(ctx.getSource()));
				})
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& ConfigManager.getConfig().uiAndVisuals.sharedPositionWaypoint.enabled;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onMessage(Component text) {
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
					CaribouStonks.LOGGER.error("{} Unable to parse a chat waypoint", getShortName(), ex);
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
			Client.sendChatToServer(message, false);
		} else {
			source.sendFeedback(CaribouStonks.prefix().get()
					.append(Component.literal("Command on cooldown!").withStyle(ChatFormatting.RED)));
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

			UIAndVisualsConfig.SharedPositionWaypoint config = ConfigManager.getConfig().uiAndVisuals.sharedPositionWaypoint;
			int showTime = config.showTime;
			Color color = config.rainbow ? Colors.RAINBOW : Color.fromInt(config.color.getRGB());

			Component waypointName = playerName.isEmpty()
					? Component.literal("- ? -").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
					: Component.literal(playerName).withStyle(ChatFormatting.AQUA);

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
			CaribouStonks.LOGGER.error("{} Unable to create waypoint", getShortName(), ex);
		}
	}
}
