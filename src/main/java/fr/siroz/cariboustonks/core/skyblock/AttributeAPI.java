package fr.siroz.cariboustonks.core.skyblock;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.data.mod.SkyBlockAttribute;
import fr.siroz.cariboustonks.util.Rarity;
import fr.siroz.cariboustonks.util.StonksUtils;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AttributeAPI {

	public static final int MAX_LEVEL = 10;
	public static final String HUNTING_BOX = "Hunting Box";
	public static final String ATTRIBUTE_MENU = "Attribute Menu";
	public static final String FUSION_BOX = "Fusion Box";
	public static final String SHARD_FUSION = "Shard Fusion";
	public static final Pattern SHARD_GUI_PATTERN = Pattern.compile("(" + HUNTING_BOX + "|" + ATTRIBUTE_MENU + ")");
	public static final Pattern SOURCE_PATTERN = Pattern.compile("Source: (?<shardName>[A-Za-z ]+?) Shard \\((?<id>[CUREL]\\d+)\\)");
	public static final Pattern RARITY_AND_ID_PATTERN = Pattern.compile("(COMMON|UNCOMMON|RARE|EPIC|LEGENDARY).*?SHARD \\(ID ([CUREL]\\d+)\\)");

	private static final Map<Rarity, Int2IntMap> RARITY_2_LEVELS = new EnumMap<>(Rarity.class);

	private AttributeAPI() {
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
	public static String getSkyBlockApiIdFromNewShard(@NotNull String fallback, ItemStack item, List<Text> lines) {
		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (!fallback.isEmpty() || currentScreen == null) return fallback;
		if (!item.isOf(Items.PLAYER_HEAD)) return fallback; // pas sûr de cette verification là, à voir

		String title = currentScreen.getTitle().getString();
		switch (title) {
			case HUNTING_BOX -> {
				String name = item.getOrDefault(DataComponentTypes.CUSTOM_NAME, Text.empty()).getString();
				SkyBlockAttribute attribute = CaribouStonks.core().getModDataSource().getAttributeByShardName(name);
				return attribute != null ? attribute.skyBlockApiId() : fallback;
			}
			case ATTRIBUTE_MENU -> {
				String id = null;
				for (Text line : lines) {
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
			case FUSION_BOX, SHARD_FUSION -> {
				String id = null;
				for (Text line : lines) {
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
			case null, default -> {
				return fallback;
			}
		}
	}

	private static String getAttributeId(@Nullable String id, @NotNull String fallback) {
		SkyBlockAttribute attribute = CaribouStonks.core().getModDataSource().getAttributeById(id);
		return attribute != null ? attribute.skyBlockApiId() : fallback;
	}

	@Deprecated
	public static int getShardsUntilMax(Rarity rarity, int level) {
		if (level == MAX_LEVEL) return 0;

		Int2IntMap level2Count = RARITY_2_LEVELS.get(rarity);
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

	static {
		RARITY_2_LEVELS.put(Rarity.COMMON, StonksUtils.make(new Int2IntArrayMap(), map -> {
			map.put(1, 1);
			map.put(2, 4);
			map.put(3, 9);
			map.put(4, 15);
			map.put(5, 22);
			map.put(6, 30);
			map.put(7, 40);
			map.put(8, 54);
			map.put(9, 72);
			map.put(10, 96);
		}));
		RARITY_2_LEVELS.put(Rarity.UNCOMMON, StonksUtils.make(new Int2IntArrayMap(), map -> {
			map.put(1, 1);
			map.put(2, 3);
			map.put(3, 6);
			map.put(4, 10);
			map.put(5, 15);
			map.put(6, 21);
			map.put(7, 28);
			map.put(8, 36);
			map.put(9, 48);
			map.put(10, 64);
		}));
		RARITY_2_LEVELS.put(Rarity.RARE, StonksUtils.make(new Int2IntArrayMap(), map -> {
			map.put(1, 1);
			map.put(2, 3);
			map.put(3, 6);
			map.put(4, 9);
			map.put(5, 13);
			map.put(6, 17);
			map.put(7, 22);
			map.put(8, 28);
			map.put(9, 39);
			map.put(10, 48);
		}));
		RARITY_2_LEVELS.put(Rarity.EPIC, StonksUtils.make(new Int2IntArrayMap(), map -> {
			map.put(1, 1);
			map.put(2, 2);
			map.put(3, 4);
			map.put(4, 6);
			map.put(5, 9);
			map.put(6, 12);
			map.put(7, 16);
			map.put(8, 20);
			map.put(9, 25);
			map.put(10, 32);
		}));
		RARITY_2_LEVELS.put(Rarity.LEGENDARY, StonksUtils.make(new Int2IntArrayMap(), map -> {
			map.put(1, 1);
			map.put(2, 2);
			map.put(3, 3);
			map.put(4, 5);
			map.put(5, 7);
			map.put(6, 9);
			map.put(7, 12);
			map.put(8, 15);
			map.put(9, 19);
			map.put(10, 24);
		}));
	}
}
