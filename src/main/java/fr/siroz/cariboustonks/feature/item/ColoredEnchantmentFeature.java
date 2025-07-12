package fr.siroz.cariboustonks.feature.item;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.mod.ModDataSource;
import fr.siroz.cariboustonks.core.data.mod.SkyBlockEnchantment;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.ItemRenderEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.RomanNumeralUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Credits to AzureAaron (<a href="https://github.com/AzureAaron">GitHub AzureAaron</a>).
 * <p>
 * This logic almost the same, however, I don't integrate the “Rainbow” like he does,
 * his request to have a texture pack and to develop a Shader, I don't know anything about all that.
 * <p>
 * What's more, I think it's a shame that Skyblocker doesn't integrate it, either in “Rainbow” mode or as I do.
 * <p>
 * According to AzureAaron's Mod, you always have to activate the texture pack, it doesn't set automatically,
 * and then there are incompatibility issues with certain mods.
 */
public class ColoredEnchantmentFeature extends Feature {

	private final ModDataSource modDataSource;

	public ColoredEnchantmentFeature() {
		this.modDataSource = CaribouStonks.core().getModDataSource();
		ItemRenderEvents.TOOLTIP_APPENDER.register(this::onTooltipLine);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@Nullable
	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	@EventHandler(event = "ItemRenderEvents.TOOLTIP_APPENDER")
	private LoreComponent onTooltipLine(ItemStack itemStack, LoreComponent loreComponent) {
		if (itemStack == null || itemStack.isEmpty()) return null;
		if (loreComponent == null || loreComponent.lines().isEmpty()) return null;
		if (!isEnabled()) return null;

		NbtCompound enchantments = ItemUtils.getCustomData(itemStack).getCompoundOrEmpty("enchantments");
		if (enchantments.isEmpty()) {
			return null;
		}

		Object2IntMap<String> maxEnchantmentColors = new Object2IntOpenHashMap<>();
		Object2IntMap<String> goodEnchantmentColors = new Object2IntOpenHashMap<>();
		for (String id : enchantments.getKeys()) {

			SkyBlockEnchantment enchantment = modDataSource.getSkyBlockEnchantment(id);
			int level = enchantments.getInt(id, 0);
			if (enchantment != null && enchantment.isGoodOrMaxLevel(level) && level > 0) {

				String name = enchantment.name() + " " + RomanNumeralUtils.generate(level);
				if (enchantment.isMaxLevel(level)) {
					maxEnchantmentColors.put(name, maxEnchantsColor().getRGB());
				} else if (enchantment.isGoodLevel(level) && showGoodEnchants()) {
					goodEnchantmentColors.put(name, goodEnchantsColor().getRGB());
				}
			}
		}

		if (maxEnchantmentColors.isEmpty() && goodEnchantmentColors.isEmpty()) {
			return null;
		}

		List<Text> lines = loreComponent.lines().stream()
				.map(this::recursiveCopy)
				.collect(Collectors.toList());

		for (Text line : lines) {

			if (showMaxEnchants() && !maxEnchantmentColors.isEmpty()
					&& maxEnchantmentColors.keySet().stream().anyMatch(line.getString()::contains)
			) {
				for (Text currentText : line.getSiblings()) {
					String enchant = currentText.getString().trim();

					//noinspection DataFlowIssue
					if (maxEnchantmentColors.containsKey(enchant)
							&& currentText.getStyle().getColor().getRgb() == Formatting.BLUE.getColorValue()
					) {
						((MutableText) currentText).withColor(maxEnchantmentColors.getInt(enchant));
						maxEnchantmentColors.removeInt(enchant);
					}
				}
			}

			if (showGoodEnchants() && !goodEnchantmentColors.isEmpty()
					&& goodEnchantmentColors.keySet().stream().anyMatch(line.getString()::contains)
			) {
				for (Text currentText : line.getSiblings()) {
					String enchant = currentText.getString().trim();

					//noinspection DataFlowIssue
					if (goodEnchantmentColors.containsKey(enchant)
							&& currentText.getStyle().getColor().getRgb() == Formatting.BLUE.getColorValue()
					) {
						((MutableText) currentText).withColor(goodEnchantmentColors.getInt(enchant));
						goodEnchantmentColors.removeInt(enchant);
					}
				}
			}
		}

		return new LoreComponent(lines);
	}

	private boolean showMaxEnchants() {
		return ConfigManager.getConfig().uiAndVisuals.coloredEnchantment.showMaxEnchants;
	}

	private boolean showGoodEnchants() {
		return ConfigManager.getConfig().uiAndVisuals.coloredEnchantment.showGoodEnchants;
	}

	private Color maxEnchantsColor() {
		return ConfigManager.getConfig().uiAndVisuals.coloredEnchantment.maxEnchantsColor;
	}

	private Color goodEnchantsColor() {
		return ConfigManager.getConfig().uiAndVisuals.coloredEnchantment.goodEnchantsColor;
	}

	@NotNull
	private MutableText recursiveCopy(@NotNull Text original) {
		MutableText copy = MutableText.of(original.getContent()).setStyle(original.getStyle());
		((ArrayList<Text>) copy.getSiblings()).ensureCapacity(original.getSiblings().size());

		for (Text sibling : original.getSiblings()) {
			copy.getSiblings().add(recursiveCopy(sibling));
		}

		return copy;
	}
}
