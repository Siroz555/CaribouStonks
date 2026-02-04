package fr.siroz.cariboustonks.feature.ui.hud;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.TextHud;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FpsHud extends Feature {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_fps");

	public FpsHud() {
		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(HUD_ID)
				.hud(new TextHud(
						Component.literal("555 FPS"),
						this::getText,
						ConfigManager.getConfig().uiAndVisuals.fpsHud,
						6,
						64
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Contract(" -> new")
	private @NotNull Component getText() {
		return Component.literal(CLIENT.getFps() + " FPS");
	}
}
