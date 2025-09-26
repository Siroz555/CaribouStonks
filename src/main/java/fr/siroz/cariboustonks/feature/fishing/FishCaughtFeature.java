package fr.siroz.cariboustonks.feature.fishing;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Client;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class FishCaughtFeature extends Feature {

	private static final String CAUGHT_FISH_NAME = "!!!";

	public FishCaughtFeature() {
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorStandUpdate);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().fishing.fishCaughtWarning;
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorStandUpdate(@NotNull ArmorStandEntity armorStand, boolean equipment) {
		if (CLIENT.player == null || CLIENT.world == null || CLIENT.player.fishHook == null) return;
		if (equipment || !armorStand.isInvisible() || !armorStand.hasCustomName() || !armorStand.isCustomNameVisible()) return;
		if (!isEnabled()) return;

		String name = armorStand.getCustomName() != null ? armorStand.getCustomName().getString() : "";
		if (name.equals(CAUGHT_FISH_NAME) && CLIENT.player.fishHook.getBoundingBox().expand(4D).contains(armorStand.getPos())) {
			Client.showTitle(Text.literal(CAUGHT_FISH_NAME).formatted(Formatting.RED, Formatting.BOLD), 1, 35, 5);
		}
	}
}
