package fr.siroz.cariboustonks.feature.ui.hud;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.hud.Hud;
import fr.siroz.cariboustonks.manager.hud.HudProvider;
import fr.siroz.cariboustonks.manager.hud.TextHud;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class DayHud extends Feature implements HudProvider {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_day");
	private final Hud hud;

	public DayHud() {
		this.hud = new TextHud(
				Text.literal("Day: 27"),
				this::getText,
				ConfigManager.getConfig().uiAndVisuals.dayHud,
				20,
				64
		);
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
		return hud;
	}

	@Contract(" -> new")
	private @NotNull Text getText() {
		long day = CLIENT.world != null ? CLIENT.world.getTimeOfDay() / 24000 : 0L;
		return Text.literal("Day: " + day);
	}
}
