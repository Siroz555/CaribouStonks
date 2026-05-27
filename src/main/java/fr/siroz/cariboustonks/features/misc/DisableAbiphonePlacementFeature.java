package fr.siroz.cariboustonks.features.misc;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.InteractionEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class DisableAbiphonePlacementFeature extends Feature {

	private static final String ABIPHONE_ID_PREFIX = "ABIPHONE_";

	public DisableAbiphonePlacementFeature() {
		InteractionEvents.ALLOW_INTERACT_BLOCK_EVENT.register(this::allowInteractBlock);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().misc.disableAbiphonePlacement;
	}

	@EventHandler(event = "InteractionEvents.ALLOW_INTERACT_BLOCK_EVENT")
	private boolean allowInteractBlock(@NonNull ItemStack itemStack) {
		if (!isEnabled()) return true;
		if (itemStack.getItem() != Items.PLAYER_HEAD) return true;

		String skyBlockItemId = SkyBlockAPI.getSkyBlockItemId(itemStack);
		if (skyBlockItemId.isEmpty()) return true;

		return !skyBlockItemId.startsWith(ABIPHONE_ID_PREFIX);
	}
}
