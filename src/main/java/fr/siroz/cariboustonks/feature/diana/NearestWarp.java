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

    private Warp targetWarp;
    private long lastTargetWarpTime;

    public NearestWarp() {
        this.targetWarp = null;
        this.lastTargetWarpTime = 0;
    }

    public void warpToNearestWarp() {
		if (!ConfigManager.getConfig().events.mythologicalRitual.nearestWarp) return;
        if (targetWarp == null) return;

        if (System.currentTimeMillis() - lastTargetWarpTime < 5000) {
			return;
		}

        lastTargetWarpTime = System.currentTimeMillis();
        Client.sendChatMessage("/warp " + targetWarp.toString());
        targetWarp = null;
    }

    public void shouldUseNearestWarp(@NotNull Vec3d target) {
        if (CLIENT.player == null) {
            targetWarp = null;
            return;
        }

        final Vec3d playerPosition = CLIENT.player.getPos();
        targetWarp = Arrays.stream(Warp.values())
                .min(Comparator.comparingDouble(w -> w.getPosition().squaredDistanceTo(target)))
                .filter(w -> w.getPosition().squaredDistanceTo(playerPosition) > 25600) // 160
                .orElse(null);

        show();
    }

    private void show() {
        if (this.targetWarp == null) {
			return;
		}

        CLIENT.inGameHud.setTitle(Text.literal("Warp > " + this.targetWarp.getDisplayName()).formatted(Formatting.AQUA));
        CLIENT.inGameHud.setSubtitle(Text.literal("Press F").formatted(Formatting.GRAY, Formatting.ITALIC));
        CLIENT.inGameHud.setTitleTicks(0, 60, 0);
    }

    public void reset() {
        targetWarp = null;
        lastTargetWarpTime = 0;
    }

    public enum Warp {
        HUB("Hub", new Vec3d(-3, 70, -70)),
        CASTLE("Castle", new Vec3d(-250, 130, 45)),
        DA("Dark Auction", new Vec3d(90, 75, 170)),
        MUSEUM("Museum", new Vec3d(-75, 75, 80)),
        WIZARD("Wizard", new Vec3d(45, 122, 69)),
        STONKS("Stonks", new Vec3d(-50, 70.0, -50)),
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
