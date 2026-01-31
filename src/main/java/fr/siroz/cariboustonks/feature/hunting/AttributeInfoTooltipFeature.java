package fr.siroz.cariboustonks.feature.hunting;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.skyblock.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.skyblock.data.hypixel.bazaar.BazaarProduct;
import fr.siroz.cariboustonks.skyblock.item.SkyBlockAttribute;
import fr.siroz.cariboustonks.skyblock.AttributeAPI;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.system.container.tooltip.ContainerTooltipAppender;
import fr.siroz.cariboustonks.skyblock.data.hypixel.item.Rarity;
import fr.siroz.cariboustonks.util.RomanNumeralUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
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
		this.hypixelDataSource = CaribouStonks.skyBlock().getHypixelDataSource();
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
	public void appendToTooltip(@Nullable Slot focusedSlot, @NotNull ItemStack item, @NotNull List<Component> lines) {
		if (hypixelDataSource.isBazaarInUpdate()) return;

		Screen currentScreen = Minecraft.getInstance().screen;
		if (focusedSlot == null || currentScreen == null || lines.isEmpty()) return;
		if (StonksUtils.isEdgeSlot(focusedSlot.index, 6)) return;

		String title = currentScreen.getTitle().getString();
		switch (title) {
			case AttributeAPI.HUNTING_BOX -> handleHuntingBox(lines);
			case AttributeAPI.ATTRIBUTE_MENU -> handleAttributeMenu(lines);
			default -> {
			}
		}
	}

	@Override
	public int getPriority() {
		return priority;
	}

	private void handleHuntingBox(@NotNull List<Component> lines) {
		String levelStr = null;
		String ownedStr = null;
		String syphonCountStr = null;
		String rarityStr = null;
		String id = null;
		Matcher matcher = PLACEHOLDER_PATTERN.matcher("");

		for (Component line : lines) {
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

	private void handleAttributeMenu(@NotNull List<Component> lines) {
		String id = null;
		String rarityStr = null;
		String levelStr = null;
		String syphonCountStr = null;
		Matcher matcher = PLACEHOLDER_PATTERN.matcher("");

		for (Component line : lines) {
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

	private void appendTooltip(@NotNull List<Component> lines, String id, int required, int owned) {
		lines.add(Component.empty()
				.append(Component.literal("Shards Until Maxed: ").withStyle(ChatFormatting.GREEN))
				.append(Component.literal(String.valueOf(required)).withStyle(ChatFormatting.AQUA)));

		SkyBlockAttribute attribute = CaribouStonks.core().getModDataSource().getAttributeById(id);
		if (attribute != null) {
			addTotalCost(lines, required - owned, attribute.skyBlockApiId());
		}
	}

	private void addTotalCost(List<Component> lines, int required, String skyBlockApiId) {
		if (required > 0 && hypixelDataSource.hasBazaarItem(skyBlockApiId)) {
			Optional<BazaarProduct> product = hypixelDataSource.getBazaarItem(skyBlockApiId);
			if (product.isEmpty()) {
				lines.add(Component.literal("Bazaar item error.").withStyle(ChatFormatting.RED));
				return;
			}

			double price = product.get().buyPrice() * required;
			String priceDisplay = StonksUtils.INTEGER_NUMBERS.format(price);

			lines.add(Component.literal("Cost To Max: ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(priceDisplay + " Coins").withStyle(ChatFormatting.GOLD))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(price)).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY))
			);
		}
	}
}
