package fr.siroz.cariboustonks.util;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.HudEvents;
import fr.siroz.cariboustonks.mixin.accessors.PlayerTabOverlayAccessor;
import fr.siroz.cariboustonks.util.render.gui.StonksToast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.lwjgl.glfw.GLFW;

/**
 * Util lié au {@link Minecraft}.
 * <p>
 * Les méthodes sont {@code Safe Client null} ou {@code Safe World null}.
 */
public final class Client {

	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final SystemToast.SystemToastId STONKS_SYSTEM = new SystemToast.SystemToastId(10000L); // 10000L
	private static final List<String> STRING_SCOREBOARD = new ArrayList<>();
	private static final List<String> STRING_TAB = new ArrayList<>();

	private Client() {
	}

	/**
	 * Retrieves the current username of the player.
	 *
	 * @return the player's username, or {@code null} if unavailable
	 */
	public static @Nullable String getPlayerName() {
		return CLIENT.player != null ? CLIENT.player.getName().getString() : null;
	}

	/**
	 * Determines if the given {@code keyCode} is pressed.
	 * <p>
	 * See {@link GLFW}
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

	/**
	 * Retrieves the current block position of the player.
	 * <p>
	 * If the player is not available, returns {@link BlockPos#ZERO}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @return the player's {@link BlockPos}, or {@link BlockPos#ZERO} if unavailable
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
	 * If the client or player is not available, returns {@link BlockPos#ZERO}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param crosshairTargetAsBlockPos if {@code true}, use the block targeted by the crosshair if available
	 * @return the {@link BlockPos} corresponding to the current position
	 * @see #getCurrentPosition()
	 */
	public static BlockPos getCurrentPosition(boolean crosshairTargetAsBlockPos) {
		if (CLIENT.player == null) return BlockPos.ZERO;

		if (crosshairTargetAsBlockPos
				&& CLIENT.hitResult instanceof BlockHitResult blockHitResult
				&& CLIENT.hitResult.getType() == HitResult.Type.BLOCK) {
			return blockHitResult.getBlockPos();
		}

		return CLIENT.player.blockPosition();
	}

	/**
	 * Retrieves the current scoreboard lines.
	 *
	 * @return the current scoreboard lines
	 */
	@Contract(value = " -> new", pure = true)
	public static @NotNull List<String> getScoreboard() {
		return new ArrayList<>(STRING_SCOREBOARD);
	}

	/**
	 * Retrieves the current tab list lines.
	 *
	 * @return the current tab list lines
	 */
	@SuppressWarnings("unused")
	@Contract(value = " -> new", pure = true)
	public static @NotNull List<String> getTabList() {
		return new ArrayList<>(STRING_TAB);
	}

	/**
	 * Retrieves the footer of the tab list.
	 *
	 * @return the footer of the tab list, or {@code null}
	 */
	public static @Nullable String getTabListFooter() {
		Component footer = ((PlayerTabOverlayAccessor) Minecraft.getInstance().gui.getTabList()).getFooter();
		return footer != null ? footer.getString() : null;
	}

	/**
	 * Envoi un message au client.
	 * <p>
	 * {@code Client -> Client} & {@code Safe Client null}
	 *
	 * @param message le message
	 * @see #sendMessageWithPrefix(Component)
	 */
	public static void sendMessage(@NotNull Component message) {
		sendMessageInternal(message, false);
	}

	/**
	 * Envoi un message au client.
	 * <p>
	 * {@code Client -> Client} & {@code Safe Client null}
	 *
	 * @param message le message
	 * @see #sendMessage(Component)
	 */
	public static void sendMessageWithPrefix(@NotNull Component message) {
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
				.append(Component.literal(errorMessage).withStyle(ChatFormatting.RED)), false);

		if (notification) {
			showNotificationSystem(errorMessage);
		}
	}

	/**
	 * Afficher une {@code Auction Bar}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param message le message
	 */
	public static void showActionBar(@NotNull Component message) {
		sendMessageInternal(message, true);
	}

	@ApiStatus.Internal
	private static void sendMessageInternal(@NotNull Component message, boolean overlay) {
		if (CLIENT.player != null) CLIENT.player.displayClientMessage(message, overlay);
	}

	/**
	 * Clears the currently displayed title and subtitle.
	 */
	public static void clearTitleAndSubtitle() {
		if (CLIENT.player != null) CLIENT.gui.clearTitles();
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
	 * @see #showSubtitle(Component, int, int, int) showSubtitle
	 * @see #showTitleAndSubtitle(Component, Component, int, int, int) showTitleAndSubtitle
	 */
	public static void showTitle(
			@NotNull Component title,
			@Range(from = 0, to = 250) int fadeInTicks,
			@Range(from = 0, to = 1000) int stayTicks,
			@Range(from = 0, to = 250) int fadeOutTicks
	) {
		showTitleAndSubtitle(title, Component.empty(), fadeInTicks, stayTicks, fadeOutTicks);
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
	 * @see #showTitle(Component, int, int, int) showTitle
	 * @see #showTitleAndSubtitle(Component, Component, int, int, int) showTitleAndSubtitle
	 */
	public static void showSubtitle(
			@NotNull Component subtitle,
			@Range(from = 0, to = 250) int fadeInTicks,
			@Range(from = 0, to = 1000) int stayTicks,
			@Range(from = 0, to = 250) int fadeOutTicks
	) {
		showTitleAndSubtitle(Component.empty(), subtitle, fadeInTicks, stayTicks, fadeOutTicks);
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
	 * @see #showTitle(Component, int, int, int) showTitle
	 * @see #showSubtitle(Component, int, int, int) showSubtitle
	 */
	public static void showTitleAndSubtitle(
			@NotNull Component title,
			@NotNull Component subtitle,
			@Range(from = 0, to = 250) int fadeInTicks,
			@Range(from = 0, to = 1000) int stayTicks,
			@Range(from = 0, to = 250) int fadeOutTicks
	) {
		if (CLIENT.player != null) {
			CLIENT.gui.setTimes(fadeInTicks, stayTicks, fadeOutTicks);
			CLIENT.gui.setTitle(title);
			CLIENT.gui.setSubtitle(subtitle);
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
			message = StringUtil.trimChatMessage(StringUtils.normalizeSpace(message.trim()));

			if (!hideToClient) {
				CLIENT.gui.getChat().addRecentChat(message);
			}

			if (message.startsWith("/")) {
				CLIENT.player.connection.sendCommand(message.substring(1));
			} else {
				CLIENT.player.connection.sendChat(message);
			}
		}
	}

	public static void showNotification(MutableComponent text, ItemStack icon) {
		showNotification(new StonksToast(text, icon));
	}

	public static void showNotification(Toast toast) {
		CLIENT.getToastManager().addToast(toast);
	}

	public static void showNotificationSystem(@NotNull String description) {
		showNotificationSystem("CaribouStonks", description);
	}

	public static void showNotificationSystem(@NotNull String title, @NotNull String description) {
		SystemToast systemToast = SystemToast.multiline(CLIENT, STONKS_SYSTEM, Component.literal(title), Component.literal(description));
		CLIENT.getToastManager().addToast(systemToast);
	}

	public static long getWorldDay() {
		return CLIENT.level != null ? CLIENT.level.getDayTime() / 24000 : 0L;
	}

	public static long getWorldTime() {
		return CLIENT.level != null ? CLIENT.level.getGameTime() : 0;
	}

	public static void playSound(@NotNull SoundEvent sound, float volume, float pitch) {
		if (CLIENT.player != null) CLIENT.player.playSound(sound, volume, pitch);
	}

	/**
	 * Jouer le son {@code UI_BUTTON_CLICK}.
	 */
	public static void playSoundButtonClickUI() {
		CLIENT.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@ApiStatus.Internal
	public static void handleUpdates() {
		updateScoreboard();
		updateTabList();
	}

	private static void updateScoreboard() {
		try {
			STRING_SCOREBOARD.clear();

			if (CLIENT.level == null || CLIENT.level.getScoreboard() == null) {
				return;
			}

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
				HudEvents.SCOREBOARD_UPDATE.invoker().onUpdate(STRING_SCOREBOARD);
			}
		} catch (Exception ignored) {
		}
	}

	private static void updateTabList() {
		try {
			STRING_TAB.clear();

			if (CLIENT.getConnection() == null) {
				return;
			}

			List<String> stringLines = new ArrayList<>();
			for (PlayerInfo playerListEntry : CLIENT.getConnection().getOnlinePlayers()) {
				if (playerListEntry.getTabListDisplayName() == null) {
					continue;
				}

				String name = playerListEntry.getTabListDisplayName().getString();
				if (name.isEmpty() || name.startsWith("[")) {
					continue;
				}

				//String formatted = StonksUtils.strip(name); // ?
				stringLines.add(name);
			}

			STRING_TAB.addAll(stringLines);
			if (SkyBlockAPI.isOnSkyBlock()) {
				HudEvents.TAB_LIST_UPDATE.invoker().onUpdate(STRING_TAB);
			}
		} catch (Exception ignored) {
		}
	}
}
