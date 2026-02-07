package fr.siroz.cariboustonks.feature.garden;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.InteractionEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class DisableWateringCanPlacementFeature extends Feature {

	private static final String HYDRO_CAN_ID = "HYDRO_CAN";
	private static final String AQUA_MASTER_ID = "AQUAMASTER";

	public DisableWateringCanPlacementFeature() {
		InteractionEvents.ALLOW_INTERACT_BLOCK.register(this::allowInteractBlock);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.GARDEN
				&& this.config().farming.garden.disableWateringCanPlacement;
	}

	@EventHandler(event = "InteractionEvents.ALLOW_INTERACT_BLOCK")
	private boolean allowInteractBlock(@NonNull ItemStack itemStack) {
		if (!isEnabled()) return true;
		if (itemStack.getItem() != Items.PLAYER_HEAD) return true;

		String skyBlockItemId = SkyBlockAPI.getSkyBlockItemId(itemStack);
		if (skyBlockItemId.isEmpty()) return true;

		return !skyBlockItemId.startsWith(HYDRO_CAN_ID) && !skyBlockItemId.startsWith(AQUA_MASTER_ID);
	}
}
