package fr.siroz.cariboustonks.feature.ui.tracking;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.json.JsonProcessingException;
import fr.siroz.cariboustonks.skyblock.IslandType;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.fishing.RareSeaCreature;
import fr.siroz.cariboustonks.util.DeveloperTools;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("checkstyle:linelength")
public final class MobTrackingRegistry {

	private static final Path MOB_TRACKING_PATH = CaribouStonks.CONFIG_DIR.resolve("mobTracking.json");

	private Map<String, MobTrackingEntry> trackedMobs = new HashMap<>();

	public MobTrackingRegistry() {
		ClientLifecycleEvents.CLIENT_STARTED.register(_client -> this.onClientStarted());
		ClientLifecycleEvents.CLIENT_STOPPING.register(_client -> this.onClientStopping());
		loadDefaultMobs();
	}

	private void loadDefaultMobs() {
		// Slayer Boss "register" en temps réel
		// Crimson Isle - Minibosses
		register("Bladesoul", 5, MobCategory.CRIMSON_ISLE_MINIBOSS, Component.literal("Bladesoul").withStyle(ChatFormatting.GRAY), false, IslandType.CRIMSON_ISLE);
		register("Magma Boss", 5, MobCategory.CRIMSON_ISLE_MINIBOSS, Component.literal("Magma Boss").withStyle(ChatFormatting.DARK_RED), false, IslandType.CRIMSON_ISLE);
		register("Ashfang", 5, MobCategory.CRIMSON_ISLE_MINIBOSS, Component.literal("Ashfang").withStyle(ChatFormatting.GRAY), false, IslandType.CRIMSON_ISLE);
		register("Mage Outlaw", 5, MobCategory.CRIMSON_ISLE_MINIBOSS, Component.literal("Mage Outlaw").withStyle(ChatFormatting.DARK_PURPLE), false, IslandType.CRIMSON_ISLE);
		register("Barbarian Duke X", 5, MobCategory.CRIMSON_ISLE_MINIBOSS, Component.literal("Barbarian Duke X").withStyle(ChatFormatting.GRAY), false, IslandType.CRIMSON_ISLE);
		// Crimson Isle - Special
		register("Vanquisher", 1, MobCategory.SPECIAL, Component.literal("Vanquisher").withStyle(ChatFormatting.DARK_PURPLE), true, IslandType.CRIMSON_ISLE);
		// Mythological Ritual
		register("Manticore", 50, MobCategory.MYTHOLOGICAL, Component.literal("Manticore").withStyle(ChatFormatting.DARK_GREEN), false, IslandType.HUB);
		register("Minos Inquisitor", 50, MobCategory.MYTHOLOGICAL, Component.literal("Minos Inquisitor").withStyle(ChatFormatting.LIGHT_PURPLE), false, IslandType.HUB);
		register("King Minos", 55, MobCategory.MYTHOLOGICAL, Component.literal("King Minos").withStyle(ChatFormatting.RED), false, IslandType.HUB);
		// Rare Fishing Mobs
		for (RareSeaCreature seaCreature : RareSeaCreature.values()) {
			register(seaCreature.getName(), 50, MobCategory.FISHING, Component.literal(seaCreature.getName()).withStyle(seaCreature.getColor()), true);
		}
	}

	public void updateMobTrackingConfig(Map<String, MobTrackingEntry> newTrackedMobs) {
		if (newTrackedMobs == null || newTrackedMobs.isEmpty()) {
			return;
		}

		Map<String, MobTrackingEntry> copy = new HashMap<>();
		for (Map.Entry<String, MobTrackingEntry> entry : newTrackedMobs.entrySet()) {
			if (entry.getValue() == null) {
				MobTrackingEntry backup = trackedMobs.get(entry.getKey());
				if (backup != null) {
					copy.put(entry.getKey(), backup);
				}
			} else {
				copy.put(entry.getKey(), entry.getValue());
			}
		}
		trackedMobs = copy;
	}

	public void saveMobTrackingConfig() {
		try {
			List<MobTrackingConfig> configs = new ArrayList<>();
			for (Map.Entry<String, MobTrackingEntry> entry : trackedMobs.entrySet()) {
				String mobName = entry.getValue().config().name;
				boolean enabled = entry.getValue().config().enabled;
				boolean notifyOnSpawn = entry.getValue().config().notifyOnSpawn;
				configs.add(new MobTrackingConfig(mobName, enabled, notifyOnSpawn));
			}
			CaribouStonks.core().getJsonFileService().save(MOB_TRACKING_PATH, configs);
		} catch (JsonProcessingException ex) {
			CaribouStonks.LOGGER.error("[MobTrackingFeature] Unable to save tracked config", ex);
		}
	}

	@Contract(value = " -> new", pure = true)
	public @NonNull Map<String, MobTrackingEntry> getTrackedMobs() {
		return new HashMap<>(trackedMobs);
	}

	@Nullable
	public MobTrackingEntry findMob(@Nullable String customName, @NotNull IslandType currentIsland) {
		if (customName == null || customName.isBlank()) return null;

		// Recherche partielle (toujours, car avec les tags des MobType et le health -_-)
		for (MobTrackingEntry entry : trackedMobs.values()) {
			if (entry.config().enabled
					&& entry.isAllowedOn(currentIsland)
					&& customName.contains(entry.config().name)
			) {
				return entry;
			}
		}

		return null;
	}

	@EventHandler(event = "ClientLifecycleEvents.CLIENT_STARTED")
	private void onClientStarted() {
		loadMobTrackingConfig().thenAccept(this::loadExistingMobTracking);
	}

	@EventHandler(event = "ClientLifecycleEvents.CLIENT_STOPPING")
	private void onClientStopping() {
		saveMobTrackingConfig();
	}

	private void register(
			@NotNull String mobName,
			int priority, MobCategory category,
			Component displayName,
			boolean notifyOnSpawn,
			IslandType... islandTypes
	) {
		MobTrackingConfig config = new MobTrackingConfig(mobName, true, notifyOnSpawn);
		trackedMobs.put(mobName, new MobTrackingEntry(config, priority, category, displayName, islandTypes));
	}

	private CompletableFuture<List<MobTrackingConfig>> loadMobTrackingConfig() {
		if (!Files.exists(MOB_TRACKING_PATH)) {
			return CompletableFuture.completedFuture(List.of());
		}

		return CompletableFuture.supplyAsync(() -> {
			try {
				return CaribouStonks.core().getJsonFileService().loadList(MOB_TRACKING_PATH, MobTrackingConfig.class);
			} catch (JsonProcessingException ex) {
				CaribouStonks.LOGGER.error("[MobTrackingFeature] Unable to load mob tracking configs", ex);
				return Collections.emptyList();
			}
		});
	}

	private void loadExistingMobTracking(@NotNull List<MobTrackingConfig> mobTrackingList) {
		for (MobTrackingConfig config : mobTrackingList) {

			int priority = 0;
			MobTrackingEntry priorityEntry = trackedMobs.get(config.name);
			if (priorityEntry != null) {
				priority = priorityEntry.priority();
			}

			MobCategory category = MobCategory.DEFAULT;
			MobTrackingEntry categoryEntry = trackedMobs.get(config.name);
			if (categoryEntry != null) {
				category = categoryEntry.category();
			}

			IslandType[] allowedIslands = null;
			MobTrackingEntry islandEntry = trackedMobs.get(config.name);
			if (islandEntry != null) {
				allowedIslands = islandEntry.allowedIslands();
			}

			Component displayName = Component.literal(config.name);
			MobTrackingEntry displayNameEntry = trackedMobs.get(config.name);
			if (displayNameEntry != null) {
				displayName = displayNameEntry.displayName();
			}

			MobTrackingEntry entry = new MobTrackingEntry(config, priority, category, displayName, allowedIslands);
			trackedMobs.put(entry.config().name, entry);
		}

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[MobTrackingFeature] Loaded {} mob tracking config", mobTrackingList.size());
		}
	}

	public static class MobTrackingConfig {
		public String name;
		public boolean enabled;
		public boolean notifyOnSpawn;

		public MobTrackingConfig(String name, boolean enabled, boolean notifyOnSpawn) {
			this.name = name;
			this.enabled = enabled;
			this.notifyOnSpawn = notifyOnSpawn;
		}
	}

	public record MobTrackingEntry(
			@NotNull MobTrackingConfig config,
			int priority,
			@NotNull MobCategory category,
			@NotNull Component displayName,
			@Nullable IslandType... allowedIslands
	) {

		public boolean isAllowedOn(IslandType currentIsland) {
			if (allowedIslands == null || allowedIslands.length == 0) {
				return true;
			}

			if (allowedIslands.length == 1) {
				if (allowedIslands[0] == IslandType.ANY) {
					return true;
				}

				return allowedIslands[0] == currentIsland;
			}

			for (IslandType allowed : allowedIslands) {
				if (allowed == currentIsland) {
					return true;
				}
			}

			return false;
		}
	}

	public enum MobCategory {
		DEFAULT(Component.literal("Default").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)),
		CRIMSON_ISLE_MINIBOSS(Component.literal("Crimson Isle Miniboss").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)),
		FISHING(Component.literal("Fishing").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)),
		MYTHOLOGICAL(Component.literal("Mythological").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD)),
		SPECIAL(Component.literal("Special").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)),
		;

		private final Component name;

		MobCategory(Component name) {
			this.name = name;
		}

		public Component getName() {
			return name;
		}
	}
}
