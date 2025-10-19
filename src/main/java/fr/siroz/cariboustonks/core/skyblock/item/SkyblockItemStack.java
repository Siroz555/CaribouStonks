package fr.siroz.cariboustonks.core.skyblock.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an SkyBlock ItemStack.
 *
 * @param skyBlockId   the SkyBlock item's ID or an empty string
 * @param amount       the item's amount
 * @param metadata     the item's metadata
 */
public record SkyblockItemStack(
		@NotNull String skyBlockId,
		int amount,
		@NotNull ItemMetadata metadata
) {

	/**
	 * Parses the {@code SkyblockItemStack} from the given {@code ItemStack}.
	 *
	 * @param itemStack the {@code ItemStack} to parse
	 * @return the {@code SkyblockItemStack} parsed from the given {@code ItemStack}
	 */
	public static @NotNull SkyblockItemStack of(@NotNull ItemStack itemStack) {
		NbtCompound customData = itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		String skyBlockId = customData.getString("id", "");
		return new SkyblockItemStack(skyBlockId, itemStack.getCount(), ItemMetadata.ofNbt(customData));
	}
}
