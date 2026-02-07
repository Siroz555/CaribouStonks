package fr.siroz.cariboustonks.core.skyblock.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.NonNull;

/**
 * Represents an SkyBlock ItemStack.
 *
 * @param skyBlockId   the SkyBlock item's ID or an empty string
 * @param amount       the item's amount
 * @param metadata     the item's metadata
 */
public record SkyblockItemStack(
		@NonNull String skyBlockId,
		int amount,
		@NonNull ItemMetadata metadata
) {

	/**
	 * Parses the {@code SkyblockItemStack} from the given {@code ItemStack}.
	 *
	 * @param itemStack the {@code ItemStack} to parse
	 * @return the {@code SkyblockItemStack} parsed from the given {@code ItemStack}
	 */
	public static @NonNull SkyblockItemStack of(@NonNull ItemStack itemStack) {
		CompoundTag customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		String skyBlockId = customData.getStringOr("id", "");
		return new SkyblockItemStack(skyBlockId, itemStack.getCount(), ItemMetadata.ofNbt(customData));
	}
}
