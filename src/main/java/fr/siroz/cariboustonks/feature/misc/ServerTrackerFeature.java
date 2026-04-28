package fr.siroz.cariboustonks.feature.misc;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.TimeUtils;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ServerTrackerFeature extends Feature {

	private final Map<String, Instant> serverVisitHistory = new HashMap<>();

	public ServerTrackerFeature() {
		SkyBlockEvents.ISLAND_CHANGE.register(this::onIslandChange);
	}

	@Override
	public boolean isEnabled() {
		return ConfigManager.getConfig().misc.serverTracker;
	}

	@EventHandler(event = "SkyBlockEvents.ISLAND_CHANGE_EVENT")
	private void onIslandChange(IslandType islandType, String serverId) {
		if (serverId == null || serverId.isBlank()) return;
		if (!isEnabled()) return;

		Instant now = Instant.now();
		Instant lastVisit = serverVisitHistory.get(serverId);
		if (lastVisit != null) {
			String elapsed = TimeUtils.getDurationFormatted(lastVisit, now, false);
			Client.sendMessageWithPrefix(Text.empty()
					.append(Text.literal(serverId).formatted(Formatting.DARK_GRAY))
					.append(Text.literal(" viewed ").formatted(Formatting.DARK_AQUA))
					.append(Text.literal(elapsed).formatted(Formatting.AQUA))
					.append(Text.literal(" ago").formatted(Formatting.DARK_AQUA)));
		}

		serverVisitHistory.put(serverId, now);
	}
}
