package fr.siroz.cariboustonks.feature.fishing;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.feature.Features;
import fr.siroz.cariboustonks.util.Client;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BobberTimerFeature extends Feature {

	private static final Pattern TIMER_PATTERN = Pattern.compile("\\d.\\d");
	private static final double MIN_BOBBER_TIMER_DISTANCE = 0.1D;

	@Nullable
	private RareSeaCreatureFeature rareSeaCreatureFeature;

	@Nullable
	private ArmorStand bobberTimerArmorStand;

	public BobberTimerFeature() {
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorStandUpdate);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().fishing.bobberTimerDisplay;
	}

	@Override
	protected void postInitialize(@NotNull Features features) {
		rareSeaCreatureFeature = features.getFeature(RareSeaCreatureFeature.class);
	}

	@Override
	protected void onClientJoinServer() {
		bobberTimerArmorStand = null;
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorStandUpdate(@NotNull ArmorStand armorStand, boolean equipment) {
		if (equipment || !armorStand.hasCustomName()) return;
		if (CLIENT.player == null || CLIENT.player.fishing == null) return;
		if (!isEnabled()) return;

		if (bobberTimerArmorStand != null) {
			return;
		}

		if (armorStand.position().distanceTo(CLIENT.player.fishing.position()) > MIN_BOBBER_TIMER_DISTANCE) {
			return;
		}

		Matcher timerMatcher = TIMER_PATTERN.matcher(armorStand.getName().getString());
		if (timerMatcher.matches()) {
			bobberTimerArmorStand = armorStand;
		} else {
			bobberTimerArmorStand = null;
		}
	}

	@Override
	protected void onClientTick() {
		if (bobberTimerArmorStand == null || !bobberTimerArmorStand.isAlive() || !bobberTimerArmorStand.hasCustomName()) {
			bobberTimerArmorStand = null; // reset
			return;
		}

		if (CLIENT.player == null || CLIENT.player.fishing == null || bobberTimerArmorStand == null) {
			return;
		}

		Component bobberTimerText = bobberTimerArmorStand.getCustomName();
		if (isEnabled() && bobberTimerText != null) {
			// Priorité pour RareSeaCreatureFeature
			if (rareSeaCreatureFeature == null || !rareSeaCreatureFeature.hasFoundCreature()) {
				Client.showSubtitle(bobberTimerText, 0, 25, 1);
			}
		}
	}
}
