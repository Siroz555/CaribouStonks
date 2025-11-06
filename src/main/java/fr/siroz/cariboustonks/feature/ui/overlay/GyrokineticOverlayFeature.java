package fr.siroz.cariboustonks.feature.ui.overlay;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.colors.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class GyrokineticOverlayFeature extends Feature {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final String GYROKINETIC_ITEM_ID = "GYROKINETIC_WAND";
	private static final int REACH = 24;
	private static final int RADIUS = 10;

	public GyrokineticOverlayFeature() {
		RenderEvents.WORLD_RENDER.register(this::render);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().uiAndVisuals.overlay.gyrokineticWand;
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER")
	public void render(WorldRenderer renderer) {
		if (CLIENT.player == null || CLIENT.world == null || CLIENT.getCameraEntity() == null) return;
		if (!isEnabled()) return;

		String skyBlockId = SkyBlockAPI.getSkyBlockItemId(CLIENT.player.getMainHandStack());
		if (!GYROKINETIC_ITEM_ID.equals(skyBlockId)) {
			return;
		}

		HitResult hitResult = CLIENT.getCameraEntity().raycast(REACH, 1.0F, false);
		if (hitResult.getType() == HitResult.Type.MISS) {
			return;
		}

		Vec3d position = hitResult.getPos().subtract(0, 0.1D, 0);
		renderer.submitCircle(position, RADIUS, 16, .015f, Colors.MAGENTA, Direction.Axis.Y, true);
	}
}
