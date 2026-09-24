package fr.siroz.cariboustonks.core.skyblock.item;

import fr.siroz.cariboustonks.core.skyblock.item.metadata.ItemMetadata;
import org.jspecify.annotations.NonNull;

/**
 * Represents an SkyBlock ItemStack.
 *
 * @param skyBlockId   the SkyBlock item's ID or an empty string
 * @param amount       the item's amount
 * @param metadata     the item's metadata
 */
public record SkyblockItemStack(@NonNull String skyBlockId, int amount, @NonNull ItemMetadata metadata) {
}
