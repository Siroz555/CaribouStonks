package fr.siroz.cariboustonks.mc.api;

import com.mojang.brigadier.Command;
import fr.siroz.cariboustonks.mc.MinecraftAPI;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides a view of the local player's state.
 * <p>
 * This interface is the single point of access for any player-related data within the mod.
 * Features must never import or reference Minecraft's {@code LocalPlayer} directly.
 * <p>
 * Implementations are responsible for handling the case where the player is not yet available.
 * Every method is guaranteed to return a safe default or return {@code null}.
 *
 * @see MinecraftAPI#client()
 */
public interface ClientContext {

	/**
	 * Checks if the {@code LocalPlayer} is available.
	 * <p>
	 * Identified with:
	 * <pre>
	 *     Minecraft.getInstance().player != null
	 * </pre>
	 *
	 * @return {@code true} if available
	 */
	boolean isAvailable();

	/**
	 * Retrieves the current username of the player.
	 *
	 * @return the player's username, or {@code null} if unavailable
	 */
	@Nullable String getPlayerName();

	/**
	 * Retrieves the current position of the player.
	 * <p>
	 * If the player is not available, returns {@link Vec3#ZERO}.
	 *
	 * @return the player's {@link Vec3}, or {@link Vec3#ZERO} if unavailable
	 */
	@NonNull Vec3 position();

	/**
	 * Retrieves the current block position of the player.
	 * <p>
	 * If the player is not available, returns {@link BlockPos#ZERO}.
	 *
	 * @return the player's {@link BlockPos}, or {@link BlockPos#ZERO} if unavailable
	 * @see #blockPosition(boolean)
	 */
	@NonNull BlockPos blockPosition();

	/**
	 * Retrieves the current position related to the client.
	 * <p>
	 * If {@code crosshairTargetAsBlockPos} is {@code true} and the crosshair is currently targeting a block,
	 * returns the position of the targeted block. Otherwise, returns the player's current block position.
	 * If the client or player is not available, returns {@link BlockPos#ZERO}.
	 *
	 * @param crosshairTargetAsBlockPos if {@code true}, use the block targeted by the crosshair if available
	 * @return the {@link BlockPos} corresponding to the current position
	 * @see #blockPosition()
	 */
	@NonNull BlockPos blockPosition(boolean crosshairTargetAsBlockPos);

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
	 * Retrieves the currently held item in the main hand of the client.
	 *
	 * @return the {@link ItemStack} representing the item currently held in the main hand of the client or {@code null}
	 * @see #getHeldItem()
	 */
	@Nullable ItemStack getMainHandItem();

	/**
	 * Retrieves the currently held item of the client.
	 *
	 * @return the {@link ItemStack} representing the item currently held by the client or {@code null}
	 * @see #getMainHandItem()
	 */
	@Nullable ItemStack getHeldItem();

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
	 * Send a message to the <b>client</b>.
	 *
	 * @param message the message
	 * @see #sendMessageWithPrefix(Component)
	 */
	void sendMessage(@NonNull Component message);

	/**
	 * Send a message to the <b>client</b> with the Mod prefix.
	 *
	 * @param message the message
	 * @see #sendMessage(Component)
	 */
	void sendMessageWithPrefix(@NonNull Component message);

	/**
	 * Send a {@code message} in the chat to the <b>server</b>.
	 *
	 * @param message the message
	 * @see #sendCommandToServer(String, boolean)
	 */
	void sendChatToServer(@NonNull String message, boolean hideToClient);

	/**
	 * Send a {@code command} in the chat to the <b>server</b>.
	 * <p>
	 * The command can be: {@code /pc hello} or {@code pc hello}.
	 *
	 * @param command the command
	 * @see #sendChatToServer(String, boolean)
	 */
	void sendCommandToServer(@NonNull String command, boolean hideToClient);

	/**
	 * Send an error message to the <b>client</b>.
	 * The message contains the Mod prefix and the given message in red format.
	 *
	 * @param errorMessage the error message
	 * @param notification if the message should be displayed in a Toast Notification
	 */
	void sendErrorMessage(@NonNull String errorMessage, boolean notification);

	/**
	 * Display a {@code Title}.
	 *
	 * @param title        the title
	 * @param fadeInTicks  duration in ticks of the title appearance animation (0 to 250)
	 * @param stayTicks    duration in ticks during which the title remains visible (0 to 1000)
	 * @param fadeOutTicks duration in ticks of the title fade-out animation (0 to 250)
	 * @see #showSubtitle(Component, int, int, int) showSubtitle
	 * @see #showTitleAndSubtitle(Component, Component, int, int, int) showTitleAndSubtitle
	 */
	default void showTitle(@NonNull Component title, int fadeInTicks, int stayTicks, int fadeOutTicks) {
		showTitleAndSubtitle(title, Component.empty(), fadeInTicks, stayTicks, fadeOutTicks);
	}

	/**
	 * Display a {@code Subtitle}.
	 *
	 * @param subtitle     the subtitle
	 * @param fadeInTicks  duration in ticks of the title appearance animation (0 to 250)
	 * @param stayTicks    duration in ticks during which the title remains visible (0 to 1000)
	 * @param fadeOutTicks duration in ticks of the title fade-out animation (0 to 250)
	 * @see #showTitle(Component, int, int, int) showTitle
	 * @see #showTitleAndSubtitle(Component, Component, int, int, int) showTitleAndSubtitle
	 */
	default void showSubtitle(@NonNull Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
		showTitleAndSubtitle(Component.empty(), subtitle, fadeInTicks, stayTicks, fadeOutTicks);
	}

	/**
	 * Display a {@code Title} and a {@code Subtitle}.
	 *
	 * @param title        the title
	 * @param subtitle     the subtitle
	 * @param fadeInTicks  duration in ticks of the title appearance animation (0 to 250)
	 * @param stayTicks    duration in ticks during which the title remains visible (0 to 1000)
	 * @param fadeOutTicks duration in ticks of the title fade-out animation (0 to 250)
	 * @see #showTitle(Component, int, int, int) showTitle
	 * @see #showSubtitle(Component, int, int, int) showSubtitle
	 */
	void showTitleAndSubtitle(@NonNull Component title, @NonNull Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks);

	/**
	 * Display a {@code Toast Notification}.
	 *
	 * @param text the text
	 * @param icon the icon
	 */
	void showNotification(@NonNull MutableComponent text, @NonNull ItemStack icon);

	/**
	 * Display a {@code Toast Notification System} with the Mod prefix.
	 *
	 * @param description the text description
	 */
	default void showNotificationSystem(@NonNull String description) {
		showNotificationSystem("CaribouStonks", description);
	}

	/**
	 * Display a {@code Toast Notification System}.
	 *
	 * @param title       the text title
	 * @param description the text description
	 */
	void showNotificationSystem(@NonNull String title, @NonNull String description);

	/**
	 * Display the {@code ErrorScreen} (Fatal Screen).
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param title   the title
	 * @param message the message
	 */
	void showFatalErrorScreen(@NonNull Component title, @NonNull Component message);

	/**
	 * Display a {@link BossEvent} to the client.
	 *
	 * @param bossBar the bossBar to show
	 */
	void showBossBar(@NonNull BossEvent bossBar);

	/**
	 * Remove the given {@link BossEvent} to the client.
	 *
	 * @param bossBar the bossBar to remove
	 */
	void removeBossBar(@NonNull BossEvent bossBar);

	/**
	 * Play a {@code Sound}.
	 *
	 * @param sound the sound
	 */
	default void playSound(@NonNull SoundEvent sound) {
		playSound(sound, 1f, 1f);
	}

	/**
	 * Play a {@code Sound}.
	 *
	 * @param sound  the sound
	 * @param volume the volume
	 * @param pitch  the pitch
	 */
	void playSound(@NonNull SoundEvent sound, float volume, float pitch);

	/**
	 * Play {@code UI_BUTTON_CLICK} sound.
	 */
	void playSoundButtonClickUI();

	/**
	 * Set the given {@code String} to the Client Clipboard.
	 *
	 * @param toClipboard the string
	 */
	void setToClipboard(@NonNull String toClipboard);

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
	 * Determines if the given {@code keyCode} is pressed.
	 * <p>
	 * See {@code GLFW}
	 *
	 * @param keyCode the keyCode to check
	 * @return {@code true} if the keyCode is pressed
	 */
	boolean isKeyPressed(int keyCode);

	/**
	 * Determines if the {@code Shift key} is currently pressed by checking if either
	 * the left or right Shift keys (340 or 344) are pressed.
	 *
	 * @return {@code true} if the Shift key is pressed
	 */
	boolean hasShiftDown();
}
