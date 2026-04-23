package fr.siroz.cariboustonks.core.skyblock.data.hypixel.item;

import org.jspecify.annotations.NonNull;

/**
 * Thrown when a {@link SkyBlockItemData} cannot be parsed from a {@code JsonObject}.
 */
public final class SkyBlockItemParseException extends Exception {
	public SkyBlockItemParseException(@NonNull String skyBlockId, @NonNull Throwable cause) {
		super("Failed to parse SkyBlock item: " + skyBlockId, cause);
	}
}
