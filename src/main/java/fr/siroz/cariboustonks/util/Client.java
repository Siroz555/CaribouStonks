package fr.siroz.cariboustonks.util;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.util.render.notification.Notification;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Util lié au {@link MinecraftClient}.
 * <p>
 * Les méthodes sont {@code Safe Client null} ou {@code Safe World null}.
 */
public final class Client {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private Client() {
	}

	/**
	 * Retrieves the current block position of the player.
	 * <p>
	 * If the player is not available, returns {@link BlockPos#ORIGIN}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @return the player's {@link BlockPos}, or {@link BlockPos#ORIGIN} if unavailable
	 * @see #getCurrentPosition(boolean)
	 */
	public static BlockPos getCurrentPosition() {
		return getCurrentPosition(false);
	}

	/**
	 * Retrieves the current position related to the client.
	 * <p>
	 * If {@code crosshairTargetAsBlockPos} is {@code true} and the crosshair is currently targeting a block,
	 * returns the position of the targeted block. Otherwise, returns the player's current block position.
	 * If the client or player is not available, returns {@link BlockPos#ORIGIN}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param crosshairTargetAsBlockPos if {@code true}, use the block targeted by the crosshair if available
	 * @return the {@link BlockPos} corresponding to the current position
	 * @see #getCurrentPosition()
	 */
	public static BlockPos getCurrentPosition(boolean crosshairTargetAsBlockPos) {
		if (CLIENT.player == null) return BlockPos.ORIGIN;

		if (crosshairTargetAsBlockPos
				&& CLIENT.crosshairTarget instanceof BlockHitResult blockHitResult
				&& CLIENT.crosshairTarget.getType() == HitResult.Type.BLOCK) {
			return blockHitResult.getBlockPos();
		}

		return CLIENT.player.getBlockPos();
	}

	/**
	 * Envoi un message au client.
	 * <p>
	 * {@code Client -> Client} & {@code Safe Client null}
	 *
	 * @param message le message
	 * @see #sendMessageWithPrefix(Text)
	 */
	public static void sendMessage(@NotNull Text message) {
		sendMessageInternal(message, false);
	}

	/**
	 * Envoi un message au client.
	 * <p>
	 * {@code Client -> Client} & {@code Safe Client null}
	 *
	 * @param message le message
	 * @see #sendMessage(Text)
	 */
	public static void sendMessageWithPrefix(@NotNull Text message) {
		sendMessageInternal(CaribouStonks.prefix().get().append(message), false);
	}

	/**
	 * Envoi un message au client sous la forme d'erreur. Le message contient le prefix et est coloré en rouge.
	 * <p>
	 * {@code Client -> Client} & {@code Safe Client null}
	 *
	 * @param errorMessage le message d'erreur
	 * @param notification si le message est également affiché dans un Toast de type System
	 */
	public static void sendErrorMessage(@NotNull String errorMessage, boolean notification) {
		CaribouStonks.LOGGER.warn("Chat error message sent: {}", errorMessage);
		sendMessageInternal(CaribouStonks.prefix().get()
				.append(Text.literal(errorMessage).formatted(Formatting.RED)), false);

		if (notification) {
			Notification.showSystem(errorMessage);
			CLIENT.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_WITHER_HURT, 0.5f, 2f));
		}
	}

	/**
	 * Afficher une {@code Auction Bar}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param message le message
	 */
	public static void showActionBar(@NotNull Text message) {
		sendMessageInternal(message, true);
	}

	@ApiStatus.Internal
	private static void sendMessageInternal(@NotNull Text message, boolean overlay) {
		if (CLIENT.player != null) CLIENT.player.sendMessage(message, overlay);
	}

	/**
	 * Afficher un {@code Title}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param title        le title
	 * @param fadeInTicks  durée en ticks de l'animation d'apparition du title (0 à 250).
	 * @param stayTicks    durée en ticks pendant laquelle le title reste visible (0 à 1000)
	 * @param fadeOutTicks durée en ticks de l'animation de disparition du title (0 à 250)
	 * @see #showSubtitle(Text, int, int, int) showSubtitle
	 * @see #showTitleAndSubtitle(Text, Text, int, int, int) showTitleAndSubtitle
	 */
	public static void showTitle(
			@NotNull Text title,
			@Range(from = 0, to = 250) int fadeInTicks,
			@Range(from = 0, to = 1000) int stayTicks,
			@Range(from = 0, to = 250) int fadeOutTicks
	) {
		showTitleAndSubtitle(title, Text.empty(), fadeInTicks, stayTicks, fadeOutTicks);
	}

	/**
	 * Afficher un {@code Subtitle}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param subtitle     le subtitle
	 * @param fadeInTicks  durée en ticks de l'animation d'apparition du subtitle (0 à 250).
	 * @param stayTicks    durée en ticks pendant laquelle le subtitle reste visible (0 à 1000)
	 * @param fadeOutTicks durée en ticks de l'animation de disparition du subtitle (0 à 250)
	 * @see #showTitle(Text, int, int, int) showTitle
	 * @see #showTitleAndSubtitle(Text, Text, int, int, int) showTitleAndSubtitle
	 */
	public static void showSubtitle(
			@NotNull Text subtitle,
			@Range(from = 0, to = 250) int fadeInTicks,
			@Range(from = 0, to = 1000) int stayTicks,
			@Range(from = 0, to = 250) int fadeOutTicks
	) {
		showTitleAndSubtitle(Text.empty(), subtitle, fadeInTicks, stayTicks, fadeOutTicks);
	}

	/**
	 * Afficher un {@code Title} et un {@code Subtitle}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param title        le title
	 * @param subtitle     le subtitle
	 * @param fadeInTicks  durée en ticks de l'animation d'apparition du title/subtitle (0 à 250).
	 * @param stayTicks    durée en ticks pendant laquelle le title/subtitle reste visible (0 à 1000)
	 * @param fadeOutTicks durée en ticks de l'animation de disparition du title/subtitle (0 à 250)
	 * @see #showTitle(Text, int, int, int) showTitle
	 * @see #showSubtitle(Text, int, int, int) showSubtitle
	 */
	public static void showTitleAndSubtitle(
			@NotNull Text title,
			@NotNull Text subtitle,
			@Range(from = 0, to = 250) int fadeInTicks,
			@Range(from = 0, to = 1000) int stayTicks,
			@Range(from = 0, to = 250) int fadeOutTicks
	) {
		if (CLIENT.player != null) {
			CLIENT.inGameHud.setTitleTicks(fadeInTicks, stayTicks, fadeOutTicks);
			CLIENT.inGameHud.setTitle(title);
			CLIENT.inGameHud.setSubtitle(subtitle);
		}
	}

	/**
	 * Envoi un {@code message} ou une {@code commande} dans le chat.
	 * <p>
	 * {@code Client -> Server} & {@code Safe Client null}
	 *
	 * @param message le message de chat/commande à envoyer
	 */
	public static void sendChatMessage(@NotNull String message) {
		sendChatMessage(message, false);
	}

	/**
	 * Envoi un {@code message} ou une {@code commande} dans le chat.
	 * <p>
	 * {@code Client -> Server} & {@code Safe Client null}
	 *
	 * @param message      le message de chat/commande à envoyer
	 * @param hideToClient si le message/commande sera caché coté client
	 */
	public static void sendChatMessage(@NotNull String message, boolean hideToClient) {
		if (CLIENT.player != null) {
			message = StringHelper.truncateChat(StringUtils.normalizeSpace(message.trim()));

			if (!hideToClient) {
				CLIENT.inGameHud.getChatHud().addToMessageHistory(message);
			}

			if (message.startsWith("/")) {
				CLIENT.player.networkHandler.sendCommand(message.substring(1));
			} else {
				CLIENT.player.networkHandler.sendChatMessage(message);
			}
		}
	}

	public static void playSound(@NotNull SoundEvent sound, float volume, float pitch) {
		if (CLIENT.player != null) CLIENT.player.playSound(sound, volume, pitch);
	}

	/**
	 * Jouer le son {@code ENTITY_EXPERIENCE_ORB_PICKUP}.
	 * <p>
	 * {@code Safe Client null}
	 */
	public static void playSoundNotificationOrb() {
		if (CLIENT.player != null) CLIENT.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 0.1f);
	}

	/**
	 * Jouer le son {@code BLOCK_NOTE_BLOCK_CHIME}.
	 * <p>
	 * {@code Safe Client null}
	 */
	public static void playSoundNotificationChime() {
		if (CLIENT.player != null) CLIENT.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 2f, 1); // 1.2
	}

	/**
	 * Jouer le son {@code UI_BUTTON_CLICK}.
	 */
	public static void playSoundButtonClickUI() {
		CLIENT.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
}
