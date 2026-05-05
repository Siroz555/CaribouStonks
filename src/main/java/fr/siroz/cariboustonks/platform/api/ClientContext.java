package fr.siroz.cariboustonks.platform.api;

import com.mojang.brigadier.Command;
import fr.siroz.cariboustonks.platform.MinecraftService;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.ContainerInput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides a view of the client's state.
 * <p>
 * This interface is the single point of access for any client-related data within the mod.
 * Features must never import or reference Minecraft's {@code internal code} directly.
 * <p>
 * Implementations are responsible for handling the case where the client is not yet available.
 * Every method is guaranteed to return a safe default or return {@code null}.
 *
 * @see MinecraftService#client()
 */
public interface ClientContext {

	/**
	 * Retrieves the current username of the player.
	 *
	 * @return the player's username, or {@code null} if unavailable
	 */
	@NonNull String getPlayerName();

	/**
	 * Retrieves the current scoreboard lines.
	 *
	 * @return the current scoreboard lines
	 */
	@NonNull List<String> getScoreboard();

	/**
	 * Retrieves the footer of the tab list.
	 *
	 * @return the footer of the tab list, or {@code null}
	 */
	@Nullable String getTabListFooter();

	/**
	 * Sets to the client the given {@link Screen}
	 *
	 * @param screen the screen, can be null
	 */
	void setScreen(@Nullable Screen screen);

	/**
	 * Create a command that queues a screen to be opened in the next tick.
	 * Used to prevent the screen from closing immediately after the command is executed.
	 *
	 * @param screenSupplier the screen supplier
	 * @return {@link Command Command with FabricClientCommandSource}
	 */
	@NonNull Command<FabricClientCommandSource> openScreen(@NonNull Supplier<Screen> screenSupplier);

	/**
	 * Play a sound to the client in {@code UI} interface
	 *
	 * @param sound the sound
	 * @param pitch the pitch
	 */
	void playSound(@NonNull SoundEvent sound, float pitch);

	/**
	 * Returns the {@link Font} of the GUI environment
	 *
	 * @return the {@code Font}
	 */
	@NonNull Font getFont();

	/**
	 * Returns the {@code scaled height} from the window GUI.
	 *
	 * @return the scaled height
	 */
	int getWindowGuiScaledHeight();

	/**
	 * Handle a Mouse Click within a Container.
	 *
	 * @param containerId the container id
	 * @param slotId      the slot id
	 * @param type        the click type
	 */
	void handleMouseClick(int containerId, int slotId, ContainerInput type);

	/**
	 * Returns a view of {@link PlayerInfo} from online players in multiplayer environnement.
	 *
	 * @return a collection, can be empty
	 * @see #isPacketListenerAvailable()
	 */
	Collection<PlayerInfo> getOnlinePlayers();

	/**
	 * Send the given {@link Packet} to the server, if multiplayer environnement is available
	 *
	 * @param packet the packet to send
	 * @see #isPacketListenerAvailable()
	 */
	void sendPacket(@NonNull Packet<?> packet);

	/**
	 * Add to the client chat log the given message
	 *
	 * @param component the component message
	 * @param instant   the instance
	 */
	void addToChatLog(@NonNull Component component, @NonNull Instant instant);

	/**
	 * Queue to the Narrator the given message
	 *
	 * @param component the component message
	 */
	void queueNarrator(@NonNull Component component);

	/**
	 * Returns a collection of {@link GuiMessage} representing chat messages.
	 *
	 * @return a collection of messages
	 */
	@NonNull List<GuiMessage> getChatMessages();

	/**
	 * Returns a collection of {@link GuiMessage.Line} representing the visible chat messages
	 *
	 * @return a collection of messages
	 */
	@NonNull List<GuiMessage.Line> getChatVisibleMessages();

	/**
	 * Returns the number of lines per page of the chat
	 *
	 * @return the count of lines per page
	 */
	int getChatLinesPerPage();

	/**
	 * Returns the {@code Scrollbar} position of the chat
	 *
	 * @return the position
	 */
	int getChatScrollbarPos();

	/**
	 * Returns the {@code scale} of the chat
	 *
	 * @return the scale
	 */
	double getChatScale();

	/**
	 * Return {@code width} of the chat
	 *
	 * @return the width
	 */
	int getChatWidth();

	/**
	 * Returns the {@code height} of the chat line
	 *
	 * @return the height
	 */
	int getChatLineHeight();

	/**
	 * Set the given {@code String} to the Client Clipboard.
	 *
	 * @param toClipboard the string
	 */
	void setToClipboard(@NonNull String toClipboard);

	/**
	 * Checks if the client is in {@code Singleplayer}
	 *
	 * @return {@code true} if is in Singleplayer Mode
	 */
	boolean isLocalServer();

	/**
	 * Determines if the given {@code keyCode} is pressed.
	 * <p>
	 * See {@code GLFW}
	 *
	 * @param keyCode the keyCode to check
	 * @return {@code true} if the keyCode is pressed
	 */
	boolean isKeyPressed(int keyCode);

	/**
	 * Checks if the {@code ClientPacketListener} is available.
	 * <p>
	 * Identified with:
	 * <pre>
	 *     Minecraft.getInstance().getConnection() != null;
	 * </pre>
	 *
	 * @return {@code true} if is available
	 */
	boolean isPacketListenerAvailable();

	/**
	 * Determines if the {@code Shift key} is currently pressed by checking if either
	 * the left or right Shift keys (340 or 344) are pressed.
	 *
	 * @return {@code true} if the Shift key is pressed
	 */
	boolean hasShiftDown();
}
