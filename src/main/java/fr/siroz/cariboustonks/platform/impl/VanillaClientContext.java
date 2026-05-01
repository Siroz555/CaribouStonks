package fr.siroz.cariboustonks.platform.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.Command;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ClientEvents;
import fr.siroz.cariboustonks.platform.api.ClientContext;
import fr.siroz.cariboustonks.mixin.accessors.PlayerTabOverlayAccessor;
import fr.siroz.cariboustonks.rendering.gui.element.StonksToast;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringUtil;
import net.minecraft.world.BossEvent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Vanilla-backed implementation of {@link ClientContext}.
 */
public final class VanillaClientContext implements ClientContext {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final SystemToast.SystemToastId STONKS_SYSTEM = new SystemToast.SystemToastId(10000L);
	private static final List<String> STRING_SCOREBOARD = new ArrayList<>();

	public VanillaClientContext() {
		TickScheduler.getInstance().runRepeating(this::updateScoreboard, 1, TimeUnit.SECONDS);
	}

	@Override
	public boolean isAvailable() {
		return CLIENT.player != null;
	}

	@Override
	public @Nullable String getPlayerName() {
		return isAvailable() ? CLIENT.player.getName().getString() : null;
	}

	@Override
	public @NonNull Vec3 position() {
		return isAvailable() ? CLIENT.player.position() : Vec3.ZERO;
	}

	@Override
	public @NonNull BlockPos blockPosition() {
		return blockPosition(false);
	}

	@Override
	public @NonNull BlockPos blockPosition(boolean crosshairTargetAsBlockPos) {
		if (CLIENT.player == null) return BlockPos.ZERO;

		if (crosshairTargetAsBlockPos
				&& CLIENT.hitResult instanceof BlockHitResult blockHitResult
				&& CLIENT.hitResult.getType() == HitResult.Type.BLOCK) {
			return blockHitResult.getBlockPos();
		}

		return CLIENT.player.blockPosition();
	}

	@Override
	public @NonNull List<String> getScoreboard() {
		return new ArrayList<>(STRING_SCOREBOARD);
	}

	@Override
	public @Nullable String getTabListFooter() {
		Component footer = ((PlayerTabOverlayAccessor) CLIENT.gui.getTabList()).getFooter();
		return footer != null ? footer.getString() : null;
	}

	@Override
	public @Nullable ItemStack getMainHandItem() {
		return CLIENT.player != null ? CLIENT.player.getMainHandItem() : null;
	}

	@Override
	public @Nullable ItemStack getHeldItem() {
		return CLIENT.player != null ? CLIENT.player.getInventory().getSelectedItem() : null;
	}

	@Override
	public void setScreen(@Nullable Screen screen) {
		CLIENT.setScreen(screen);
	}

	@Override
	public @NonNull Command<FabricClientCommandSource> openScreen(@NonNull Supplier<Screen> screenSupplier) {
		return _ -> {
			CLIENT.schedule(() -> CLIENT.setScreen(screenSupplier.get()));
			return Command.SINGLE_SUCCESS;
		};
	}

	@Override
	public void sendMessage(@NonNull Component message) {
		sendMessageInternal(message);
	}

	@Override
	public void sendMessageWithPrefix(@NonNull Component message) {
		sendMessageInternal(CaribouStonks.prefix().get().append(message));
	}

	@Override
	public void sendChatToServer(@NonNull String message, boolean hideToClient) {
		sendToServerInternal(message, hideToClient, false);
	}

	@Override
	public void sendCommandToServer(@NonNull String command, boolean hideToClient) {
		sendToServerInternal(command, hideToClient, true);
	}

	@Override
	public void sendErrorMessage(@NonNull String errorMessage, boolean notification) {
		CaribouStonks.LOGGER.warn("Chat error message sent: {}", errorMessage);
		sendMessageInternal(CaribouStonks.prefix().get()
				.append(Component.literal(errorMessage).withStyle(ChatFormatting.RED)));

		if (notification) {
			showNotificationSystem(errorMessage);
		}
	}

	@Override
	public void showTitleAndSubtitle(@NonNull Component title, @NonNull Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
		if (CLIENT.player != null) {
			CLIENT.gui.setTimes(fadeInTicks, stayTicks, fadeOutTicks);
			CLIENT.gui.setTitle(title);
			CLIENT.gui.setSubtitle(subtitle);
		}
	}

	@Override
	public void showNotification(@NonNull MutableComponent text, @NonNull ItemStack icon) {
		CLIENT.getToastManager().addToast(new StonksToast(text, icon));
	}

	@Override
	public void showNotificationSystem(@NonNull String title, @NonNull String description) {
		SystemToast systemToast = SystemToast.multiline(CLIENT, STONKS_SYSTEM, Component.literal(title), Component.literal(description));
		CLIENT.getToastManager().addToast(systemToast);
	}

	@Override
	public void showFatalErrorScreen(@NonNull Component title, @NonNull Component message) {
		if (CLIENT.player != null) CLIENT.setScreen(new ErrorScreen(title, message));
	}

	@Override
	public void showBossBar(@NonNull BossEvent bossBar) {
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

	@Override
	public void removeBossBar(@NonNull BossEvent bossBar) {
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

	@Override
	public void playSound(@NonNull SoundEvent sound, float volume, float pitch) {
		if (CLIENT.player != null) CLIENT.player.playSound(sound, volume, pitch);
	}

	@Override
	public void playSoundButtonClickUI() {
		CLIENT.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@Override
	public void setToClipboard(@NonNull String toClipboard) {
		CLIENT.keyboardHandler.setClipboard(toClipboard);
	}

	@Override
	public void handleMouseClick(int containerId, int slotId, ContainerInput type) {
		if (CLIENT.player != null && CLIENT.gameMode != null) {
			CLIENT.gameMode.handleContainerInput(containerId, slotId, 0, type, CLIENT.player);
		}
	}

	@Override
	public Collection<PlayerInfo> getOnlinePlayers() {
		return CLIENT.getConnection() != null ? CLIENT.getConnection().getOnlinePlayers() : Collections.emptyList();
	}

	@Override
	public void sendPacket(@NonNull Packet<?> packet) {
		if (CLIENT.getConnection() != null) {
			CLIENT.getConnection().send(packet);
		}
	}

	@Override
	public boolean isPacketListenerAvailable() {
		return CLIENT.getConnection() != null;
	}

	@Override
	public boolean isKeyPressed(int keyCode) {
		return InputConstants.isKeyDown(CLIENT.getWindow(), keyCode);
	}

	@Override
	public boolean hasShiftDown() {
		return InputConstants.isKeyDown(CLIENT.getWindow(), 340) || InputConstants.isKeyDown(CLIENT.getWindow(), 344);
	}

	private void sendMessageInternal(@NonNull Component message) {
		if (CLIENT.player != null) CLIENT.player.sendSystemMessage(message);
	}

	private void sendToServerInternal(@NonNull String content, boolean hideToClient, boolean command) {
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

	private void updateScoreboard() {
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
				ClientEvents.SCOREBOARD_UPDATE_EVENT.invoker().onUpdate(STRING_SCOREBOARD);
			}
		} catch (Exception _) {
		}
	}
}
