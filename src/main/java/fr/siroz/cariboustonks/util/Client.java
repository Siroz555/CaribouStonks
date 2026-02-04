package fr.siroz.cariboustonks.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.Command;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.HudEvents;
import fr.siroz.cariboustonks.mixin.accessors.PlayerTabOverlayAccessor;
import fr.siroz.cariboustonks.util.render.gui.StonksToast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringUtil;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

/**
 * Client utilities.
 * <p>
 * The methods are {@code Safe Client null} / {@code Safe World null}.
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
	 * <p>
	 * {@code Safe Client null}
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
	 * Set the given {@code String} to the Client Clipboard.
	 *
	 * @param toClipboard the string
	 */
	public static void setToClipboard(@NotNull String toClipboard) {
		CLIENT.keyboardHandler.setClipboard(toClipboard);
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
		Component footer = ((PlayerTabOverlayAccessor) CLIENT.gui.getTabList()).getFooter();
		return footer != null ? footer.getString() : null;
	}

	/**
	 * Retrieves the currently held item in the main hand of the client.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @return the {@link ItemStack} representing the item currently held in the main hand of the client or {@code null}
	 * @see #getHeldItem()
	 */
	public static @Nullable ItemStack getMainHandItem() {
		return CLIENT.player != null ? CLIENT.player.getMainHandItem() : null;
	}

	/**
	 * Retrieves the currently held item of the client.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @return the {@link ItemStack} representing the item currently held by the client or {@code null}
	 * @see #getMainHandItem()
	 */
	@Nullable
	public static ItemStack getHeldItem() {
		return CLIENT.player != null ? CLIENT.player.getInventory().getSelectedItem() : null;
	}

	/**
	 * Retrieves the {@code Day} of the current world.
	 * <p>
	 * {@code Safe World null}
	 *
	 * @return the day of the current world
	 */
	public static long getWorldDay() {
		return CLIENT.level != null ? CLIENT.level.getDayTime() / 24000 : 0L;
	}

	/**
	 * Retrieves the {@code World Time} of the current world.
	 * <p>
	 * {@code Safe World null}
	 *
	 * @return the world time of the current world
	 */
	public static long getWorldTime() {
		return CLIENT.level != null ? CLIENT.level.getGameTime() : 0;
	}

	/**
	 * Returns a List of {@link Entity}s close to the player, according to the given conditions.
	 * <p>
	 * {@code Safe Client null} & {@code Safe World null}
	 *
	 * @param entity           the entity class type
	 * @param distanceInBlocks the distance in blocks from the player's position
	 * @param entityPredicate  the entity predicate (e.g. {@code Entity::hasCustomName})
	 * @param <T>              the entity class type
	 * @return a List of {@link Entity}s close to the player if the conditions are met, otherwise an empty list
	 */
	@SuppressWarnings("unused") // SIROZ-NOTE: Impl
	public static <T extends Entity> List<T> findClosestEntities(
			@NotNull Class<T> entity,
			double distanceInBlocks,
			@NotNull Predicate<? super T> entityPredicate
	) {
		if (CLIENT.player == null || CLIENT.level == null) {
			return Collections.emptyList();
		}

		return CLIENT.level.getEntitiesOfClass(entity, CLIENT.player.getBoundingBox().inflate(distanceInBlocks), entityPredicate);
	}

	/**
	 * Returns the {@link Entity} closest to the player, according to the given conditions.
	 * <p>
	 * {@code Safe Client null} & {@code Safe World null}
	 *
	 * @param entity           the entity class type
	 * @param distanceInBlocks the distance in blocks from the player's position
	 * @param entityPredicate  the entity predicate (e.g. {@code Entity::hasCustomName})
	 * @param filterPredicate  the filter predicate (e.g. {@code e -> "King Minos".equals(e.getName().getString())})
	 * @param <T>              the entity class type
	 * @return the closest entity to the player if the conditions are met, otherwise null
	 */
	@SuppressWarnings("unused") // SIROZ-NOTE: Impl
	public static <T extends Entity> @Nullable T findClosestEntity(
			@NotNull Class<T> entity,
			double distanceInBlocks,
			@NotNull Predicate<? super T> entityPredicate,
			@NotNull Predicate<? super T> filterPredicate
	) {
		if (CLIENT.player == null || CLIENT.level == null) {
			return null;
		}

		return findClosestEntities(entity, distanceInBlocks, entityPredicate).stream()
				.filter(filterPredicate)
				.min(Comparator.comparingDouble(as -> as.distanceToSqr(CLIENT.player)))
				.orElse(null);
	}

	/**
	 * Create a command that queues a screen to be opened in the next tick.
	 * Used to prevent the screen from closing immediately after the command is executed.
	 *
	 * @param screenSupplier the screen supplier
	 * @return {@link Command Command with FabricClientCommandSource}
	 */
	@Contract(pure = true)
	public static @NotNull Command<FabricClientCommandSource> openScreen(@NotNull Supplier<Screen> screenSupplier) {
		return context -> {
			CLIENT.schedule(() -> CLIENT.setScreen(screenSupplier.get()));
			return Command.SINGLE_SUCCESS;
		};
	}

	/**
	 * Send a message to the <b>client</b>.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param message the message
	 * @see #sendMessageWithPrefix(Component)
	 */
	public static void sendMessage(@NotNull Component message) {
		sendMessageInternal(message);
	}

	/**
	 * Send a message to the <b>client</b> with the Mod prefix.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param message the message
	 * @see #sendMessage(Component)
	 */
	public static void sendMessageWithPrefix(@NotNull Component message) {
		sendMessageInternal(CaribouStonks.prefix().get().append(message));
	}

	/**
	 * Send an error message to the <b>client</b>.
	 * The message contains the Mod prefix and the given message in red format.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param errorMessage the error message
	 * @param notification if the message should be displayed in a Toast Notification
	 */
	public static void sendErrorMessage(@NotNull String errorMessage, boolean notification) {
		CaribouStonks.LOGGER.warn("Chat error message sent: {}", errorMessage);
		sendMessageInternal(CaribouStonks.prefix().get()
				.append(Component.literal(errorMessage).withStyle(ChatFormatting.RED)));

		if (notification) {
			showNotificationSystem(errorMessage);
		}
	}

	@ApiStatus.Internal
	private static void sendMessageInternal(@NotNull Component message) {
		if (CLIENT.player != null) CLIENT.player.displayClientMessage(message, false);
	}

	/**
	 * Display a {@code Title}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param title        the title
	 * @param fadeInTicks  duration in ticks of the title appearance animation (0 to 250)
	 * @param stayTicks    duration in ticks during which the title remains visible (0 to 1000)
	 * @param fadeOutTicks duration in ticks of the title fade-out animation (0 to 250)
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
	 * Display a {@code Subtitle}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param subtitle     the subtitle
	 * @param fadeInTicks  duration in ticks of the title appearance animation (0 to 250)
	 * @param stayTicks    duration in ticks during which the title remains visible (0 to 1000)
	 * @param fadeOutTicks duration in ticks of the title fade-out animation (0 to 250)
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
	 * Display a {@code Title} and a {@code Subtitle}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param title        the title
	 * @param subtitle     the subtitle
	 * @param fadeInTicks  duration in ticks of the title appearance animation (0 to 250)
	 * @param stayTicks    duration in ticks during which the title remains visible (0 to 1000)
	 * @param fadeOutTicks duration in ticks of the title fade-out animation (0 to 250)
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
	 * Send a {@code message} in the chat to the <b>server</b>.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param message the message
	 * @see #sendCommandToServer(String, boolean)
	 */
	public static void sendChatToServer(@NotNull String message, boolean hideToClient) {
		sendToServerInternal(message, hideToClient, false);
	}

	/**
	 * Send a {@code command} in the chat to the <b>server</b>.
	 * <p>
	 * The command can be: {@code /pc hello} or {@code pc hello}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param command the command
	 * @see #sendChatToServer(String, boolean)
	 */
	public static void sendCommandToServer(@NotNull String command, boolean hideToClient) {
		sendToServerInternal(command, hideToClient, true);
	}

	@ApiStatus.Internal
	private static void sendToServerInternal(@NotNull String content, boolean hideToClient, boolean command) {
		if (CLIENT.player != null) {
			content = StringUtil.trimChatMessage(StringUtils.normalizeSpace(content.trim()));

			if (!hideToClient) {
				CLIENT.gui.getChat().addRecentChat(content);
			}

			if (command) {
				content = content.startsWith("/") ? content.substring(1) : content;
				CLIENT.player.connection.sendCommand(content);
			} else {
				CLIENT.player.connection.sendChat(content);
			}
		}
	}

	/**
	 * Display a {@code Toast Notification}.
	 *
	 * @param text the text
	 * @param icon the icon
	 */
	public static void showNotification(@NotNull MutableComponent text, @NotNull ItemStack icon) {
		CLIENT.getToastManager().addToast(new StonksToast(text, icon));
	}

	/**
	 * Display a {@code Toast Notification System} with the Mod prefix.
	 *
	 * @param description the text description
	 */
	public static void showNotificationSystem(@NotNull String description) {
		showNotificationSystem("CaribouStonks", description);
	}

	/**
	 * Display a {@code Toast Notification System}.
	 *
	 * @param title       the text title
	 * @param description the text description
	 */
	public static void showNotificationSystem(@NotNull String title, @NotNull String description) {
		SystemToast systemToast = SystemToast.multiline(CLIENT, STONKS_SYSTEM, Component.literal(title), Component.literal(description));
		CLIENT.getToastManager().addToast(systemToast);
	}

	/**
	 * Display the {@code ErrorScreen} (Fatal Screen).
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param title   the title
	 * @param message the message
	 */
	public static void showFatalErrorScreen(@NotNull Component title, @NotNull Component message) {
		if (CLIENT.player != null) CLIENT.setScreen(new ErrorScreen(title, message));
	}

	/**
	 * Display a {@link BossEvent} to the client.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param bossBar the bossBar to show
	 */
	public static void showBossBar(@NotNull BossEvent bossBar) {
		if (CLIENT.player != null) {
			try {
				CLIENT.gui.getBossOverlay().update(ClientboundBossEventPacket.createAddPacket(bossBar));
			} catch (Exception ex) {
				if (DeveloperTools.isInDevelopment()) {
					CaribouStonks.LOGGER.error("Unable to update bossBar handler (ADD)", ex);
				}
			}
		}
	}

	/**
	 * Remove the given {@link BossEvent} to the client.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param bossBar the bossBar to remove
	 */
	public static void removeBossBar(@NotNull BossEvent bossBar) {
		if (CLIENT.player != null) {
			try {
				CLIENT.gui.getBossOverlay().update(ClientboundBossEventPacket.createRemovePacket(bossBar.getId()));
			} catch (Exception ex) {
				if (DeveloperTools.isInDevelopment()) {
					CaribouStonks.LOGGER.error("Unable to update bossBar handler (REMOVE)", ex);
				}
			}
		}
	}

	/**
	 * Retrieve the {@code Sound Name} of the given Sound Packet.
	 * <p>
	 * {@code entity.warden.death}
	 *
	 * @param soundPacket the sound packet
	 * @return the sound name of an empty String
	 */
	public static @NonNull String convertSoundPacketToName(@Nullable ClientboundSoundPacket soundPacket) {
		if (soundPacket == null) return "";
		return soundPacket.getSound().value().location().getPath();
	}

	/**
	 * Play a {@code Sound}.
	 * <p>
	 * {@code Safe Client null}
	 *
	 * @param sound  the sound
	 * @param volume the volume
	 * @param pitch  the pitch
	 */
	public static void playSound(@NotNull SoundEvent sound, float volume, float pitch) {
		if (CLIENT.player != null) CLIENT.player.playSound(sound, volume, pitch);
	}

	/**
	 * Play {@code UI_BUTTON_CLICK} sound.
	 */
	public static void playSoundButtonClickUI() {
		CLIENT.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	/**
	 * Retrieves a list of armor items currently equipped by the given {@link LivingEntity}.
	 *
	 * @param entity the living entity
	 * @return list of {@link ItemStack} representing the armor items equipped by the entity
	 */
	@NotNull
	@Unmodifiable
	public static List<ItemStack> getArmorFromEntity(@NotNull LivingEntity entity) {
		return EquipmentSlotGroup.ARMOR.slots().stream()
				.filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
				.map(entity::getItemBySlot)
				.toList();
	}

	@ApiStatus.Internal
	public static void handleUpdates() {
		updateScoreboard();
		updateTabList();
	}

	private static void updateScoreboard() {
		try {
			STRING_SCOREBOARD.clear();

			if (CLIENT.level == null) {
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
