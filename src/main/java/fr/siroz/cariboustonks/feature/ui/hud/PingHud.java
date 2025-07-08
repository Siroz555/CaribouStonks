package fr.siroz.cariboustonks.feature.ui.hud;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.hud.Hud;
import fr.siroz.cariboustonks.manager.hud.HudProvider;
import fr.siroz.cariboustonks.manager.hud.TextHud;
import fr.siroz.cariboustonks.manager.network.NetworkManager;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class PingHud extends Feature implements HudProvider {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_ping");

	private final Hud hud;

	private final NetworkManager networkManager;

	public PingHud() {
		this.networkManager = CaribouStonks.managers().getManager(NetworkManager.class);
		this.hud = new TextHud(
				Text.literal("0 ms"),
				this::getText,
				ConfigManager.getConfig().uiAndVisuals.pingHud,
				58,
				8
		);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public @NotNull Pair<Identifier, Identifier> getAttachLayerAfter() {
		return Pair.of(IdentifiedLayer.STATUS_EFFECTS, HUD_ID);
	}

	@Override
	public @NotNull Hud getHud() {
		return hud;
	}

	private Text getText() {
		long currentPing = networkManager.getPing();
		String pingStr = currentPing + " ms";

		int step = Math.min((int) currentPing / 150, 3); // // 0, 150, 300, 450
		int color = getPingColor(step) | 0xFF000000;

		return Text.literal(pingStr).styled(style -> style.withColor(color));
	}

	private int getPingColor(int step) {
		return switch (step) {
			case 0 -> 0x85F290;
			case 1 -> 0xECF285;
			case 2 -> 0xFEBC49;
			case 3 -> 0xFF5C71;
			default -> 0xFFFFFFFF;
		};
	}
}
