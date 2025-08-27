package fr.siroz.cariboustonks.feature.diana;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.manager.command.CommandComponent;
import fr.siroz.cariboustonks.manager.glowing.EntityGlowProvider;
import fr.siroz.cariboustonks.manager.keybinds.KeyBind;
import fr.siroz.cariboustonks.manager.waypoint.Waypoint;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.keybinds.KeyBindComponent;
import fr.siroz.cariboustonks.manager.waypoint.options.TextOption;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.position.Position;
import fr.siroz.cariboustonks.util.render.WorldRenderUtils;
import fr.siroz.cariboustonks.util.render.WorldRendererProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.TimeUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO : Refactor & Clean up
//  Séparer par feature? Priorité sur la détection du Mythological Ritual et virer le #onDiana pour être
//  remplacé par un Mayor Util.

public final class MythologicalRitualFeature extends Feature implements EntityGlowProvider, WorldRendererProvider {

	private static final Pattern GRIFFIN_BURROW_DUG = Pattern.compile(
			"(?<message>You dug out a Griffin Burrow!|You finished the Griffin burrow chain!) \\((?<index>\\d)/4\\)");
	private static final Pattern INQUISITOR_FOUND_PATTERN = Pattern.compile(".* You dug out a Minos Inquisitor!");
	private static final String INQUISITOR_ENTITY_NAME = "Minos Inquisitor";

	private final GriffinBurrowParticleFinder particleFinder;
	private final GuessBurrow guessBurrow;
	private final NearestWarp nearestWarp;
	private final KeyBind nearestWarpKeyBind;

	private final Map<BlockPos, Waypoint> burrows = new HashMap<>();
	@Nullable
	private Waypoint guessWaypoint;
	@Nullable
	private Position currentInquisitorPosition;

	private boolean onDiana = false;

	public MythologicalRitualFeature() {
		this.particleFinder = new GriffinBurrowParticleFinder(this);
		this.guessBurrow = new GuessBurrow(this);
		this.nearestWarp = new NearestWarp(this);
		this.nearestWarpKeyBind = new KeyBind("Warp Diana", GLFW.GLFW_KEY_F, true, nearestWarp::warpToNearestWarp);

		ChatEvents.MESSAGE_RECEIVED.register(this::onMessage);
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> resetAndClear());
		WorldRenderEvents.AFTER_TRANSLUCENT.register(this::render);

		addComponent(CommandComponent.class, d -> d.register(ClientCommandManager.literal(CaribouStonks.NAMESPACE)
				.then(ClientCommandManager.literal("resetDiana")
						.executes(context -> {
							resetAndClear();
							context.getSource().sendFeedback(CaribouStonks.prefix().get()
									.append(Text.literal("Diana reset successfully.").formatted(Formatting.GREEN)));
							return 1;
						})
				)
		));

		addComponent(KeyBindComponent.class, () -> Collections.singletonList(nearestWarpKeyBind));
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.HUB
				&& ConfigManager.getConfig().events.mythologicalRitual.enabled;
	}

	public boolean onPlayerFoundInquisitor(@Nullable String playerName, @NotNull Position position) {
		if (!isEnabled() || !onDiana) {
			return false;
		}

		if (playerName != null && !playerName.contains(CLIENT.getSession().getUsername()) && isAcceptInquisitorShareEnabled()) {
			currentInquisitorPosition = position;
			nearestWarp.onInquisitor();
			TickScheduler.getInstance().runLater(() -> currentInquisitorPosition = null, 30, TimeUnit.SECONDS);

			Client.sendMessageWithPrefix(Text.literal(playerName).formatted(Formatting.YELLOW, Formatting.BOLD)
					.append(Text.literal(" found an Inquisitor!").formatted(Formatting.GREEN, Formatting.BOLD)));
			Client.playSound(SoundEvents.ENTITY_WITHER_SHOOT, 1f, 1f);
			return true;
		}

		return false;
	}

	@Override
	public int getEntityGlowColor(@NotNull Entity entity) {
		if (ConfigManager.getConfig().events.mythologicalRitual.highlightInquisitor
				&& entity.getName().getString().contains(INQUISITOR_ENTITY_NAME)
		) {
			return ConfigManager.getConfig().events.mythologicalRitual.highlightInquisitorColor.getRGB();
		}

		return DEFAULT;
	}

	@Override
	public void render(WorldRenderContext context) {
		if (CLIENT.player == null || CLIENT.world == null) return;

		if (isGuessEnabled() && guessWaypoint != null) {
			guessWaypoint.getRenderer().render(context);
		}

		if (isParticleFinderEnabled()) {
			for (Waypoint waypoint : burrows.values()) {
				waypoint.getRenderer().render(context);
			}
		}

		if (currentInquisitorPosition != null && isAcceptInquisitorShareEnabled()) {
			double distance = context.camera().getPos().distanceTo(currentInquisitorPosition.toVec3d());
			float scale = Math.max((float) distance / 5, 1);
			WorldRenderUtils.renderText(context,
					Text.empty().append(Text.literal("/K").formatted(Formatting.DARK_RED, Formatting.OBFUSCATED))
							.append(Text.literal(" Inquisitor ").formatted(Formatting.RED, Formatting.BOLD))
							.append(Text.literal("/K").formatted(Formatting.DARK_RED, Formatting.OBFUSCATED)),
					currentInquisitorPosition.toVec3d().add(0, 5, 0),
					scale,
					true
			);
		}

		if (!ConfigManager.getConfig().events.mythologicalRitual.lineToClosestBurrow) {
			return;
		}

		if (guessWaypoint != null && burrows.isEmpty()) {
			WorldRenderUtils.renderLineFromCursor(context, guessWaypoint.getPosition().toVec3d(), Colors.YELLOW, 1f);
		} else if (!burrows.isEmpty() && CLIENT.player != null) {

			Vec3d closest = null;
			double minDistanceSquared = Double.MAX_VALUE;

			List<Vec3d> positions = burrows.values().stream()
					.map(waypoint -> waypoint.getPosition().toVec3d())
					.toList();

			for (Vec3d pos : positions) {
				double distanceSquared = CLIENT.player.getPos().squaredDistanceTo(pos);

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

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onMessage(@NotNull Text text) {
		if (!isEnabled()) {
			return;
		}

		Matcher burrowDugMatcher = GRIFFIN_BURROW_DUG.matcher(text.getString());
		if (burrowDugMatcher.matches()) {
			onDiana = true;
			particleFinder.handleGriffinBurrowDugMessage();
		}

		Matcher inquisitorFoundMatcher = INQUISITOR_FOUND_PATTERN.matcher(text.getString());
		if (inquisitorFoundMatcher.matches()) {
			Client.sendMessageWithPrefix(Text.literal("You found an Inquisitor!").formatted(Formatting.GREEN, Formatting.BOLD));
			Client.showTitle(Text.empty().append(Text.literal("/K").formatted(Formatting.DARK_RED, Formatting.OBFUSCATED))
							.append(Text.literal(" Inquisitor ").formatted(Formatting.RED, Formatting.BOLD))
							.append(Text.literal("/K").formatted(Formatting.DARK_RED, Formatting.OBFUSCATED)),
					1, 80, 20);
			Client.playSound(SoundEvents.ENTITY_WITHER_SPAWN, 0.8f, 1.5f);

			if (CLIENT.player != null && ConfigManager.getConfig().events.mythologicalRitual.shareInquisitor) {
				Position position = Position.of(CLIENT.player.getPos());
				Client.sendChatMessage("/pc " + position.asChatCoordinates());
			}
		}
	}

	boolean isGuessEnabled() {
		return ConfigManager.getConfig().events.mythologicalRitual.guessBurrow;
	}

	boolean isParticleFinderEnabled() {
		return ConfigManager.getConfig().events.mythologicalRitual.burrowParticleFinder;
	}

	String getNearestWarpBoundKeyLocalized() {
		return nearestWarpKeyBind.getKeyBinding().getBoundKeyLocalizedText().getString();
	}

	void onBurrowGuess(@Nullable Vec3d location) {
		if (location == null) return;

		Position position = Position.of(location);

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

	void onBurrowDetected(@Nullable GriffinBurrow burrow) {
		if (burrow == null) return;
		if (burrows.containsKey(burrow.getPos())) return;

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
			Client.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1f);
			burrows.putIfAbsent(burrow.getPos(), waypoint);
		}
	}

	void onBurrowDug(BlockPos pos) {
		burrows.remove(pos);
		// "Useless" je pense depuis le patch du ParticleFinder
		burrows.remove(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
		burrows.remove(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()));
	}

	private boolean isAcceptInquisitorShareEnabled() {
		return ConfigManager.getConfig().events.mythologicalRitual.acceptInquisitorShare;
	}

	private void resetAndClear() {
		guessBurrow.reset();
		onDiana = false;
		guessWaypoint = null;
		burrows.clear();
		particleFinder.resetAndClear();
		nearestWarp.reset();
		currentInquisitorPosition = null;
	}
}
