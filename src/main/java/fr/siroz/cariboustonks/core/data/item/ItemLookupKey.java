package fr.siroz.cariboustonks.core.data.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a unique key for item lookup, which can be based on either a Neu ID or a Hypixel SkyBlock ID.
 * This class is immutable and ensures that at least one of the two identifiers (Neu ID or Hypixel SkyBlock ID)
 * is specified when creating an instance.
 */
public final class ItemLookupKey {

    private final @Nullable String neuId;
    private final @Nullable String hypixelSkyBlockId;

    private ItemLookupKey(@Nullable String neuId, @Nullable String hypixelSkyBlockId) {
        this.neuId = neuId;
        this.hypixelSkyBlockId = hypixelSkyBlockId;
    }

    /**
     * Creates a new {@link ItemLookupKey} instance with the provided Hypixel SkyBlock ID.
     *
     * @param hypixelSkyBlockId the unique identifier of the item in Hypixel SkyBlock.
     * @return a new {@link ItemLookupKey} instance that uses the provided Hypixel SkyBlock ID.
     */
    public static @NotNull ItemLookupKey ofHypixelSkyBlockId(@NotNull String hypixelSkyBlockId) {
        return new ItemLookupKey(null, hypixelSkyBlockId);
    }

    /**
     * Creates a new {@link ItemLookupKey} instance with the provided NEU ID.
     *
     * @param neuId the unique identifier of the item in the NEU ID system.
     * @return a new {@link ItemLookupKey} instance that uses the provided NEU ID.
     */
    public static @NotNull ItemLookupKey ofNeuId(@NotNull String neuId) {
        return new ItemLookupKey(neuId, null);
    }

    /**
     * Creates a new {@link ItemLookupKey} instance with the provided Neu ID and/or Hypixel SkyBlock ID.
     * Either of the parameters can be null, but at least one should be non-null to uniquely identify the item.
     *
     * @param neuId             the unique identifier of the item in the NEU ID system, or null if not applicable
     * @param hypixelSkyBlockId the unique identifier of the item in the Hypixel SkyBlock system, or null if not applicable
     * @return a new {@link ItemLookupKey} instance built with the specified identifiers
     */
    public static @NotNull ItemLookupKey of(@Nullable String neuId, @Nullable String hypixelSkyBlockId) {
        return new ItemLookupKey(neuId, hypixelSkyBlockId);
    }

    /**
     * Retrieves the NEU ID associated with this item lookup key. The NEU ID is a unique identifier
     * for the item in the NEU (NotEnoughUpdates) system.
     *
     * @return the NEU ID if it exists, or null if no ID is associated.
     */
    public @Nullable String getNeuId() {
        return neuId;
    }

    /**
     * Retrieves the Hypixel SkyBlock ID associated with this item lookup key.
     * The Hypixel SkyBlock ID is a unique identifier for the item in the Hypixel SkyBlock system.
     *
     * @return the Hypixel SkyBlock ID if it exists, or null if no ID is associated.
     */
    public @Nullable String getHypixelSkyBlockId() {
        return hypixelSkyBlockId;
    }

    /**
     * Determines whether this {@code ItemLookupKey} instance is null by checking if both
     * the NEU ID and Hypixel SkyBlock ID are null.
     *
     * @return {@code true} if both the NEU ID and Hypixel SkyBlock ID are null, {@code false} otherwise.
     */
    public boolean isNull() {
        return neuId == null && hypixelSkyBlockId == null;
    }

    @Override
    public String toString() {
        return "ItemLookupKey{" +
                "neuId='" + neuId + '\'' +
                ", hypixelSkyBlockId='" + hypixelSkyBlockId + '\'' +
                '}';
    }
}
