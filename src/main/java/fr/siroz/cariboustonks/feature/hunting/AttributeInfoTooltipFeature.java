package fr.siroz.cariboustonks.feature.hunting;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.algo.BazaarItemAnalytics;
import fr.siroz.cariboustonks.core.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.Product;
import fr.siroz.cariboustonks.core.data.mod.SkyBlockAttribute;
import fr.siroz.cariboustonks.core.skyblock.AttributeAPI;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.manager.container.tooltip.ContainerTooltipAppender;
import fr.siroz.cariboustonks.util.InventoryUtils;
import fr.siroz.cariboustonks.core.skyblock.Rarity;
import fr.siroz.cariboustonks.util.RomanNumeralUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttributeInfoTooltipFeature extends Feature implements ContainerMatcherTrait, ContainerTooltipAppender {

	private static final Pattern NAME_AND_LEVEL_PATTERN = Pattern.compile(".*? ([IVX]+) \\(.*\\)");
	private static final Pattern OWNED_PATTERN = Pattern.compile("Owned: ([\\d,]+) Shards?");
	private static final Pattern LEVEL_PATTERN = Pattern.compile("Level: (\\d+)");
	private static final Pattern SYPHON_COUNT_PATTERN = Pattern.compile("Syphon (\\d+) more to level up!");
	private static final Pattern RARITY_PATTERN = Pattern.compile("Rarity: (COMMON|UNCOMMON|RARE|EPIC|LEGENDARY)");
	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("");

	private final HypixelDataSource hypixelDataSource;
	private final int priority;

	public AttributeInfoTooltipFeature(int priority) {
		this.priority = priority;
		this.hypixelDataSource = CaribouStonks.core().getHypixelDataSource();
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().hunting.attributeInfos;
	}

	@Override
	public @Nullable Pattern getTitlePattern() {
		return AttributeAPI.SHARD_GUI_PATTERN;
	}

	@Override
	public void appendToTooltip(@Nullable Slot focusedSlot, @NotNull ItemStack item, @NotNull List<Text> lines) {
		if (hypixelDataSource.isBazaarInUpdate()) return;

		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (focusedSlot == null || currentScreen == null || lines.isEmpty()) return;
		if (InventoryUtils.isEdgeSlot(focusedSlot.id, 6)) return;

		String title = currentScreen.getTitle().getString();
		switch (title) {
			case AttributeAPI.HUNTING_BOX -> handleHuntingBox(lines);
			case AttributeAPI.ATTRIBUTE_MENU -> handleAttributeMenu(lines);
			case null, default -> {
			}
		}
	}

	@Override
	public int getPriority() {
		return priority;
	}

	private void handleHuntingBox(@NotNull List<Text> lines) {
		String levelStr = null;
		String ownedStr = null;
		String syphonCountStr = null;
		String rarityStr = null;
		String id = null;
		Matcher matcher = PLACEHOLDER_PATTERN.matcher("");

		for (Text line : lines) {
			String lineText = line.getString();
			if (lineText.isEmpty()) {
				continue;
			}

			matcher.reset(lineText);

			if (levelStr == null && matcher.usePattern(NAME_AND_LEVEL_PATTERN).matches()) {
				// I, II, VI
				levelStr = matcher.group(1);

			} else if (ownedStr == null && matcher.usePattern(OWNED_PATTERN).matches()) {
				// 1, 22
				ownedStr = matcher.group(1).replace(",", "");

			} else if (syphonCountStr == null && matcher.usePattern(SYPHON_COUNT_PATTERN).matches()) {
				// 10, 15 | X Maxed
				syphonCountStr = matcher.group(1);

			} else if (matcher.usePattern(AttributeAPI.RARITY_AND_ID_PATTERN).matches()) {
				rarityStr = matcher.group(1);
				// (ID: 22)
				id = matcher.group(2);
				break;
			}
		}

		if (levelStr == null || ownedStr == null || rarityStr == null || id == null) {
			return;
		}

		int level = RomanNumeralUtils.parse(levelStr);
		if (level < 0 || level > AttributeAPI.MAX_LEVEL) {
			return;
		}

		int owned = StonksUtils.toInt(ownedStr, -1);
		if (owned < 0) {
			return;
		}

		Rarity itemRarity = Rarity.valueOf(rarityStr.toUpperCase(Locale.ROOT));
		int shardsUntilMax = AttributeAPI.getShardsUntilMax(itemRarity, level + 1);
		if (shardsUntilMax < 0 || syphonCountStr == null) {
			return;
		}

		int syphonCount = StonksUtils.toInt(syphonCountStr, -1);
		if (syphonCount < 0) {
			return;
		}

		int required = shardsUntilMax + syphonCount;
		appendTooltip(lines, id, required, owned);
	}

	private void handleAttributeMenu(@NotNull List<Text> lines) {
		String id = null;
		String rarityStr = null;
		String levelStr = null;
		String syphonCountStr = null;
		Matcher matcher = PLACEHOLDER_PATTERN.matcher("");

		for (Text line : lines) {
			String lineText = line.getString();
			if (lineText.isEmpty()) {
				continue;
			}

			matcher.reset(lineText);

			if (id == null && matcher.usePattern(AttributeAPI.SOURCE_PATTERN).matches()) {
				// (ID:: 22)
				id = matcher.group("id");

			} else if (rarityStr == null && matcher.usePattern(RARITY_PATTERN).matches()) {
				rarityStr = matcher.group(1);

			} else if (levelStr == null && matcher.usePattern(LEVEL_PATTERN).matches()) {
				// 10, 15 | X Maxed
				levelStr = matcher.group(1);

			} else if (matcher.usePattern(SYPHON_COUNT_PATTERN).matches()) {
				// 6, 8 | X Maxed
				syphonCountStr = matcher.group(1);
				break;
			}
		}

		if (id == null || rarityStr == null || levelStr == null || syphonCountStr == null) {
			return;
		}

		int level = StonksUtils.toInt(levelStr, -1);
		if (level < 0) {
			return;
		}

		int syphonCount = StonksUtils.toInt(syphonCountStr, -1);
		if (syphonCount < 0) {
			return;
		}

		Rarity itemRarity = Rarity.valueOf(rarityStr.toUpperCase(Locale.ROOT));
		int shardsUntilMax = AttributeAPI.getShardsUntilMax(itemRarity, level + 1);
		if (shardsUntilMax < 0) {
			return;
		}

		int required = shardsUntilMax + syphonCount;
		// HuntingBox != AttributeMenu
		appendTooltip(lines, id, required, 0);
	}

	private void appendTooltip(@NotNull List<Text> lines, String id, int required, int owned) {
		lines.add(Text.empty()
				.append(Text.literal("Shards Until Maxed: ").formatted(Formatting.GREEN))
				.append(Text.literal(String.valueOf(required)).formatted(Formatting.AQUA)));

		SkyBlockAttribute attribute = CaribouStonks.core().getModDataSource().getAttributeById(id);
		if (attribute != null) {
			addTotalCost(lines, required - owned, attribute.skyBlockApiId());
		}
	}

	private void addTotalCost(List<Text> lines, int required, String skyBlockApiId) {
		if (required > 0 && hypixelDataSource.hasBazaarItem(skyBlockApiId)) {
			Optional<Product> product = hypixelDataSource.getBazaarItem(skyBlockApiId);
			if (product.isEmpty()) {
				lines.add(Text.literal("Bazaar item error.").formatted(Formatting.RED));
				return;
			}

			double price = BazaarItemAnalytics.buyPrice(product.get()) * required;
			String priceDisplay = StonksUtils.INTEGER_NUMBERS.format(price);

			lines.add(Text.literal("Cost To Max: ").formatted(Formatting.YELLOW)
					.append(Text.literal(priceDisplay + " Coins").formatted(Formatting.GOLD))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(price)).formatted(Formatting.GOLD))
					.append(Text.literal(")").formatted(Formatting.GRAY))
			);
		}
	}
}
