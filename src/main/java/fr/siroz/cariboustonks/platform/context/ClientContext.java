package fr.siroz.cariboustonks.platform.context;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.Command;
import fr.siroz.cariboustonks.core.infrastructure.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ClientEvents;
import fr.siroz.cariboustonks.platform.mixin.accessors.PlayerTabOverlayAccessor;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides a view of the client's state.
 * <p>
 * Implementations are responsible for handling the case where the client is not yet available.
 * Every method is guaranteed to return a safe default or return {@code null}.
 */
public final class ClientContext {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final List<String> STRING_SCOREBOARD = new ArrayList<>();

	private ClientContext() {
	}

	/**
	 * Init
	 */
	public static void bootstrap() {
		TickScheduler.getInstance().runRepeating(ClientContext::updateScoreboard, 1, TimeUnit.SECONDS);
	}

	/**
	 * Retrieves the current username of the player.
	 *
	 * @return the player's username
	 */
	public static @NonNull String getPlayerName() {
		return CLIENT.getUser().getName();
	}

	/**
	 * Retrieves the current scoreboard lines.
	 *
	 * @return the current scoreboard lines
	 */
	public static @NonNull List<String> getScoreboard() {
		return STRING_SCOREBOARD;
	}

	/**
	 * Retrieves the footer of the tab list.
	 *
	 * @return the footer of the tab list, or {@code null}
	 */
	public static @Nullable String getTabListFooter() {
		Component footer = ((PlayerTabOverlayAccessor) CLIENT.gui.getTabList()).getFooter();
		return footer != null ? footer.getString() : null;
	}

	/**
	 * Returns the current {@link Screen}
	 *
	 * @return the current screen
	 */
	public static @Nullable Screen getScreen() {
		return CLIENT.screen;
	}

	/**
	 * Sets to the client the given {@link Screen}
	 *
	 * @param screen the screen, can be null
	 */
	public static void setScreen(@Nullable Screen screen) {
		CLIENT.setScreen(screen);
	}

	/**
	 * Create a command that queues a screen to be opened in the next tick.
	 * Used to prevent the screen from closing immediately after the command is executed.
	 *
	 * @param screenSupplier the screen supplier
	 * @return {@link Command Command with FabricClientCommandSource}
	 */
	public static @NonNull Command<FabricClientCommandSource> openScreen(@NonNull Supplier<Screen> screenSupplier) {
		return _ -> {
			CLIENT.schedule(() -> CLIENT.setScreen(screenSupplier.get()));
			return Command.SINGLE_SUCCESS;
		};
	}

	/**
	 * Play a sound to the client in {@code UI} interface
	 *
	 * @param sound the sound
	 */
	public static void playSound(@NonNull SoundEvent sound) {
		CLIENT.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
	}

	/**
	 * Returns the {@link Font} of the GUI environment
	 *
	 * @return the {@code Font}
	 */
	public static @NonNull Font getFont() {
		return CLIENT.font;
	}

	/**
	 * Returns the {@link ChatListener}
	 *
	 * @return the {@code ChatListener}
	 */
	public static @NonNull ChatListener getChatListener() {
		return CLIENT.getChatListener();
	}

	/**
	 * Returns the {@link ChatComponent}
	 *
	 * @return the {@code ChatComponent}
	 */
	public static @NonNull ChatComponent getChat() {
		return CLIENT.gui.getChat();
	}

	/**
	 * Handle a Mouse Click within a Container.
	 *
	 * @param containerId the container id
	 * @param slotId      the slot id
	 */
	public static void handleMouseClick(int containerId, int slotId) {
		if (CLIENT.player != null && CLIENT.gameMode != null) {
			CLIENT.gameMode.handleContainerInput(containerId, slotId, 0, ContainerInput.PICKUP, CLIENT.player);
		}
	}

	/**
	 * Returns a view of {@link PlayerInfo} from online players in multiplayer environnement.
	 *
	 * @return a collection, can be empty
	 */
	public static Collection<PlayerInfo> getOnlinePlayers() {
		return CLIENT.getConnection() != null ? CLIENT.getConnection().getOnlinePlayers() : Collections.emptyList();
	}

	/**
	 * Send the given {@link Packet} to the server, if multiplayer environnement is available
	 *
	 * @param packet the packet to send
	 */
	public static void sendPacket(@NonNull Packet<?> packet) {
		if (CLIENT.player != null && CLIENT.level != null && CLIENT.getConnection() != null) {
			CLIENT.getConnection().send(packet);
		}
	}

	/**
	 * Display the {@code ErrorScreen} (Fatal Screen).
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param title   the title
	 * @param message the message
	 */
	public static void showFatalErrorScreen(@NonNull Component title, @NonNull Component message) {
		CLIENT.setScreen(new ErrorScreen(title, message));
	}

	/**
	 * Set the given {@code String} to the Client Clipboard.
	 *
	 * @param toClipboard the string
	 */
	public static void setToClipboard(@NonNull String toClipboard) {
		CLIENT.keyboardHandler.setClipboard(toClipboard);
	}

	/**
	 * Checks if the client is in {@code Singleplayer}
	 *
	 * @return {@code true} if is in Singleplayer Mode
	 */
	public static boolean isLocalServer() {
		return CLIENT.isLocalServer();
	}

	/**
	 * Determines if the given {@code keyCode} is pressed.
	 * <p>
	 * See {@code GLFW}
	 *
	 * @param keyCode the keyCode to check
	 * @return {@code true} if the keyCode is pressed
	 */
	public static boolean isKeyPressed(int keyCode) {
		return InputConstants.isKeyDown(CLIENT.getWindow(), keyCode);
	}

	/**
	 * Determines if the {@code Shift key} is currently pressed by checking if either
	 * the left or right Shift keys (340 or 344) are pressed.
	 *
	 * @return {@code true} if the Shift key is pressed
	 */
	public static boolean hasShiftDown() {
		return InputConstants.isKeyDown(CLIENT.getWindow(), 340) || InputConstants.isKeyDown(CLIENT.getWindow(), 344);
	}

	private static void updateScoreboard() {
		try {
			// Toujours clear, toujours
			STRING_SCOREBOARD.clear();
			// Rien à faire
			if (CLIENT.player == null || CLIENT.level == null) return;

			Scoreboard scoreboard = CLIENT.level.getScoreboard();
			Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BY_ID.apply(1));
			List<String> stringLines = new ArrayList<>();

			for (ScoreHolder scoreHolder : scoreboard.getTrackedPlayers()) {
				if (scoreboard.listPlayerScores(scoreHolder).containsKey(objective)) {
					PlayerTeam team = scoreboard.getPlayersTeam(scoreHolder.getScoreboardName());

					if (team != null) {
						String strLine = team.getPlayerPrefix().getString() + team.getPlayerSuffix().getString();

						if (!strLine.trim().isEmpty()) {
							String formatted = StonksUtils.stripColor(strLine);
							stringLines.add(formatted);
						}
					}
				}
			}

			if (objective != null) {
				stringLines.add(objective.getDisplayName().getString());
				Collections.reverse(stringLines);
			}

			STRING_SCOREBOARD.addAll(stringLines);
			if (SkyBlockAPI.isOnSkyBlock()) {
				ClientEvents.SCOREBOARD_UPDATE_EVENT.invoker().onUpdate(STRING_SCOREBOARD);
			}
		} catch (Exception _) {
		}
	}
}
