package fr.siroz.cariboustonks.platform.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.Command;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ClientEvents;
import fr.siroz.cariboustonks.mixin.accessors.PlayerTabOverlayAccessor;
import fr.siroz.cariboustonks.platform.api.ClientContext;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Vanilla-backed implementation of {@link ClientContext}.
 */
public final class VanillaClientContext implements ClientContext {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final List<String> STRING_SCOREBOARD = new ArrayList<>();

	public VanillaClientContext() {
		TickScheduler.getInstance().runRepeating(this::updateScoreboard, 1, TimeUnit.SECONDS);
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
	public void setToClipboard(@NonNull String toClipboard) {
		CLIENT.keyboardHandler.setClipboard(toClipboard);
	}

	@Override
	public boolean isKeyPressed(int keyCode) {
		return InputConstants.isKeyDown(CLIENT.getWindow(), keyCode);
	}

	@Override
	public boolean isPacketListenerAvailable() {
		return CLIENT.getConnection() != null;
	}

	@Override
	public boolean hasShiftDown() {
		return InputConstants.isKeyDown(CLIENT.getWindow(), 340) || InputConstants.isKeyDown(CLIENT.getWindow(), 344);
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
