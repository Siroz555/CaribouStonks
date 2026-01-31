package fr.siroz.cariboustonks.feature.ui.hud;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.hud.Hud;
import fr.siroz.cariboustonks.system.hud.HudProvider;
import fr.siroz.cariboustonks.system.hud.TextHud;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FpsHud extends Feature implements HudProvider {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_fps");

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public @NotNull Pair<Identifier, Identifier> getAttachLayerAfter() {
		return Pair.of(VanillaHudElements.STATUS_EFFECTS, HUD_ID);
	}

	@Override
	public @NotNull Hud getHud() {
		return new TextHud(
				Component.literal("555 FPS"),
				this::getText,
				ConfigManager.getConfig().uiAndVisuals.fpsHud,
				6,
				64
		);
	}

	@Contract(" -> new")
	private @NotNull Component getText() {
		return Component.literal(CLIENT.getFps() + " FPS");
	}
}
