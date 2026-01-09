package fr.siroz.cariboustonks.feature.combat.tracking;

import fr.siroz.cariboustonks.feature.fishing.RareSeaCreature;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public final class MobTrackingRegistry {

	// SIROZ-NOTE: Faire le menu custom pour add/delete.
	//  Faire des check des island maybe, bref rajouter des conditions
	//  voir créer des lists selon chaque island, et récupérer la bonne liste selon la location.

	private final Map<String, MobTrackingEntry> trackedMobs = new HashMap<>();

	public MobTrackingRegistry() {
		loadDefaultMobs();
	}

	private void loadDefaultMobs() { // SIROZ-NOTE: TEST TEST TEST
		// Crimson Isle - Minibosses
		register("Bladesoul", 5);
		register("Magma Boss", 5);
		register("Ashfang", 5);
		register("Mage Outlaw", 5);
		register("Barbarian Duke X", 5);
		// Crimson Isle - Special
		register("Vanquisher", 1);
		// Mythological Ritual
		register("Minos Inquisitor", 50);
		register("King Minos", 55);
		// Register Rare Fishing Mobs
		for (RareSeaCreature seaCreature : RareSeaCreature.values()) {
			register(seaCreature.getName(), 50);
		}
	}

	public void register(@NotNull String mobName, int priority/*, IslandType... allowedIslands*/) {
		trackedMobs.put(mobName, new MobTrackingEntry(mobName, priority/*, allowedIslands*/));
	}

	public void unregister(@Nullable String mobName) {
		if (mobName != null) {
			trackedMobs.remove(mobName);
		}
	}

	@Contract(value = " -> new", pure = true)
	public @NonNull Map<String, MobTrackingEntry> getTrackedMobs() {
		return new HashMap<>(trackedMobs);
	}

	@Nullable
	public MobTrackingEntry findMob(@Nullable String customName) {
		if (customName == null || customName.isBlank()) return null;

		// Recherche exacte d'abord
		MobTrackingEntry exact = trackedMobs.get(customName);
		if (exact != null /*&& exact.isAllowedOn(currentIsland)*/) {
			return exact;
		}

		// Recherche partielle
		for (MobTrackingEntry entry : trackedMobs.values()) {
			/*if (!entry.isAllowedOn(currentIsland)) {
				continue;
			}*/

			if (customName.contains(entry.name())) {
				return entry;
			}
		}

		return null;
	}

	public record MobTrackingEntry(String name, int priority/*, IslandType... allowedIslands*/) {

//		public boolean isAllowedOn(IslandType currentIsland) {
//			if (allowedIslands == null || allowedIslands.length == 0) {
//				return true;
//			}
//
//			for (IslandType allowed : allowedIslands) {
//				if (allowed == currentIsland) {
//					return true;
//				}
//			}
//
//			return false;
//		}
	}
}
