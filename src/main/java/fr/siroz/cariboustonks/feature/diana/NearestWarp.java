package fr.siroz.cariboustonks.feature.diana;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.Client;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

final class NearestWarp {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	// Considérer le target comme "suffisamment proche" s'il se trouve < 100 blocs
	private static final double CLOSE_TO_TARGET_DISTANCE_SQ = 100 * 100;
	// Exiger au moins cette "amélioration" (en blocs) pour suggérer un Warp
	private static final double MIN_IMPROVEMENT_BLOCKS_SQ = 80 * 80;

	private final MythologicalRitualFeature mythologicalRitual;

	private Warp targetWarp;
	private long lastTargetWarpTime;
	private boolean hasInquisitor = false;

	NearestWarp(MythologicalRitualFeature mythologicalRitual) {
		this.mythologicalRitual = mythologicalRitual;
		this.targetWarp = null;
		this.lastTargetWarpTime = 0;
	}

	void reset() {
		targetWarp = null;
		lastTargetWarpTime = 0;
		hasInquisitor = false;
	}

	void onInquisitor() {
		hasInquisitor = true;
		TickScheduler.getInstance().runLater(() -> hasInquisitor = false, 20, TimeUnit.SECONDS);
	}

	void warpToNearestWarp() {
		if (!ConfigManager.getConfig().events.mythologicalRitual.nearestWarp) return;
		if (targetWarp == null) return;
		if (hasInquisitor) return;
		if (System.currentTimeMillis() - lastTargetWarpTime < 5000) return;

		lastTargetWarpTime = System.currentTimeMillis();
		Client.sendChatMessage("/warp " + targetWarp.toString());
		Client.clearTitleAndSubtitle();
		targetWarp = null;
	}

	void shouldUseNearestWarp(@NotNull Vec3d target) {
		if (CLIENT.player == null) {
			targetWarp = null;
			return;
		}

		if (hasInquisitor) {
			return;
		}

		final Vec3d playerPos = CLIENT.player.getPos();
		final double playerToTargetSq = playerPos.squaredDistanceTo(target);

		// Si le joueur est déjà proche du target, pas de Warp
		if (playerToTargetSq <= CLOSE_TO_TARGET_DISTANCE_SQ) {
			targetWarp = null;
			return;
		}

		// Trouver le Warp le plus proche du target
		Warp bestWarp = Arrays.stream(Warp.values())
				.filter(Warp::isEnabled) // ignore les Warp désactivés dans la config
				.min(Comparator.comparingDouble(w -> w.getPosition().squaredDistanceTo(target)))
				.orElse(null);

		if (bestWarp == null) {
			targetWarp = null;
			return;
		}

		double warpToTargetSq = bestWarp.getPosition().squaredDistanceTo(target);

		// Suggère un Warp que si la distance entre le point de Warp
		// et le target est au moins MIN_IMPROVEMENT_BLOCKS plus proche
		// que la distance actuelle entre le joueur et le target.
		if (playerToTargetSq > warpToTargetSq + MIN_IMPROVEMENT_BLOCKS_SQ) {
			targetWarp = bestWarp;
			show();
		} else {
			targetWarp = null;
		}
	}

	private void show() {
		if (targetWarp != null) {
			Client.showTitleAndSubtitle(
					Text.literal("Warp > " + targetWarp.getDisplayName()).formatted(Formatting.AQUA),
					Text.literal("Press " + mythologicalRitual.getNearestWarpBoundKeyLocalized()).formatted(Formatting.GRAY, Formatting.ITALIC),
					0, 40, 0
			);
		}
	}

	private enum Warp {
		HUB("Hub", new Vec3d(-3, 70, -70), () -> true),
		CRYPT("Crypt", new Vec3d(190, 75, -88), () -> ConfigManager.getConfig().events.mythologicalRitual.warpToCrypt),
		CASTLE("Castle", new Vec3d(-250, 130, 45), () -> true),
		DA("Dark Auction", new Vec3d(90, 75, 170), () -> ConfigManager.getConfig().events.mythologicalRitual.warpToDarkAuction),
		MUSEUM("Museum", new Vec3d(-75, 75, 80), () -> true),
		WIZARD("Wizard", new Vec3d(45, 122, 69), () -> ConfigManager.getConfig().events.mythologicalRitual.warpToWizard),
		STONKS("Stonks", new Vec3d(-50, 70, -50), () -> ConfigManager.getConfig().events.mythologicalRitual.warpToStonks),
		;

		private final String displayName;
		private final Vec3d position;
		private final Supplier<Boolean> enabled;

		Warp(String displayName, Vec3d position, Supplier<Boolean> enabled) {
			this.displayName = displayName;
			this.position = position;
			this.enabled = enabled;
		}

		public Vec3d getPosition() {
			return position;
		}

		public String getDisplayName() {
			return displayName;
		}

		public boolean isEnabled() {
			return enabled.get();
		}
	}
}
