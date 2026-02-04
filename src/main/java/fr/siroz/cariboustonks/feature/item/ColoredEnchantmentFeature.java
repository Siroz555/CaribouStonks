package fr.siroz.cariboustonks.feature.item;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.mod.ModDataSource;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockEnchantment;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.ItemRenderEvents;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.RomanNumeralUtils;
import fr.siroz.cariboustonks.util.render.animation.AnimationUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	private final BooleanSupplier configShowMaxEnchants =
			() -> ConfigManager.getConfig().uiAndVisuals.coloredEnchantment.showMaxEnchants;

	private final BooleanSupplier configShowGoodEnchants =
			() -> ConfigManager.getConfig().uiAndVisuals.coloredEnchantment.showGoodEnchants;

	private final BooleanSupplier configMaxEnchantsRainbow =
			() -> ConfigManager.getConfig().uiAndVisuals.coloredEnchantment.maxEnchantsRainbow;

	public ColoredEnchantmentFeature() {
		this.modDataSource = CaribouStonks.mod().getModDataSource();
		ItemRenderEvents.TOOLTIP_APPENDER.register(this::onTooltipLine);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && (configShowMaxEnchants.getAsBoolean() || configShowGoodEnchants.getAsBoolean());
	}

	@Nullable
	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	@EventHandler(event = "ItemRenderEvents.TOOLTIP_APPENDER")
	private ItemLore onTooltipLine(ItemStack itemStack, ItemLore loreComponent) {
		if (itemStack == null || itemStack.isEmpty()) return null;
		if (loreComponent == null || loreComponent.lines().isEmpty()) return null;
		if (!isEnabled()) return null;
		if (!configShowMaxEnchants.getAsBoolean() && !configShowGoodEnchants.getAsBoolean()) return null;

		CompoundTag enchantments = ItemUtils.getCustomData(itemStack).getCompoundOrEmpty("enchantments");
		if (enchantments.isEmpty()) {
			return null;
		}

		Object2IntMap<String> maxEnchantmentColors = new Object2IntOpenHashMap<>();
		Object2IntMap<String> goodEnchantmentColors = new Object2IntOpenHashMap<>();
		for (String id : enchantments.keySet()) {

			SkyBlockEnchantment enchantment = modDataSource.getSkyBlockEnchantment(id);
			int level = enchantments.getIntOr(id, 0);
			if (enchantment != null && enchantment.isGoodOrMaxLevel(level) && level > 0) {

				String name = enchantment.name() + " " + RomanNumeralUtils.generate(level);
				if (enchantment.isMaxLevel(level)) {
					maxEnchantmentColors.put(name, maxEnchantsColor().getRGB());
				} else if (enchantment.isGoodLevel(level) && configShowGoodEnchants.getAsBoolean()) {
					goodEnchantmentColors.put(name, goodEnchantsColor().getRGB());
				}
			}
		}

		if (maxEnchantmentColors.isEmpty() && goodEnchantmentColors.isEmpty()) {
			return null;
		}

		boolean applied = false;
		List<Component> lines = loreComponent.lines().stream()
				.map(this::recursiveCopy)
				.collect(Collectors.toList());

		for (Component line : lines) {

			if (configShowMaxEnchants.getAsBoolean() && !maxEnchantmentColors.isEmpty()
					&& maxEnchantmentColors.keySet().stream().anyMatch(line.getString()::contains)
			) {
				if (configMaxEnchantsRainbow.getAsBoolean()) {
					ListIterator<Component> iterator = line.getSiblings().listIterator();
					while (iterator.hasNext()) {
						Component currentText = iterator.next();
						String fullText = currentText.getString();
						String enchant = trimEnchantName(fullText);

						//noinspection DataFlowIssue
						if (maxEnchantmentColors.containsKey(enchant)
								&& currentText.getStyle().getColor().getValue() == ChatFormatting.BLUE.getColor()
						) {
							// Extraire la partie après l'enchantement (virgule, espace)
							String suffix = fullText.substring(enchant.length());
							// Créer le nouveau component avec rainbow + le suffixe original
							MutableComponent newComponent = (MutableComponent) AnimationUtils.applyRainbow(enchant);
							if (!suffix.isEmpty()) {
								newComponent.append(Component.literal(suffix).withStyle(currentText.getStyle()));
							}

							iterator.set(newComponent);
							maxEnchantmentColors.removeInt(enchant);
							applied = true;
						}
					}
				} else {
					for (Component currentText : line.getSiblings()) {
						String enchant = trimEnchantName(currentText.getString());

						//noinspection DataFlowIssue
						if (maxEnchantmentColors.containsKey(enchant)
								&& currentText.getStyle().getColor().getValue() == ChatFormatting.BLUE.getColor()
						) {
							((MutableComponent) currentText).withColor(maxEnchantmentColors.getInt(enchant));
							maxEnchantmentColors.removeInt(enchant);
							applied = true;
						}
					}
				}
			}

			if (configShowGoodEnchants.getAsBoolean() && !goodEnchantmentColors.isEmpty()
					&& goodEnchantmentColors.keySet().stream().anyMatch(line.getString()::contains)
			) {
				for (Component currentText : line.getSiblings()) {
					String enchant = trimEnchantName(currentText.getString());

					//noinspection DataFlowIssue
					if (goodEnchantmentColors.containsKey(enchant)
							&& currentText.getStyle().getColor().getValue() == ChatFormatting.BLUE.getColor()
					) {
						((MutableComponent) currentText).withColor(goodEnchantmentColors.getInt(enchant));
						goodEnchantmentColors.removeInt(enchant);
						applied = true;
					}
				}
			}
		}

		// Le flag permet de s'assurer qu'il y a eu au moins un ou plusieurs changements
		if (applied) {
			return new ItemLore(lines);
		}

		return null;
	}

	private Color maxEnchantsColor() {
		return ConfigManager.getConfig().uiAndVisuals.coloredEnchantment.maxEnchantsColor;
	}

	private Color goodEnchantsColor() {
		return ConfigManager.getConfig().uiAndVisuals.coloredEnchantment.goodEnchantsColor;
	}

	@NotNull
	private MutableComponent recursiveCopy(@NotNull Component original) {
		MutableComponent copy = MutableComponent.create(original.getContents()).setStyle(original.getStyle());
		((ArrayList<Component>) copy.getSiblings()).ensureCapacity(original.getSiblings().size());

		for (Component sibling : original.getSiblings()) {
			copy.getSiblings().add(recursiveCopy(sibling));
		}

		return copy;
	}

	@NotNull
	private String trimEnchantName(@NotNull String enchantName) {
		int commaIndex = enchantName.indexOf(',');
		return commaIndex > -1 ? enchantName.substring(0, commaIndex).trim() : enchantName.trim();
	}
}
