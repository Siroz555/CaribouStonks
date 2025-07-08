package fr.siroz.cariboustonks.feature.hunting;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class FishingNetPlacementFeature extends Feature {

	private static final String FISHING_NET = "Fishing Net";

	public FishingNetPlacementFeature() {
		UseBlockCallback.EVENT.register(this::onBlockPlace);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().hunting.cancelFishingNetPlacement;
	}

	@EventHandler(event = "UseBlockCallback.EVENT")
	private ActionResult onBlockPlace(PlayerEntity player, World _world, Hand hand, BlockHitResult _hitResult) {
		if (!isEnabled()) {
			return ActionResult.PASS;
		}

		ItemStack heldItem = player.getStackInHand(hand);
		if (heldItem.getItem() == Items.COBWEB && heldItem.getName().getString().contains(FISHING_NET)) {
			return ActionResult.FAIL;
		}

		return ActionResult.PASS;
	}
}
