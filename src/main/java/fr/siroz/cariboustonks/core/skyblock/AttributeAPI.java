package fr.siroz.cariboustonks.core.skyblock;

import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.Rarity;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockAttribute;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class AttributeAPI {

	public static final int MAX_LEVEL = 10;
	public static final String HUNTING_BOX = "Hunting Box";
	public static final String ATTRIBUTE_MENU = "Attribute Menu";
	public static final String FUSION_BOX = "Fusion Box";
	public static final String SHARD_FUSION = "Shard Fusion";
	public static final String CONFIRM_FUSION = "Confirm Fusion";

	public static final Pattern SHARD_WITH_QUANTITY_PATTERN = Pattern.compile("[A-Za-z ]+ Shard(?: x(?<amount>\\d+))?");
	public static final Pattern SOURCE_PATTERN = Pattern.compile("Source: (?<shardName>[A-Za-z ]+?) Shard \\((?<id>[CUREL]\\d+)\\)");
	public static final Pattern RARITY_AND_ID_PATTERN = Pattern.compile("(COMMON|UNCOMMON|RARE|EPIC|LEGENDARY).*?SHARD \\(ID ([CUREL]\\d+)\\)");

	private AttributeAPI() {
	}

	private static Function<String, SkyBlockAttribute> byName;
	private static Function<String, SkyBlockAttribute> byId;

	static void bootstrap(
			@NonNull Function<String, SkyBlockAttribute> byNameFactory,
			@NonNull Function<String, SkyBlockAttribute> byIdFactory
	) {
		byName = byNameFactory;
		byId = byIdFactory;
	}

	/**
	 * Returns the {@code SkyBlock API ID} of the ItemStack in Hunting Box, Attribute Menu, Fusion Machine GUIs.
	 * <p>
	 * Depuis la The Foraging Update 0.23, dans l'inventaire ou ou Bazaar par exemple, les Shards sont des
	 * ATTRIBUTE_SHARD avec ID, mais dans les GUIs comme l'Hunting Box, ce sont des PLAYER_HEAD sans ID...
	 * Le skyBlockApiId n'est pas récupérable, il n'est pas présent, il faut donc trouver l'ID à partir
	 * du nom de l'item ou dans le lore, à partir du fichier "ATTRIBUTES_JSON" accessible via le ModDataSource.
	 * SkyHanni / Skyblocker et les autres, utilisent ce fichier "ATTRIBUTES_JSON".
	 *
	 * @param fallback the skyBlockApiId fallback
	 * @param item     the ItemStack
	 * @param lines    the lore of the ItemStack
	 * @return the skyBlockApiId or the fallback
	 */
	public static String getSkyBlockApiIdFromNewShard(@NonNull String fallback, ItemStack item, List<Component> lines) {
		Screen currentScreen = Minecraft.getInstance().screen;
		if (!fallback.isEmpty() || currentScreen == null) return fallback;

		String title = currentScreen.getTitle().getString();
		switch (title) {
			case HUNTING_BOX -> {
				String name = item.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty()).getString();
				SkyBlockAttribute attribute = byName != null ? byName.apply(name) : null;
				return attribute != null ? attribute.skyBlockApiId() : fallback;
			}
			case ATTRIBUTE_MENU -> {
				String id = null;
				for (Component line : lines) {
					String lineText = line.getString();
					if (lineText.isEmpty()) {
						continue;
					}

					Matcher attributeIdMatcher = SOURCE_PATTERN.matcher(lineText);
					if (attributeIdMatcher.matches()) {
						id = attributeIdMatcher.group("id");
						break;
					}
				}

				return getAttributeId(id, fallback);
			}
			case FUSION_BOX, SHARD_FUSION, CONFIRM_FUSION -> {
				String id = null;
				for (Component line : lines) {
					String lineText = line.getString();
					if (lineText.isEmpty()) {
						continue;
					}

					Matcher attributeRarityAndIdMatcher = RARITY_AND_ID_PATTERN.matcher(lineText);
					if (attributeRarityAndIdMatcher.matches()) {
						id = attributeRarityAndIdMatcher.group(2);
						break;
					}
				}

				return getAttributeId(id, fallback);
			}
			default -> {
				// Les rewards chest de Dungeon & Kuudra
				if (SkyBlockConstants.DUNGEON_CHESTS.contains(title) || SkyBlockConstants.KUUDRA_CHESTS.contains(title)) {
					String name = item.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty()).getString();
					Matcher matcher = SHARD_WITH_QUANTITY_PATTERN.matcher(name);
					if (name.contains("Shard") && matcher.matches()) {
						SkyBlockAttribute attribute = byName != null ? byName.apply(name) : null;
						return attribute != null ? attribute.skyBlockApiId() : fallback;
					}
				}
				return fallback;
			}
		}
	}

	public static @Nullable SkyBlockAttribute getAttributeByName(@Nullable String name) {
		return byName != null ? byName.apply(name) : null;
	}

	public static @Nullable SkyBlockAttribute getAttributeById(@Nullable String id) {
		return byId != null ? byId.apply(id) : null;
	}

	private static String getAttributeId(@Nullable String id, @NonNull String fallback) {
		SkyBlockAttribute attribute = getAttributeById(id);
		return attribute != null ? attribute.skyBlockApiId() : fallback;
	}

	public static int getShardsUntilMax(Rarity rarity, int level) {
		if (level == MAX_LEVEL) return 0;

		Int2IntMap level2Count = SkyBlockConstants.ATTRIBUTE_LEVELS.get(rarity);
		if (level2Count == null) {
			return -1;
		}

		int currentShardCount = level2Count.getOrDefault(level, -1);
		if (currentShardCount == -1) {
			return -1;
		}

		int maxShardCount = level2Count.getOrDefault(MAX_LEVEL, -1);
		if (maxShardCount == -1) {
			return -1;
		}

		return maxShardCount - currentShardCount;
	}
}
