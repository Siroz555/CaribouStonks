package fr.siroz.cariboustonks.feature.fishing;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.feature.FeatureManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.util.Client;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FishCaughtFeature extends Feature {

	private static final String CAUGHT_FISH_NAME = "!!!";

	private @Nullable RareSeaCreatureFeature rareSeaCreatureFeature;

	public FishCaughtFeature() {
		NetworkEvents.ARMORSTAND_UPDATE_PACKET.register(this::onArmorStandUpdate);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().fishing.fishCaughtWarning;
	}

	@Override
	protected void postInitialize(@NonNull FeatureManager features) {
		rareSeaCreatureFeature = features.getFeature(RareSeaCreatureFeature.class);
	}

	@EventHandler(event = "NetworkEvents.ARMORSTAND_UPDATE_PACKET")
	private void onArmorStandUpdate(@NonNull ArmorStand armorStand, boolean equipment) {
		if (CLIENT.player == null || CLIENT.level == null || CLIENT.player.fishing == null) return;
		if (equipment || !armorStand.isInvisible() || !armorStand.hasCustomName() || !armorStand.isCustomNameVisible()) return;
		if (!isEnabled()) return;

		String name = armorStand.getCustomName() != null ? armorStand.getCustomName().getString() : "";
		if (name.equals(CAUGHT_FISH_NAME) && CLIENT.player.fishing.getBoundingBox().inflate(4D).contains(armorStand.position())) {
			// Priorité pour RareSeaCreatureFeature
			if (rareSeaCreatureFeature == null || !rareSeaCreatureFeature.hasFoundCreature()) {
				Client.showTitle(Component.literal(CAUGHT_FISH_NAME).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), 1, 25, 1);
			}
		}
	}
}
