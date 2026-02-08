package fr.siroz.cariboustonks.features.ui.hud;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.TextHud;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class FpsHud extends Feature {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_fps");

	public FpsHud() {
		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(HUD_ID)
				.hud(new TextHud(
						Component.literal("555 FPS"),
						this::getText,
						this.config().uiAndVisuals.fpsHud,
						6,
						64
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	private @NonNull Component getText() {
		return Component.literal(CLIENT.getFps() + " FPS");
	}
}
