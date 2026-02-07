package fr.siroz.cariboustonks.feature.ui.hud;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.TextHud;
import fr.siroz.cariboustonks.system.NetworkSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class PingHud extends Feature {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_ping");

	private final NetworkSystem networkSystem;

	public PingHud() {
		this.networkSystem = CaribouStonks.systems().getSystem(NetworkSystem.class);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(HUD_ID)
				.hud(new TextHud(
						Component.literal("0 ms"),
						this::getText,
						this.config().uiAndVisuals.pingHud,
						58,
						8
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	private @NonNull Component getText() {
		long currentPing = networkSystem.getPing();
		String pingStr = currentPing + " ms";

		int step = Math.min((int) currentPing / 150, 3); // // 0, 150, 300, 450
		int color = getPingColor(step) | 0xFF000000;

		return Component.literal(pingStr).withStyle(style -> style.withColor(color));
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
