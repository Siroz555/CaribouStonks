package fr.siroz.cariboustonks.feature.fishing;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.feature.Features;
import fr.siroz.cariboustonks.util.Client;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BobberTimerFeature extends Feature {

	private static final Pattern TIMER_PATTERN = Pattern.compile("\\d.\\d");
	private static final double MIN_BOBBER_TIMER_DISTANCE = 0.1D;

	@Nullable
	private RareSeaCreatureFeature rareSeaCreatureFeature;

	@Nullable
	private ArmorStandEntity bobberTimerArmorStand;

	public BobberTimerFeature() {
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> this.bobberTimerArmorStand = null);
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorStandUpdate);
		ClientTickEvents.END_CLIENT_TICK.register(_client -> this.update());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().fishing.bobberTimerDisplay;
	}

	@Override
	protected void postInitialize(@NotNull Features features) {
		rareSeaCreatureFeature = features.getFeature(RareSeaCreatureFeature.class);
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorStandUpdate(@NotNull ArmorStandEntity armorStand, boolean equipment) {
		if (equipment || !armorStand.hasCustomName() || armorStand.getName() == null) return;
		if (CLIENT.player == null || CLIENT.player.fishHook == null) return;
		if (!isEnabled()) return;

		if (bobberTimerArmorStand != null) {
			return;
		}

		if (armorStand.getPos().distanceTo(CLIENT.player.fishHook.getPos()) > MIN_BOBBER_TIMER_DISTANCE) {
			return;
		}

		Matcher timerMatcher = TIMER_PATTERN.matcher(armorStand.getName().getString());
		if (timerMatcher.matches()) {
			bobberTimerArmorStand = armorStand;
		} else {
			bobberTimerArmorStand = null;
		}
	}

	@EventHandler(event = "ClientTickEvents.END_CLIENT_TICK")
	private void update() {
		if (bobberTimerArmorStand == null || !bobberTimerArmorStand.isAlive() || !bobberTimerArmorStand.hasCustomName()) {
			bobberTimerArmorStand = null; // reset
			return;
		}

		if (CLIENT.player == null || CLIENT.player.fishHook == null || bobberTimerArmorStand == null) {
			return;
		}

		Text bobberTimerText = bobberTimerArmorStand.getCustomName();
		if (isEnabled() && bobberTimerText != null) {
			// Priorité pour RareSeaCreatureFeature
			if (rareSeaCreatureFeature == null || !rareSeaCreatureFeature.hasFoundCreature()) {
				Client.showSubtitle(bobberTimerText, 0, 25, 1);
			}
		}
	}
}
