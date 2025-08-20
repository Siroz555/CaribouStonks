package fr.siroz.cariboustonks.feature.diana;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.util.Client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;

final class NearestWarp {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    // Considérer le target comme "suffisamment proche" s'il se trouve < 100 blocs
    private static final double CLOSE_TO_TARGET_DISTANCE_SQ = 100 * 100;
    // Exiger au moins cette "amélioration" (en blocs) pour suggérer un Warp
    private static final double MIN_IMPROVEMENT_BLOCKS_SQ = 80 * 80;

    private Warp targetWarp;
    private long lastTargetWarpTime;

    NearestWarp() {
        this.targetWarp = null;
        this.lastTargetWarpTime = 0;
    }

    public void warpToNearestWarp() {
		if (!ConfigManager.getConfig().events.mythologicalRitual.nearestWarp) return;
		if (targetWarp == null) return;
		if (System.currentTimeMillis() - lastTargetWarpTime < 5000) return;

        lastTargetWarpTime = System.currentTimeMillis();
        Client.sendChatMessage("/warp " + targetWarp.toString());
        targetWarp = null;
    }

    public void shouldUseNearestWarp(@NotNull Vec3d target) {
        if (CLIENT.player == null) {
            targetWarp = null;
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
					Text.literal("Press F").formatted(Formatting.GRAY, Formatting.ITALIC),
					0, 60, 0
			);
		}
    }

    public void reset() {
        targetWarp = null;
        lastTargetWarpTime = 0;
    }

    private enum Warp {
        HUB("Hub", new Vec3d(-3, 70, -70)),
        CASTLE("Castle", new Vec3d(-250, 130, 45)),
        DA("Dark Auction", new Vec3d(90, 75, 170)),
        MUSEUM("Museum", new Vec3d(-75, 75, 80)),
        WIZARD("Wizard", new Vec3d(45, 122, 69)),
        STONKS("Stonks", new Vec3d(-50, 70, -50)),
        ;

        private final String displayName;
        private final Vec3d position;

        Warp(String displayName, Vec3d position) {
            this.displayName = displayName;
            this.position = position;
        }

        public Vec3d getPosition() {
            return position;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
