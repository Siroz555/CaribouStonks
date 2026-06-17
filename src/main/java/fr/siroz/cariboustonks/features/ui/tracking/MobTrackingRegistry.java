package fr.siroz.cariboustonks.features.ui.tracking;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.model.MobTrackingModel;
import fr.siroz.cariboustonks.core.service.json.JsonFileService;
import fr.siroz.cariboustonks.core.service.json.JsonProcessingException;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.features.fishing.RareSeaCreature;
import fr.siroz.cariboustonks.util.DeveloperTools;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("checkstyle:linelength")
public final class MobTrackingRegistry {
	private static final Path MOB_TRACKING_PATH = CaribouStonks.CONFIG_DIR.resolve("mobTracking.json");

	private Map<String, MobTrackingEntry> trackedMobs = new HashMap<>();

	public MobTrackingRegistry() {
		this.registerDefaults();

		ClientLifecycleEvents.CLIENT_STARTED.register(_mc -> this.onClientStarted());
		ClientLifecycleEvents.CLIENT_STOPPING.register(_mc -> this.onClientStopping());
	}

	private void registerDefaults() {
		// Slayer Boss "register" en temps réel
		// Crimson Isle - Minibosses
		register("Bladesoul", 5, NotifyConditions.ONCE, MobCategory.CRIMSON_ISLE_MINIBOSS, Component.literal("Bladesoul").withStyle(ChatFormatting.GRAY), false, false, IslandType.CRIMSON_ISLE);
		register("Magma Boss", 5, NotifyConditions.ONCE, MobCategory.CRIMSON_ISLE_MINIBOSS, Component.literal("Magma Boss").withStyle(ChatFormatting.DARK_RED), false, false, IslandType.CRIMSON_ISLE);
		register("Ashfang", 5, NotifyConditions.ONCE, MobCategory.CRIMSON_ISLE_MINIBOSS, Component.literal("Ashfang").withStyle(ChatFormatting.GRAY), false, false, IslandType.CRIMSON_ISLE);
		register("Mage Outlaw", 5, NotifyConditions.ONCE, MobCategory.CRIMSON_ISLE_MINIBOSS, Component.literal("Mage Outlaw").withStyle(ChatFormatting.DARK_PURPLE), false, false, IslandType.CRIMSON_ISLE);
		register("Barbarian Duke X", 5, NotifyConditions.ONCE, MobCategory.CRIMSON_ISLE_MINIBOSS, Component.literal("Barbarian Duke X").withStyle(ChatFormatting.GRAY), false, false, IslandType.CRIMSON_ISLE);
		// Crimson Isle - Special
		register("Vanquisher", 1, NotifyConditions.maxTimes(2), MobCategory.SPECIAL, Component.literal("Vanquisher").withStyle(ChatFormatting.DARK_PURPLE), true, true, IslandType.CRIMSON_ISLE);
		// Mythological Ritual
		register("Manticore", 50, NotifyConditions.ONCE, MobCategory.MYTHOLOGICAL, Component.literal("Manticore").withStyle(ChatFormatting.DARK_GREEN), false, false, IslandType.HUB);
		register("Minos Inquisitor", 50, NotifyConditions.ONCE, MobCategory.MYTHOLOGICAL, Component.literal("Minos Inquisitor").withStyle(ChatFormatting.LIGHT_PURPLE), false, false, IslandType.HUB);
		register("King Minos", 55, NotifyConditions.ONCE, MobCategory.MYTHOLOGICAL, Component.literal("King Minos").withStyle(ChatFormatting.RED), false, false, IslandType.HUB);
		// Rare Fishing Mobs
		for (RareSeaCreature seaCreature : RareSeaCreature.values()) {
			NotifyCondition notifyCondition = NotifyConditions.ONCE;
			// Cas particulier pour le Puddle Jumper, car il se fait détecter tout le long des jumps
			if (seaCreature == RareSeaCreature.PUDDLE_JUMPER) notifyCondition = NotifyConditions.onceIfBelowY(74);

			register(seaCreature.getName(), 50, notifyCondition, MobCategory.FISHING, Component.literal(seaCreature.getName()).withStyle(seaCreature.getColor()), true, seaCreature.isHighlightable(), seaCreature.getIslandType());
		}
		// Mining - Mineshaft
		register("Littlefoot", 50, NotifyConditions.ALWAYS, MobCategory.MINING, Component.literal("Littlefoot").withStyle(ChatFormatting.BLUE), true, true, IslandType.GLACITE_MINESHAFT);
	}

	public void updateMobTrackingConfig(Map<String, MobTrackingEntry> newTrackedMobs) {
		if (newTrackedMobs == null || newTrackedMobs.isEmpty()) return;

		Map<String, MobTrackingEntry> copy = new HashMap<>();
		for (Map.Entry<String, MobTrackingEntry> entry : newTrackedMobs.entrySet()) {
			// Si la nouvelle valeur est null, on conserve l'entrée existante, "hot reload" partiel
			MobTrackingEntry resolved = entry.getValue() != null ? entry.getValue() : trackedMobs.get(entry.getKey());
			if (resolved != null) {
				copy.put(entry.getKey(), resolved);
			}
		}
		trackedMobs = copy;
	}

	public void saveMobTrackingConfig() {
		try {
			List<MobTrackingModel> models = new ArrayList<>();
			for (Map.Entry<String, MobTrackingEntry> entry : trackedMobs.entrySet()) {
				String mobName = entry.getValue().model().getName();
				boolean enabled = entry.getValue().model().isEnabled();
				boolean notifyOnSpawn = entry.getValue().model().isNotifyOnSpawn();
				boolean highlightable = entry.getValue().model().isHighlightable();
				models.add(new MobTrackingModel(mobName, enabled, notifyOnSpawn, highlightable));
			}
			JsonFileService.get().save(MOB_TRACKING_PATH, models);
		} catch (JsonProcessingException ex) {
			CaribouStonks.LOGGER.error("[MobTrackingFeature] Unable to save tracked config", ex);
		}
	}

	public @NonNull Map<String, MobTrackingEntry> getTrackedMobsSnapshot() {
		return new HashMap<>(trackedMobs);
	}

	@Nullable
	public MobTrackingEntry findMob(
			@Nullable String customName,
			@NonNull BiFunction<String, String, Boolean> searchFunction,
			@NonNull IslandType currentIsland
	) {
		if (customName == null || customName.isBlank()) return null;

		// Recherche partielle [CONTAINS] (toujours, car avec les tags des MobType et le health -_-)
		// OU
		// Strictement égal [EQUALS] (dans le cas ou l'entity a un nom comme le Mage Outlaw ou un Aligator)
		for (MobTrackingEntry entry : trackedMobs.values()) {
			if (entry.model().isEnabled()
					&& entry.isAllowedOn(currentIsland)
					&& searchFunction.apply(customName, entry.model.getName())
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
			@NonNull String mobName,
			int priority,
			NotifyCondition notifyCondition,
			MobCategory category,
			Component displayName,
			boolean notifyOnSpawn,
			boolean highlightable,
			IslandType... islandTypes
	) {
		MobTrackingModel model = new MobTrackingModel(mobName, true, notifyOnSpawn, highlightable);
		trackedMobs.put(mobName, new MobTrackingEntry(model, priority, notifyCondition, category, displayName, islandTypes));
	}

	private CompletableFuture<List<MobTrackingModel>> loadMobTrackingConfig() {
		if (!Files.exists(MOB_TRACKING_PATH)) {
			return CompletableFuture.completedFuture(List.of());
		}

		return CompletableFuture.supplyAsync(() -> {
			try {
				return JsonFileService.get().loadList(MOB_TRACKING_PATH, MobTrackingModel.class);
			} catch (JsonProcessingException ex) {
				CaribouStonks.LOGGER.error("[MobTrackingFeature] Unable to load mob tracking configs", ex);
				return Collections.emptyList();
			}
		});
	}

	private void loadExistingMobTracking(@NonNull List<MobTrackingModel> models) {
		for (MobTrackingModel model : models) {

			MobTrackingEntry existing = trackedMobs.get(model.getName());

			int priority = existing != null ? existing.priority() : 0;
			NotifyCondition condition = existing != null ? existing.notifyCondition() : NotifyConditions.ONCE;
			MobCategory category = existing != null ? existing.category() : MobCategory.DEFAULT;
			IslandType[] islands = existing != null ? existing.allowedIslands() : null;
			Component displayName = existing != null ? existing.displayName() : Component.literal(model.getName());

			MobTrackingEntry entry = new MobTrackingEntry(model, priority, condition, category, displayName, islands);
			trackedMobs.put(entry.model().getName(), entry);
		}

		if (DeveloperTools.isInDevelopment()) {
			CaribouStonks.LOGGER.info("[MobTrackingFeature] Loaded {} mob tracking config", models.size());
		}
	}

	public record MobTrackingEntry(
			@NonNull MobTrackingModel model,
			int priority,
			@NonNull NotifyCondition notifyCondition,
			@NonNull MobCategory category,
			@NonNull Component displayName,
			@Nullable IslandType... allowedIslands
	) {

		public boolean isAllowedOn(IslandType currentIsland) {
			if (allowedIslands == null || allowedIslands.length == 0) return true;

			if (allowedIslands.length == 1) {
				if (allowedIslands[0] == IslandType.ANY) return true;
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
		MINING(Component.literal("Mining").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD)),
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
