package fr.siroz.cariboustonks.feature.ui.hud;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.TextHud;
import fr.siroz.cariboustonks.util.Client;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class DayHud extends Feature {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_day");

	public DayHud() {
		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(HUD_ID)
				.hud(new TextHud(
						Component.literal("Day: 27"),
						this::getText,
						this.config().uiAndVisuals.dayHud,
						20,
						64
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	private @NonNull Component getText() {
		long day = Client.getWorldDay();
		return Component.literal("Day: " + day);
	}
}
