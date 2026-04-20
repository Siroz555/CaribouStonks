package fr.siroz.cariboustonks.features.misc;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.TimeUtils;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class ServerTrackerFeature extends Feature {

	private final Map<String, Instant> serverVisitHistory = new HashMap<>();

	public ServerTrackerFeature() {
		SkyBlockEvents.ISLAND_CHANGE_EVENT.register(this::onIslandChange);
	}

	@Override
	public boolean isEnabled() {
		return this.config().misc.serverTracker;
	}

	@EventHandler(event = "SkyBlockEvents.ISLAND_CHANGE_EVENT")
	private void onIslandChange(@NonNull IslandType islandType, String serverId) {
		if (serverId == null || serverId.isBlank()) return;
		if (!isEnabled()) return;

		Instant now = Instant.now();
		Instant lastVisit = serverVisitHistory.get(serverId);
		if (lastVisit != null) {
			String elapsed = TimeUtils.getDurationFormatted(lastVisit, now, false);
			Client.sendMessageWithPrefix(Component.empty()
					.append(Component.literal(serverId).withStyle(ChatFormatting.DARK_GRAY))
					.append(Component.literal(" viewed ").withStyle(ChatFormatting.DARK_AQUA))
					.append(Component.literal(elapsed).withStyle(ChatFormatting.AQUA))
					.append(Component.literal(" ago").withStyle(ChatFormatting.DARK_AQUA)));
		}

		serverVisitHistory.put(serverId, now);
	}
}
