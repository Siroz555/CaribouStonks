package fr.siroz.cariboustonks.features.chat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.configs.UIAndVisualsConfig;
import fr.siroz.cariboustonks.core.component.CommandComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Color;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.cooldown.Cooldown;
import fr.siroz.cariboustonks.core.module.position.Position;
import fr.siroz.cariboustonks.core.module.waypoint.Waypoint;
import fr.siroz.cariboustonks.core.module.waypoint.options.TextOption;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.util.Client;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onMessage);

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
		return SkyBlockAPI.isOnSkyBlock() && this.config().uiAndVisuals.sharedPositionWaypoint.enabled;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onMessage(Component text) {
		if (!isEnabled()) return;

		String message = text.getString();
		if (message.startsWith("[CaribouStonks]")) return;

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

	private int shareCurrentPosition(FabricClientCommandSource source) {
		if (COOLDOWN.test()) {
			Position position = Position.of(source.getPosition());
			String area = "";
			if (this.config().uiAndVisuals.sharedPositionWaypoint.shareWithArea) {
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
		if (x.isEmpty() && y.isEmpty() && z.isEmpty()) return;

		try {
			int positionX = Integer.parseInt(x);
			int positionY = Integer.parseInt(y);
			int positionZ = Integer.parseInt(z);
			Position position = Position.of(positionX, positionY, positionZ);

			Component waypointName = Component.literal("- ? -").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
			if (!playerName.isEmpty()) {
				waypointName = Objects.requireNonNullElseGet(
						getTabListName(playerName),
						() -> Component.literal(playerName).withStyle(ChatFormatting.AQUA)
				);
			}

			UIAndVisualsConfig.SharedPositionWaypoint config = this.config().uiAndVisuals.sharedPositionWaypoint;

			Waypoint.builder(position)
					.type(config.type)
					.color(config.rainbow ? Colors.RAINBOW : Color.fromInt(config.color.getRGB()))
					.timeout(config.showTime, TimeUnit.SECONDS)
					.resetBetweenWorlds(true)
					.textOption(TextOption.builder()
							.withText(waypointName)
							.scaleAdjustment(5)
							.withDistance(true)
							.build())
					.buildAndRegister();
		} catch (Exception ex) { // NumberFormatException
			CaribouStonks.LOGGER.error("{} Unable to create waypoint", getShortName(), ex);
		}
	}

	private @Nullable Component getTabListName(String playerName) {
		if (CLIENT.getConnection() == null) return null;

		for (PlayerInfo playerInfo : CLIENT.getConnection().getOnlinePlayers()) {
			String profileName = playerInfo.getProfile().name();
			if (profileName != null && profileName.equals(playerName)) {
				return getNameForDisplay(playerInfo, profileName);
			}
		}

		return null;
	}

	/**
	 * Import depuis PlayerTabOverlay
	 */
	private Component getNameForDisplay(@NonNull PlayerInfo playerInfo, String profileName) {
		return playerInfo.getTabListDisplayName() != null
				? playerInfo.getTabListDisplayName().copy()
				: PlayerTeam.formatNameForTeam(playerInfo.getTeam(), Component.literal(profileName));
	}
}
