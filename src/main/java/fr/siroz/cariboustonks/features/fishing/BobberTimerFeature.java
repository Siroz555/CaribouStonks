package fr.siroz.cariboustonks.features.fishing;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.feature.FeatureManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.util.Client;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BobberTimerFeature extends Feature {

	private static final Pattern TIMER_PATTERN = Pattern.compile("\\d.\\d");
	private static final double MIN_BOBBER_TIMER_DISTANCE = 0.1D;

	private @Nullable RareSeaCreatureFeature rareSeaCreatureFeature;

	private @Nullable ArmorStand bobberTimerArmorStand;

	public BobberTimerFeature() {
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorStandUpdate);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().fishing.bobberTimerDisplay;
	}

	@Override
	protected void postInitialize(@NonNull FeatureManager features) {
		rareSeaCreatureFeature = features.getFeature(RareSeaCreatureFeature.class);
	}

	@Override
	protected void onClientJoinServer() {
		bobberTimerArmorStand = null;
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorStandUpdate(@NonNull ArmorStand armorStand, boolean equipment) {
		if (equipment || !armorStand.hasCustomName()) return;
		if (CLIENT.player == null || CLIENT.player.fishing == null) return;
		if (bobberTimerArmorStand != null) return;
		if (!isEnabled()) return;

		double distance = armorStand.position().distanceTo(CLIENT.player.fishing.position());
		if (distance > MIN_BOBBER_TIMER_DISTANCE) return;

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
