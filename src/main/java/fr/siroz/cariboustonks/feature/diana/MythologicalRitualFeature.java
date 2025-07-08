package fr.siroz.cariboustonks.feature.diana;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.manager.command.CommandRegistration;
import fr.siroz.cariboustonks.manager.keybinds.KeyBind;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.keybinds.KeyBindRegistration;
import fr.siroz.cariboustonks.manager.waypoint.options.TextOption;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.position.Position;
import fr.siroz.cariboustonks.util.render.WorldRenderUtils;
import fr.siroz.cariboustonks.util.render.WorldRenderer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@ApiStatus.Experimental
public final class MythologicalRitualFeature extends Feature
		implements KeyBindRegistration, CommandRegistration, WorldRenderer { // TODO

	static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final Pattern GRIFFIN_BURROW_DUG = Pattern.compile(
			"(?<message>You dug out a Griffin Burrow!|You finished the Griffin burrow chain!) \\((?<index>\\d)/4\\)");
	private static final Pattern INQUISITOR_FOUND_PATTERN = Pattern.compile(".* You dug out a Minos Inquisitor!");

	private final GriffinBurrowParticleFinder particleFinder;
	private final GuessBurrow guessBurrow;
	private final NearestWarp nearestWarp;

	private final Map<BlockPos, Waypoint> burrows = new HashMap<>();
	private Waypoint guessWaypoint;

	private boolean onDiana = false;

	public MythologicalRitualFeature() {
		this.particleFinder = new GriffinBurrowParticleFinder(this);
		this.guessBurrow = new GuessBurrow(this);
		this.nearestWarp = new NearestWarp();

		ChatEvents.MESSAGE_RECEIVED.register(this::onMessage);
		ClientReceiveMessageEvents.GAME.register(this::onChatMessage);
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> onWorldChange());
		WorldRenderEvents.AFTER_TRANSLUCENT.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.HUB
				&& ConfigManager.getConfig().events.mythologicalRitual.enabled
				&& CLIENT.world != null
				&& CLIENT.player != null;
	}

	public void onPlayerFoundInquisitor(@Nullable String playerName) {
		if (!isEnabled() || !onDiana) {
			return;
		}

		if (playerName != null && !playerName.contains(CLIENT.getSession().getUsername())) {
			Client.sendMessageWithPrefix(Text.literal(playerName).formatted(Formatting.YELLOW, Formatting.BOLD)
					.append(Text.literal(" found an Inquisitor!").formatted(Formatting.GREEN, Formatting.BOLD)));
			Client.playSound(SoundEvents.ENTITY_WITHER_SHOOT, 1f, 1f);
		}
	}

	@Override
	public @NotNull List<KeyBind> registerKeyBinds() {
		return List.of(new KeyBind("Warp Diana", GLFW.GLFW_KEY_F, true, nearestWarp::warpToNearestWarp));
	}

	@Override
	public void register(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher, @NotNull CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal(CaribouStonks.NAMESPACE)
				.then(ClientCommandManager.literal("resetDiana")
						.executes(context -> {
							onWorldChange();
							context.getSource().sendFeedback(CaribouStonks.prefix().get()
									.append(Text.literal("Diana reset success.").formatted(Formatting.GREEN)));
							return Command.SINGLE_SUCCESS;
						})
				)
		);
	}

	@Override
	public void render(WorldRenderContext context) {
		if (isGuessEnabled() && guessWaypoint != null) {
			guessWaypoint.getRenderer().render(context);
		}

		if (isParticleFinderEnabled()) {
			for (Waypoint waypoint : burrows.values()) {
				waypoint.getRenderer().render(context);
			}
		}

		if (!ConfigManager.getConfig().events.mythologicalRitual.lineToClosestBurrow) {
			return;
		}

		if (guessWaypoint != null && burrows.isEmpty()) {
			WorldRenderUtils.renderLineFromCursor(context, guessWaypoint.getPosition().toVec3d(), Colors.YELLOW, 1f);
		} else if (!burrows.isEmpty() && MinecraftClient.getInstance().player != null) {

			Vec3d closest = null;
			double minDistanceSquared = Double.MAX_VALUE;

			List<Vec3d> positions = burrows.values().stream()
					.map(waypoint -> waypoint.getPosition().toVec3d())
					.toList();

			for (Vec3d pos : positions) {
				double distanceSquared = MinecraftClient.getInstance().player.getPos().squaredDistanceTo(pos);

				if (distanceSquared < minDistanceSquared) {
					minDistanceSquared = distanceSquared;
					closest = pos;
				}
			}

			if (closest != null && minDistanceSquared < 25600) {

				Waypoint waypoint = burrows.get(new BlockPos((int) closest.x, (int) closest.y, (int) closest.z));
				if (waypoint != null) {
					WorldRenderUtils.renderLineFromCursor(context, closest.add(0.5f, 0.5f, 0.5f), waypoint.getColor(), 1f);
				}
			}
		}
	}

	private void onWorldChange() {
		guessBurrow.reset();
		onDiana = false;
		guessWaypoint = null;
		burrows.clear();
		particleFinder.onWorldChange();
		nearestWarp.reset();
	}

	private void onMessage(@NotNull Text text) {
		if (!isEnabled()) {
			return;
		}

		if (INQUISITOR_FOUND_PATTERN.matcher(text.getString()).matches()) {
			if (CLIENT.player != null) {
				Client.sendMessageWithPrefix(Text.literal("You found an Inquisitor!").formatted(Formatting.GREEN, Formatting.BOLD));
				Client.showTitle(Text.literal("/K").formatted(Formatting.DARK_RED, Formatting.OBFUSCATED)
								.append(Text.literal(" INQUISITOR ").formatted(Formatting.RED))
								.append(Text.literal("/K").formatted(Formatting.DARK_RED, Formatting.OBFUSCATED)),
						1, 80, 20);
				Client.playSound(SoundEvents.ENTITY_WITHER_SPAWN, 0.8f, 1.5f);

				if (ConfigManager.getConfig().events.mythologicalRitual.shareInquisitor) {
					Position position = Position.of(CLIENT.player.getPos());
					Client.sendChatMessage("/pc " + position.asChatCoordinates());
				}
			}
		}
	}

	private void onChatMessage(Text text, boolean overlay) {
		if (!isEnabled()) {
			return;
		}

		if (GRIFFIN_BURROW_DUG.matcher(text.getString()).matches()) {
			onDiana = true;
			particleFinder.onChatMessage();
		}
	}

	boolean isGuessEnabled() {
		return ConfigManager.getConfig().events.mythologicalRitual.guessBurrow;
	}

	boolean isParticleFinderEnabled() {
		return ConfigManager.getConfig().events.mythologicalRitual.burrowParticleFinder;
	}

	void onBurrowGuess(Vec3d location) {
		if (location == null) return;

		Position position = Position.of((int) location.getX(), (int) location.getY(), (int) location.getZ());

		guessWaypoint = Waypoint.builder(position)
				.textOption(TextOption.builder()
						.withText(Text.literal("Guess").formatted(Formatting.YELLOW, Formatting.BOLD, Formatting.ITALIC))
						.withDistance(true)
						.withOffsetY(3)
						.build())
				.color(Color.fromInt(ConfigManager.getConfig().events.mythologicalRitual.guessBurrowColor.getRGB()))
				.build();

		if (ConfigManager.getConfig().events.mythologicalRitual.nearestWarp) {
			nearestWarp.shouldUseNearestWarp(position.toVec3d());
		}
	}

	void onBurrowDetected(GriffinBurrow burrow) {
		if (burrow == null) {
			return;
		}

		if (burrows.containsKey(burrow.getPos())) {
			return;
		}

		Position position = Position.of(burrow.getPos());
		Waypoint waypoint = switch (burrow.getBurrowType()) {
			case START -> Waypoint.builder(Position.of(position))
					.textOption(TextOption.builder()
							.withText(Text.literal("Start").formatted(Formatting.GREEN, Formatting.BOLD))
							.withOffsetY(3)
							.build())
					.color(Colors.GREEN)
					.build();
			case TREASURE -> Waypoint.builder(Position.of(position))
					.textOption(TextOption.builder()
							.withText(Text.literal("Treasure").formatted(Formatting.GOLD, Formatting.BOLD))
							.withOffsetY(3)
							.build())
					.color(Colors.ORANGE)
					.build();
			case MOB -> Waypoint.builder(Position.of(position))
					.textOption(TextOption.builder()
							.withText(Text.literal("Mob").formatted(Formatting.RED, Formatting.BOLD))
							.withOffsetY(3)
							.build())
					.color(Colors.RED)
					.build();
			case UNKNOWN -> null;
		};

		if (waypoint != null) {
			if (CLIENT.player != null) {
				CLIENT.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1f);
			}

			burrows.putIfAbsent(burrow.getPos(), waypoint);
		}
	}

	void onBurrowDug(BlockPos pos) {
		burrows.remove(pos);
		burrows.remove(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
		burrows.remove(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()));
	}
}
