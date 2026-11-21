package fr.siroz.cariboustonks.features.misc;

import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.cooldown.Cooldown;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.Rarity;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockItems;
import fr.siroz.cariboustonks.events.ClientEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ItemProtectionFeature extends Feature {
	private static final int OUT_OF_BOUNDS_SLOT = -999;
	private static final int MOD_SLOT = -555;
	private static final int WARNING_THRESHOLD = 3;

	private static final Cooldown ATTEMPT_WINDOW = Cooldown.of(5, TimeUnit.SECONDS);
	private static final Cooldown WARNING_COOLDOWN = Cooldown.of(2500, TimeUnit.MILLISECONDS);

	private int blockedAttempts = 0;

	public ItemProtectionFeature() {
		ClientEvents.ALLOW_DROP_ITEM_EVENT.register(this::allowDropItem);
		ClientEvents.ALLOW_DROP_ITEM_IN_SCREEN_EVENT.register(this::allowDropItemInScreen);
	}

	@Override
	public boolean isEnabled() {
		return this.skyBlock().location().onSkyBlock() && this.config().general.itemProtection.enabled;
	}

	@EventHandler(event = "ClientEvents.ALLOW_DROP_ITEM_EVENT")
	private boolean allowDropItem(int slotId, @Nullable ItemStack stack) {
		if (!isEnabled()) return true;
		if (stack == null || stack.isEmpty()) return true;

		// Ici, on n'est jamais dans un Screen, toujours via le "GameScreen" sans surcouche GUI (inventaire...)
		// Drop "direct" en jeu, coté Hotbar.
		if (slotId >= 0 && slotId <= 7 && isProtected(stack, true)) {
			onDropBlocked();
			return false;
		}

		return true;
	}

	@EventHandler(event = "ClientEvents.ALLOW_DROP_ITEM_IN_SCREEN_EVENT")
	private boolean allowDropItemInScreen(int slotId, @Nullable ItemStack stack) {
		if (!isEnabled()) return true;
		if (stack == null || stack.isEmpty()) return true;
		if (this.config().general.itemProtection.onlyHotbarMode) return true;

		// Virer l'item de l'inventaire
		if (slotId == OUT_OF_BOUNDS_SLOT && isProtected(stack, false)) {
			onDropBlocked();
			return false;
		}

		// Dédié uniquement au Mod, si l'item est drop de facon "général"
		if (slotId == MOD_SLOT && isProtected(stack, false)) {
			onDropBlocked();
			return false;
		}

		return true;
	}

	private boolean isProtected(ItemStack stack, boolean hotbarDrop) {
		String uuid = SkyBlockItems.getSkyBlockItemUuid(stack);
		if (uuid.isBlank()) return false;

		Rarity rarity = SkyBlockItems.getRarity(stack);
		if (rarity == Rarity.UNKNOWN) return false;

		boolean isProtected = rarity.getPower() >= this.config().general.itemProtection.minIncluisveRarity.getPower();

		// Drop "direct" en jeu, n'est pas considéré comme protégé si l'option Dungeon ou Special Cases est enabled
		if (hotbarDrop) {
			// --- Dungeons ---
			boolean shouldDropInDungeon
					= this.skyBlock().location().isOn(IslandType.DUNGEON)
					&& this.config().general.itemProtection.shouldDropInDungeon;
			if (shouldDropInDungeon) return false;

			// --- Special Cases ---
			String skyBlockId = SkyBlockItems.getSkyBlockItemId(stack);
			boolean shouldDropForSpecialCases
					= this.config().general.itemProtection.shouldDropForSpecialCases
					&& ("SHINING_COIN".equals(skyBlockId) || this.skyBlock().location().isOn(IslandType.SAFARI));

			if (shouldDropForSpecialCases) return false;
		}

		return isProtected;
	}

	private void onDropBlocked() {
		// Trop de temps depuis la dernière tentative
		if (ATTEMPT_WINDOW.testSilently()) blockedAttempts = 0;
		ATTEMPT_WINDOW.reset();

		if (++blockedAttempts < WARNING_THRESHOLD) return;
		blockedAttempts = 0;

		if (WARNING_COOLDOWN.test()) {
			PlayerContext.sendMessageWithPrefix(Component.literal("Item Protection has blocked the drop of a protected item.").withColor(Colors.RED_RGB));
			PlayerContext.sendMessage(Component.literal(" Disable this in General > Item Protection").withStyle(ChatFormatting.DARK_GRAY));
			PlayerContext.sendMessage(Component.empty());
		}
	}
}
