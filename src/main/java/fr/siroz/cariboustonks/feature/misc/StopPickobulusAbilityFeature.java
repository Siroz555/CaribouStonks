package fr.siroz.cariboustonks.feature.misc;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemUtils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class StopPickobulusAbilityFeature extends Feature {

	private static final String PICKOBULUS = "Pickobulus";

	public StopPickobulusAbilityFeature() {
		UseItemCallback.EVENT.register((player, _world, hand) -> this.onUseItem(player, hand));
		UseBlockCallback.EVENT.register((player, _world, hand, _hitResult) -> this.onUseItem(player, hand));
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.PRIVATE_ISLAND
				&& ConfigManager.getConfig().misc.stopPickobulusAbilityOnDynamic;
	}

	@EventHandler(event = "UseItemCallback.EVENT && UseBlockCallback.EVENT")
	private ActionResult onUseItem(PlayerEntity player, Hand hand) {
		if (!isEnabled()) return ActionResult.PASS;

		ItemStack itemStack = player.getStackInHand(hand);
		String ability = ItemUtils.getAbility(itemStack);
		if (ability != null && ability.equalsIgnoreCase(PICKOBULUS)) {
			Client.sendMessageWithPrefix(Text.literal("Blocked the Pickobulus ability from being used on the Private Island."));
			return ActionResult.FAIL;
		}

		return ActionResult.PASS;
	}
}
