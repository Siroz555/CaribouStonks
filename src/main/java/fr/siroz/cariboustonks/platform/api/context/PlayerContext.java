package fr.siroz.cariboustonks.platform.api.context;

import fr.siroz.cariboustonks.platform.MinecraftService;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
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
 * @see MinecraftService#player()
 */
public interface PlayerContext {

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
	 * Returns the {@link LocalPlayer}
	 *
	 * @return the {@code LocalPlayer} instance
	 */
	@Nullable LocalPlayer asLocalPlayer();

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
	 * Returns the player {@link HitResult} if available
	 *
	 * @return the {@code HitResult}
	 */
	@Nullable HitResult getHitResult();

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
}
