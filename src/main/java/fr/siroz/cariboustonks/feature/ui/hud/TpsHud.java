package fr.siroz.cariboustonks.feature.ui.hud;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.hud.Hud;
import fr.siroz.cariboustonks.system.hud.HudProvider;
import fr.siroz.cariboustonks.system.hud.TextHud;
import fr.siroz.cariboustonks.system.network.NetworkSystem;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class TpsHud extends Feature implements HudProvider {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_tps");

	private final NetworkSystem networkSystem;

	private int lastTruncatedTps = -1;
	private String cachedText = null;

	public TpsHud() {
		this.networkSystem = CaribouStonks.systems().getSystem(NetworkSystem.class);
	}

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
				Component.literal("TPS: 20.0"),
				this::getText,
				ConfigManager.getConfig().uiAndVisuals.tpsHud,
				6,
				8
		);
	}

	private Component getText() {
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
