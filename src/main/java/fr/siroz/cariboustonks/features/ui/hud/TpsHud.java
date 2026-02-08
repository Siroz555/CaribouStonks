package fr.siroz.cariboustonks.features.ui.hud;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.TextHud;
import fr.siroz.cariboustonks.system.NetworkSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class TpsHud extends Feature {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_tps");

	private final NetworkSystem networkSystem;

	private int lastTruncatedTps = -1;
	private String cachedText = null;

	public TpsHud() {
		this.networkSystem = CaribouStonks.systems().getSystem(NetworkSystem.class);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(HUD_ID)
				.hud(new TextHud(
						Component.literal("TPS: 20.0"),
						this::getText,
						this.config().uiAndVisuals.tpsHud,
						6,
						8
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	private @NonNull Component getText() {
		float tps = networkSystem.getTickRate();

		// Troncature à une décimale (sans arrondi)
		int truncatedTps = (int) (tps * 10);
		if (truncatedTps != lastTruncatedTps) {
			int integerPart = truncatedTps / 10;
			int decimalPart = truncatedTps % 10;

			cachedText = "TPS: " + integerPart + '.' + decimalPart;
			lastTruncatedTps = truncatedTps;
		}

		int textColor = getTickRateColor(tps) | 0xFF000000;
		return Component.literal(cachedText).withStyle(style -> style.withColor(textColor));
	}

	private int getTickRateColor(float tps) {
		if (tps >= 19.55F) return 0x85F290;
		if (tps >= 17.72F) return 0xECF285;
		return 0xFF5C71;
	}
}
