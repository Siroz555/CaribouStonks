package fr.siroz.cariboustonks.platform.impl.context;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.platform.api.context.PlayerContext;
import fr.siroz.cariboustonks.rendering.gui.element.StonksToast;
import fr.siroz.cariboustonks.util.DeveloperTools;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.player.LocalPlayer;
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
 * Vanilla-backed implementation of {@link PlayerContext}.
 */
public final class VanillaPlayerContext implements PlayerContext {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final SystemToast.SystemToastId STONKS_SYSTEM = new SystemToast.SystemToastId(10000L);

	@Override
	public boolean isAvailable() {
		return CLIENT.player != null;
	}

	@Override
	public @Nullable LocalPlayer asLocalPlayer() {
		return CLIENT.player;
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
		if (!isAvailable()) return BlockPos.ZERO;

		if (crosshairTargetAsBlockPos
				&& CLIENT.hitResult instanceof BlockHitResult blockHitResult
				&& CLIENT.hitResult.getType() == HitResult.Type.BLOCK) {
			return blockHitResult.getBlockPos();
		}

		return CLIENT.player.blockPosition();
	}

	@Override
	public @Nullable ItemStack getMainHandItem() {
		return isAvailable() ? CLIENT.player.getMainHandItem() : null;
	}

	@Override
	public @Nullable ItemStack getHeldItem() {
		return isAvailable() ? CLIENT.player.getInventory().getSelectedItem() : null;
	}

	@Override
	public @Nullable HitResult getHitResult() {
		return CLIENT.hitResult;
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
		if (isAvailable()) {
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
		if (isAvailable()) {
			CLIENT.setScreen(new ErrorScreen(title, message));
		}
	}

	@Override
	public void showBossBar(@NonNull BossEvent bossBar) {
		if (isAvailable()) {
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
		if (isAvailable()) {
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
		if (isAvailable()) CLIENT.player.playSound(sound, volume, pitch);
	}

	private void sendMessageInternal(@NonNull Component message) {
		if (isAvailable()) CLIENT.player.sendSystemMessage(message);
	}

	private void sendToServerInternal(@NonNull String content, boolean hideToClient, boolean command) {
		if (isAvailable()) {
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
}
