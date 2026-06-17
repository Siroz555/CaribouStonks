package fr.siroz.cariboustonks.platform.context;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.platform.rendering.gui.element.StonksToast;
import fr.siroz.cariboustonks.util.DeveloperTools;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Provides a view of the local player's state.
 * <p>
 * Implementations are responsible for handling the case where the player is not yet available.
 * Every method is guaranteed to return a safe default or return {@code null}.
 */
public final class PlayerContext {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final SystemToast.SystemToastId STONKS_SYSTEM = new SystemToast.SystemToastId(10000L);

	private PlayerContext() {
	}

	/**
	 * Init
	 */
	public static void bootstrap() {
	}

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
	public static boolean isAvailable() {
		return CLIENT.player != null;
	}

	/**
	 * Retrieves the current position of the player.
	 * <p>
	 * If the player is not available, returns {@link Vec3#ZERO}.
	 *
	 * @return the player's {@link Vec3}, or {@link Vec3#ZERO} if unavailable
	 */
	public static @NonNull Vec3 position() {
		return isAvailable() ? CLIENT.player.position() : Vec3.ZERO;
	}

	/**
	 * Retrieves the current position related to the client.
	 * <p>
	 * If {@code crosshairTargetAsBlockPos} is {@code true} and the crosshair is currently targeting a block,
	 * returns the position of the targeted block. Otherwise, returns the player's current block position.
	 * If the client or player is not available, returns {@link BlockPos#ZERO}.
	 *
	 * @param crosshairTargetAsBlockPos if {@code true}, use the block targeted by the crosshair if available
	 * @return the {@link BlockPos} corresponding to the current position
	 */
	public static @NonNull BlockPos blockPosition(boolean crosshairTargetAsBlockPos) {
		if (!isAvailable()) return BlockPos.ZERO;

		if (crosshairTargetAsBlockPos
				&& CLIENT.hitResult instanceof BlockHitResult blockHitResult
				&& CLIENT.hitResult.getType() == HitResult.Type.BLOCK) {
			return blockHitResult.getBlockPos();
		}

		return CLIENT.player.blockPosition();
	}

	/**
	 * Returns the offset from the player position to their eyes
	 *
	 * @return the offset float
	 */
	public static float getEyeHeight() {
		if (!isAvailable()) return 1.62f;
		if (!CLIENT.player.isShiftKeyDown()) return 1.62f;
		return 1.27f;
	}

	/**
	 * Retrieves the currently held item in the main hand of the client.
	 *
	 * @return the {@link ItemStack} representing the item currently held in the main hand of the client or {@code null}
	 * @see #getHeldItem()
	 */
	public static @Nullable ItemStack getMainHandItem() {
		return isAvailable() ? CLIENT.player.getMainHandItem() : null;
	}

	/**
	 * Retrieves the currently held item of the client.
	 *
	 * @return the {@link ItemStack} representing the item currently held by the client or {@code null}
	 * @see #getMainHandItem()
	 */
	public static @Nullable ItemStack getHeldItem() {
		return isAvailable() ? CLIENT.player.getInventory().getSelectedItem() : null;
	}

	/**
	 * Send a message to the <b>client</b>.
	 *
	 * @param message the message
	 * @see #sendMessageWithPrefix(Component)
	 */
	public static void sendMessage(@NonNull Component message) {
		sendMessageInternal(message);
	}

	/**
	 * Send a message to the <b>client</b> with the Mod prefix.
	 *
	 * @param message the message
	 * @see #sendMessage(Component)
	 */
	public static void sendMessageWithPrefix(@NonNull Component message) {
		sendMessageInternal(CaribouStonks.prefix().get().append(message));
	}

	/**
	 * Send a {@code message} in the chat to the <b>server</b>.
	 *
	 * @param message the message
	 * @see #sendCommandToServer(String, boolean)
	 */
	public static void sendChatToServer(@NonNull String message, boolean hideToClient) {
		sendToServerInternal(message, hideToClient, false);
	}

	/**
	 * Send a {@code command} in the chat to the <b>server</b>.
	 * <p>
	 * The command can be: {@code /pc hello} or {@code pc hello}.
	 *
	 * @param command the command
	 * @see #sendChatToServer(String, boolean)
	 */
	public static void sendCommandToServer(@NonNull String command, boolean hideToClient) {
		sendToServerInternal(command, hideToClient, true);
	}

	/**
	 * Send an error message to the <b>client</b>.
	 * The message contains the Mod prefix and the given message in red format.
	 *
	 * @param errorMessage the error message
	 * @param notification if the message should be displayed in a Toast Notification
	 */
	public static void sendErrorMessage(@NonNull String errorMessage, boolean notification) {
		CaribouStonks.LOGGER.warn("Chat error message sent: {}", errorMessage);
		sendMessageInternal(CaribouStonks.prefix().get()
				.append(Component.literal(errorMessage).withStyle(ChatFormatting.RED)));

		if (notification) {
			showNotificationSystem(errorMessage);
		}
	}

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
	public static void showTitle(@NonNull Component title, int fadeInTicks, int stayTicks, int fadeOutTicks) {
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
	public static void showSubtitle(@NonNull Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
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
	public static void showTitleAndSubtitle(@NonNull Component title, @NonNull Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
		if (isAvailable()) {
			CLIENT.gui.hud.setTimes(fadeInTicks, stayTicks, fadeOutTicks);
			CLIENT.gui.hud.setTitle(title);
			CLIENT.gui.hud.setSubtitle(subtitle);
		}
	}

	/**
	 * Display a {@code Toast Notification}.
	 *
	 * @param text the text
	 * @param icon the icon
	 */
	public static void showNotification(@NonNull MutableComponent text, @NonNull ItemStack icon) {
		CLIENT.gui.toastManager().addToast(new StonksToast(text, icon));
	}

	/**
	 * Display a {@code Toast Notification System} with the Mod prefix.
	 *
	 * @param description the text description
	 */
	public static void showNotificationSystem(@NonNull String description) {
		showNotificationSystem("CaribouStonks", description);
	}

	/**
	 * Display a {@code Toast Notification System}.
	 *
	 * @param title       the text title
	 * @param description the text description
	 */
	public static void showNotificationSystem(@NonNull String title, @NonNull String description) {
		SystemToast systemToast = new SystemToast(STONKS_SYSTEM, Component.literal(title), Component.literal(description));
		CLIENT.gui.toastManager().addToast(systemToast);
	}

	/**
	 * Display a {@link BossEvent} to the client.
	 *
	 * @param bossBar the bossBar to show
	 */
	public static void showBossBar(@NonNull BossEvent bossBar) {
		if (isAvailable()) {
			try {
				CLIENT.gui.hud.getBossOverlay().update(ClientboundBossEventPacket.createAddPacket(bossBar));
			} catch (Exception ex) {
				if (DeveloperTools.isInDevelopment()) {
					CaribouStonks.LOGGER.error("Unable to update bossBar handler (ADD)", ex);
				}
			}
		}
	}

	/**
	 * Remove the given {@link BossEvent} to the client.
	 *
	 * @param bossBar the bossBar to remove
	 */
	public static void removeBossBar(@NonNull BossEvent bossBar) {
		if (isAvailable()) {
			try {
				CLIENT.gui.hud.getBossOverlay().update(ClientboundBossEventPacket.createRemovePacket(bossBar.getId()));
			} catch (Exception ex) {
				if (DeveloperTools.isInDevelopment()) {
					CaribouStonks.LOGGER.error("Unable to update bossBar handler (REMOVE)", ex);
				}
			}
		}
	}

	/**
	 * Play a {@code Sound}.
	 *
	 * @param sound the sound
	 */
	public static void playSound(@NonNull SoundEvent sound) {
		playSound(sound, 1f, 1f);
	}

	/**
	 * Play a {@code Sound}.
	 *
	 * @param sound  the sound
	 * @param volume the volume
	 * @param pitch  the pitch
	 */
	public static void playSound(@NonNull SoundEvent sound, float volume, float pitch) {
		if (isAvailable()) {
			CLIENT.player.playSound(sound, volume, pitch);
		}
	}

	private static void sendMessageInternal(@NonNull Component message) {
		if (isAvailable()) {
			CLIENT.player.sendSystemMessage(message);
		}
	}

	private static void sendToServerInternal(@NonNull String content, boolean hideToClient, boolean command) {
		if (isAvailable()) {
			content = StringUtil.trimChatMessage(StringUtils.normalizeSpace(content.trim()));

			if (!hideToClient) {
				CLIENT.gui.hud.getChat().addRecentChat(content);
			}

			if (command) {
				content = content.startsWith("/") ? content.substring(1) : content;
				CLIENT.player.connection.sendCommand(content);
			} else {
				CLIENT.player.connection.sendChat(content);
			}
		}
	}
}
